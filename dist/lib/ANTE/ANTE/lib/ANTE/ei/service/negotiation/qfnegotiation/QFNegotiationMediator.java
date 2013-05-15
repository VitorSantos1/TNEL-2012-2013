package ei.service.negotiation.qfnegotiation;

import ei.EIAgent;
import ei.ElectronicInstitution;

import ei.agent.enterpriseagent.ontology.Need;
import ei.agent.gui.InfoGUI;
import ei.contract.*;

import ei.service.ctr.OutcomeGenerator.MappingMethod;
import ei.service.negotiation.NegotiationFacilitator;
import ei.util.LimitedSizeLinkedHashMap;
import ei.onto.negotiation.Negotiate;
import ei.onto.negotiation.qfnegotiation.*;
import ei.onto.ctr.CTROntology; 
import ei.onto.ctr.SendTopAgents;
import ei.proto.ctr.CTRParallelInits_SendTopAgents;
import ei.proto.negotiation.InterestManifestationParallelInits;
import ei.proto.negotiation.qfnegotiation.QFNegotiationInit;
import ei.proto.negotiation.qfnegotiation.QFNegotiationParallelInits;

import jade.proto.SimpleAchieveREInitiator;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;

import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WrapperBehaviour;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import jade.util.leap.List;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * The QF negotiation mediator (QFNegMed from now on) is a negotiation facilitator for the QF-negotiation protocol.
 * When an enterprise agent (EA-starter from now on) requests this service, QFNegMed starts a QF negotiation process.
 * 
 * The negotiation process includes three steps:
 * <ol>
 * <li> Interest Manifestation: for each need, QFNegMed will ask each EnterpriseAgent registered as a seller at the DF whether he is interested in participating in a negotiation.
 * 		@see InterestManifestationParallelInits
 * <li> CTR filtering: QFNegMed will use CTR to get the most trustworthy agents for each need, according to the EA-starter's preferences.
 * 		@see CTRParallelInits_SendTopAgents
 * <li> Negotiation Protocol: QFNegMed will start a qf-negotiation for each need.
 * 		@see QFNegotiationParallelInits
 * </ol>
 * If every essential need is negotiated successfully, a contract is generated.
*/
public class QFNegotiationMediator extends NegotiationFacilitator {
	private static final long serialVersionUID = -7050712646203218597L;
	
	private String xsd_file;
	
	private LimitedSizeLinkedHashMap<String,QFNegotiationParallelInits> needsNegotiations = new LimitedSizeLinkedHashMap<String, QFNegotiationParallelInits>(); //negotiationId -> ...
	
	// acquainted sellers
	private Vector<InterestedAgent> acquaintedSellers = null;

	protected void setup(){
		super.setup();

		// register additional ontologies
		getContentManager().registerOntology( QFNegotiationOntology.getInstance() );
		getContentManager().registerOntology( CTROntology.getInstance() );

		// defines the maximum number of entry in the MyLinkedHashMap structure
		if(getConfigurationArguments().containsKey("maxEntries_needsNegotiation")) {
			needsNegotiations.setMaxEntries(Integer.parseInt((String)getConfigurationArguments().get("maxEntries_needsNegotiation")));
		}
		
		xsd_file = getConfigurationArguments().getProperty("contract_xsd"); // get the xsd_file argument
	}
	
	private Vector<InterestedAgent> getSellers(Need need, boolean forceDF) {
		if(acquaintedSellers != null && !forceDF) {
			return acquaintedSellers;
		} else {
			acquaintedSellers = new Vector<InterestedAgent>();
			DFAgentDescription template = new DFAgentDescription();
			template = new DFAgentDescription();
			ServiceDescription sd1 = new ServiceDescription();
			sd1.setType(ElectronicInstitution.ROLE_SELLER_AGENT);
//			sd1.setType(need.getIsicClass()); // TODO 
			template.addServices(sd1);
			DFAgentDescription[] result = null; //the result variable is an array of agents who are registered as enterprise agents
			try {
				result = DFService.search(this, template);
			} catch (FIPAException e) {
				e.printStackTrace();
			}
			for(int a=0;a<result.length;a++) {
				acquaintedSellers.add(new InterestedAgent(result[a].getName()));
			}
			return acquaintedSellers;
		}
	}

	public LimitedSizeLinkedHashMap<String, QFNegotiationParallelInits> getNeedsNegotiations() {
		return needsNegotiations;
	}
	
	
	protected Behaviour createResponderForNegotiateActionRequest(Agent agent, ACLMessage request, Negotiate negotiate) {
		if(negotiate.getNegotiationProtocol().equals(ElectronicInstitution.QF_NEGOTIATION_PROTOCOL)) {
			return new QFNegotiationSeq(agent, request, negotiate);
		} else {
			ACLMessage response = request.createReply();
			response.setPerformative(ACLMessage.REFUSE);
			response.setContent("unsupported negotiation protocol");
			send(response);
			return null;
		}
	}

	
	private class QFNegotiationSeq extends SequentialBehaviour {
		private static final long serialVersionUID = 1L;
		
		private Hashtable<Need,Vector<InterestedAgent>> agentsPerNeed = new Hashtable<Need,Vector<InterestedAgent>>();
		
		protected QFNegotiationParallelInits qFNegotiationParallelInits;
		private ACLMessage request;
		private Negotiate negotiate;
		private String negotiationsID; 		
		
		QFNegotiationSeq(Agent agent, ACLMessage request, Negotiate negotiate) {
			super(agent);
			
			this.request = request;
			this.negotiate = negotiate;
			this.negotiationsID = request.getSender().getLocalName() + ":" + request.getConversationId();

			// create initial structure for agents per need (get info from DF)				
			for (int i=0; i < negotiate.getNeeds().size(); i++) {
				Need need = (Need) negotiate.getNeeds().get(i);

				// retrieves enterprise agents from the DF (Directory Facilitator) 
				Vector<InterestedAgent> sellers;
				if(!isInSynchronizedExperiment()) {
					sellers = getSellers(need, true);
				} else {
					sellers = getSellers(need, false);
				}

				agentsPerNeed.put(need, sellers);
			}

			// interest manifestation phase
			InterestManifestationParallelInits interestManifestationParallelInits = new InterestManifestationParallelInits(myAgent, getContractType(), request.getSender());
			addSubBehaviour(new InterestManifestationParallelInitsWrapper(interestManifestationParallelInits));

			// trust usage phase - retrieve trustworthiness values (either with pre-selection or not)
			boolean useTrustInPreselection = (negotiate.getNegotiationParameters().get(QFNegotiationParameters.USE_TRUST_IN_PRESELECTION) != null) ? Boolean.parseBoolean(negotiate.getNegotiationParameters().get(QFNegotiationParameters.USE_TRUST_IN_PRESELECTION)) : QFNegotiationParameters.USE_TRUST_IN_PRESELECTION_DEFAULT;
			boolean useTrustInProposalEvaluation = (negotiate.getNegotiationParameters().get(QFNegotiationParameters.USE_TRUST_IN_PROPOSAL_EVALUATION) != null) ? Boolean.parseBoolean(negotiate.getNegotiationParameters().get(QFNegotiationParameters.USE_TRUST_IN_PROPOSAL_EVALUATION)) : QFNegotiationParameters.USE_TRUST_IN_PROPOSAL_EVALUATION_DEFAULT;
			boolean useTrustInContractDrafting = (negotiate.getNegotiationParameters().get(QFNegotiationParameters.USE_TRUST_IN_CONTRACT_DRAFTING) != null) ? Boolean.parseBoolean(negotiate.getNegotiationParameters().get(QFNegotiationParameters.USE_TRUST_IN_CONTRACT_DRAFTING)) : QFNegotiationParameters.USE_TRUST_IN_CONTRACT_DRAFTING_DEFAULT;
			if (useTrustInPreselection || useTrustInProposalEvaluation || useTrustInContractDrafting) {
				int topNumberOfAgents;
				if (useTrustInPreselection) {	// pre-select?
					topNumberOfAgents = (negotiate.getNegotiationParameters().get(QFNegotiationParameters.TOP_N) != null) ? Integer.parseInt(negotiate.getNegotiationParameters().get(QFNegotiationParameters.TOP_N)) : QFNegotiationParameters.TOP_N_DEFAULT;				
				} else {
					topNumberOfAgents = SendTopAgents.ALL;
				}
				boolean useContextualFitness = (negotiate.getNegotiationParameters().get(QFNegotiationParameters.USE_CONTEXTUAL_FITNESS) != null) ? Boolean.parseBoolean(negotiate.getNegotiationParameters().get(QFNegotiationParameters.USE_CONTEXTUAL_FITNESS)) : QFNegotiationParameters.USE_CONTEXTUAL_FITNESS_DEFAULT;
				MappingMethod mapMethod = (negotiate.getNegotiationParameters().get(QFNegotiationParameters.MAPPING_METHOD) != null) ? MappingMethod.values()[Integer.parseInt(negotiate.getNegotiationParameters().get(QFNegotiationParameters.MAPPING_METHOD))] : QFNegotiationParameters.MAPPING_METHOD_DEFAULT;

				CTRParallelInits_SendTopAgents cTRParallelInits_SendTopAgents = new CTRParallelInits_SendTopAgents(myAgent, request.getSender(), topNumberOfAgents, useContextualFitness, mapMethod);
				addSubBehaviour(new CTRParallelInits_SendTopAgentsWrapper(cTRParallelInits_SendTopAgents));
			}

			// negotiation phase
			if(!isInSynchronizedExperiment()) {
				// add number of rounds
				if(getConfigurationArguments().getProperty(QFNegotiationParameters.NEGOTIATION_ROUNDS) != null) {
					negotiate.getNegotiationParameters().add(QFNegotiationParameters.NEGOTIATION_ROUNDS, getConfigurationArguments().getProperty(QFNegotiationParameters.NEGOTIATION_ROUNDS));
				}
			}
			qFNegotiationParallelInits = new QFNegotiationParallelInits(myAgent, negotiationsID, request.getSender(), negotiate.getNegotiationParameters());
			addSubBehaviour(new QFNegotiationParallelInitsWrapper(qFNegotiationParallelInits));
		}
		
		private String getContractType() {
			return (negotiate.getNegotiationParameters().get(QFNegotiationParameters.CONTRACT_TYPE) != null) ? 
					negotiate.getNegotiationParameters().get(QFNegotiationParameters.CONTRACT_TYPE) : QFNegotiationParameters.CONTRACT_TYPE_DEFAULT;
		}
		
		public Hashtable<Need, Vector<InterestedAgent>> getAgentsPerNeed() {
			return agentsPerNeed;
		}

		public void setAgentsPerNeed(Hashtable<Need, Vector<InterestedAgent>> agentsPerNeed) {
			this.agentsPerNeed = agentsPerNeed;
		}

		public int onEnd() {
			ACLMessage response = request.createReply();
			
			if(checkAgentsPerNeedStatus(agentsPerNeed)) {
				response.setPerformative(ACLMessage.INFORM);

				// fill contract contents
				ContractWrapper cw = ((QFNegotiationMediator) myAgent).fillContract(qFNegotiationParallelInits.getWinningProposals(), request.getSender(), getContractType());
				
				if(!isInSynchronizedExperiment()) {
					response.setContent(cw.marshal(true));

					// send contract to Notary...
					AID notaryAID = fetchAgent(ElectronicInstitution.SRV_NOTARY, false);
					if(notaryAID != null) {
						myAgent.addBehaviour(((QFNegotiationMediator) myAgent).new NotaryRegistrationInit(((QFNegotiationMediator) myAgent), cw, notaryAID));
					} else {
						logErr("NOTARY NOT FOUND!!!");
					}
				} else {
					// synchronized experiment: avoid notary
					// generate contract id
					String id = ((cw.getType() != null) ? cw.getType() : "") + "_" + System.currentTimeMillis() + Math.random();
					cw.setId(id);
					// delegate monitoring to normenv directly (no notary in between)
					try {
						myAgent.addBehaviour(new NEDelegationInit(myAgent, cw));
					} catch(Exception e) {
						logErr("Error: no normative environment agent found");
					}
					
					// create NegotiationOutcome
					NegotiationOutcome negotiationOutcome = null;
					
					// FIXME: for now we are assuming that the negotiation concerned only one need (the for loop will be executed only once)
					for (QFNegotiationInit qNegotiation : qFNegotiationParallelInits.getQFNegotiationInitList()) {
						if(qNegotiation.isSuccess()) {
							double winnerProposalUtility = qNegotiation.getWinnerProposal().getEvaluation();
							double fine = qNegotiation.getWinnerProposal().getFine();
							double lastRoundAvgUtility = qNegotiation.getLastRoundAvgUtility();
							double lastRoundUtilityStDev = qNegotiation.getStandardDeviation();					
							AID winner = qNegotiation.getWinnerProposal().getIssuer();
							double riskTolerance = (negotiate.getNegotiationParameters().get(QFNegotiationParameters.RISK_TOLERANCE) != null) ? 
										Double.parseDouble(negotiate.getNegotiationParameters().get(QFNegotiationParameters.RISK_TOLERANCE)) : QFNegotiationParameters.RISK_TOLERANCE_DEFAULT;

							negotiationOutcome = new NegotiationOutcome(winnerProposalUtility, fine, qNegotiation.getNumberOfRefusedSuppliers(), lastRoundAvgUtility, lastRoundUtilityStDev, winner, id, riskTolerance);
						}
					}
					try {
						response.setContentObject(negotiationOutcome);
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			} else {
				if(getGUI() != null) {
					InfoGUI infoGUI = new InfoGUI("Negotiation on has failed", getLocalName(), getGUI());
					infoGUI.setVisible(true);
				} else {
					log("Negotiation on has failed");
				}
				response.setPerformative(ACLMessage.FAILURE);
			}
			
			send(response);
			
			return 0;
		}
		
	} // end QFNegotiationSeq class
	
	
	/**
	 * Check the availability of agents for each need. More specifically, this method verifies that there is at least one agent for
	 * at least one of the needs, and that every essential need has at least one agent.
	 */
	protected boolean checkAgentsPerNeedStatus(Hashtable<Need,Vector<InterestedAgent>> agentsPerNeed) {
		boolean atLeastOneNeedHasAgents = false;
		boolean everyEssencialNeedHasAgents = true;

		Enumeration<Need> needs = agentsPerNeed.keys();
		while(needs.hasMoreElements()) {
			Need need = needs.nextElement();
			Vector<InterestedAgent> agents = agentsPerNeed.get(need);
			if (agents.size() > 0) {
				atLeastOneNeedHasAgents = true;
			} else {
				if (need.isEssential()) {
					everyEssencialNeedHasAgents = false;
				}
			}
		}
		
		return atLeastOneNeedHasAgents && everyEssencialNeedHasAgents;
	}
	
	
	private class InterestManifestationParallelInitsWrapper extends WrapperBehaviour {
		private static final long serialVersionUID = 1L;

		InterestManifestationParallelInitsWrapper(InterestManifestationParallelInits interestManifestationParallelInits) {
			super(interestManifestationParallelInits);
		}
		
		public void onStart() {
			// check if we should move on
			if(checkAgentsPerNeedStatus(((QFNegotiationSeq) parent).getAgentsPerNeed())) {
				((InterestManifestationParallelInits) getWrappedBehaviour()).setAgentsPerNeeed(((QFNegotiationSeq) parent).getAgentsPerNeed());
				getWrappedBehaviour().onStart();
			}
		}

		public int onEnd() {
			getWrappedBehaviour().onEnd();
			((QFNegotiationSeq) parent).setAgentsPerNeed(((InterestManifestationParallelInits) getWrappedBehaviour()).getAgentsPerNeed());
			
			if(!checkAgentsPerNeedStatus(((QFNegotiationSeq) parent).getAgentsPerNeed())) {
				if(getGUI() != null) {
					InfoGUI infoGUI = new InfoGUI("Not enough interested agents", getLocalName(), getGUI());
					infoGUI.setVisible(true);
				} else {
					log("Not enough interested agents");
				}				
			}
			
			return 0;
		}
		
	}
	
	
	private class CTRParallelInits_SendTopAgentsWrapper extends WrapperBehaviour {
		private static final long serialVersionUID = 1L;

		CTRParallelInits_SendTopAgentsWrapper(CTRParallelInits_SendTopAgents cTRParallelInits_SendTopAgents) {
			super(cTRParallelInits_SendTopAgents);
		}
		
		public void onStart() {
			// check if we should move on
			if(checkAgentsPerNeedStatus(((QFNegotiationSeq) parent).getAgentsPerNeed())) {
				((CTRParallelInits_SendTopAgents) getWrappedBehaviour()).setAgentsPerNeed(((QFNegotiationSeq) parent).getAgentsPerNeed());
				getWrappedBehaviour().onStart();
			}
		}
		
		public int onEnd() {
			((QFNegotiationSeq) parent).setAgentsPerNeed(((CTRParallelInits_SendTopAgents) getWrappedBehaviour()).getAgentsPerNeed());
			getWrappedBehaviour().onEnd();

			if(!checkAgentsPerNeedStatus(((QFNegotiationSeq) parent).getAgentsPerNeed())) {
				if(getGUI() != null) {
					InfoGUI infoGUI = new InfoGUI("Not enough qualifying agents for this needs", getLocalName(), getGUI());
					infoGUI.setVisible(true);
				} else {
					log("Not enough qualifying agents for this needs");
				}				
			}

			return 0;
		}
	}
	

	protected class QFNegotiationParallelInitsWrapper extends WrapperBehaviour {
		private static final long serialVersionUID = 1L;
		private QFNegotiationParallelInits qfNegotiationParallelInits;
		
		QFNegotiationParallelInitsWrapper(QFNegotiationParallelInits qfNegotiationParallelInits) {
			super(qfNegotiationParallelInits);
			this.qfNegotiationParallelInits = qfNegotiationParallelInits;			
		}
		
		public void onStart() {
			// check if we should move on
			if(checkAgentsPerNeedStatus(((QFNegotiationSeq) parent).getAgentsPerNeed())) {
				
				// check if we should move on				
				((QFNegotiationParallelInits) getWrappedBehaviour()).setAgentsPerNeed(((QFNegotiationSeq) parent).getAgentsPerNeed());
				getWrappedBehaviour().onStart();
			}
			
			//update needs negotiations
			((QFNegotiationMediator) myAgent).getNeedsNegotiations().put(qfNegotiationParallelInits.negotiationsID, qfNegotiationParallelInits);
		}
		
		public int onEnd() {
			((QFNegotiationSeq) parent).setAgentsPerNeed(((QFNegotiationParallelInits) getWrappedBehaviour()).getAgentsPerNeed());
			getWrappedBehaviour().onEnd();
			
			if(getGUI() != null) {
				String state = checkAgentsPerNeedStatus(((QFNegotiationSeq) parent).getAgentsPerNeed())?"successful":"FAILURE";
				((QFNegotiationMediatorGUI) ((QFNegotiationMediator) myAgent).gui).addNegotiationRow(qfNegotiationParallelInits.negotiationsID, state);
			}
			
			return 0;
		}
		
	} // end QFNegotiationParallelInitsWrapper class
	
	
	/**
	 * Inner class to implement the ne-delegation initiator role.
	 * The notary will ask the normative environment to monitor a contract.
	 * 
	 * @author hlc
	 */
	private class NEDelegationInit extends SimpleAchieveREInitiator {
		private static final long serialVersionUID = -8444859612671673642L;
		
		private AID neAID;	// the normative environment's AID
		private ContractWrapper cw;
		
		/**
		 * Creates an <code>NEDelegationInit</code> instance
		 *
		 * @param agent		The agent that adds the behaviour.
		 * @param contract	The contract to ask the normative environment to monitor.
		 */
		NEDelegationInit(Agent agent, ContractWrapper cw) {
			super(agent, new ACLMessage(ACLMessage.REQUEST));
			
			this.cw = cw;
			neAID = fetchAgent(ElectronicInstitution.SRV_NORMATIVE_ENVIRONMENT, false);
		}
		
		/**
		 * Prepares the request message.
		 * 
		 * @param msg	The message passed to the constructor of SimpleAchieveREInitiator.
		 */
		protected ACLMessage prepareRequest(ACLMessage msg) {
			// set the protocol of the request to be sent
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			// set the ontology of the request to be sent
			msg.setOntology(ElectronicInstitution.NE_DELEGATION_ONTOLOGY);
			// add the normative environment as receiver of the request to be sent
			msg.addReceiver(neAID);
			// add the contract as content of the request to be sent
			msg.setContent(cw.marshal(true));
			// return the message to use as request
			return msg;
		}

		/**
		 * Handles an agree message from the normative environment.
		 */
		protected void handleAgree(ACLMessage agree) {
			log("The normative environment agreed to monitor the contract");
		}

		/**
		 * Handles a refuse message from the normative environment.
		 */
		protected void handleRefuse(ACLMessage refuse) {
			logErr("PANIC: the normative environment refused to monitor the contract :(");
		}
		
		/**
		 * Handles an inform message from the normative environment.
		 */
		protected void handleInform(ACLMessage inform) {
			log("Contract is being monitored by the normative environment");
		}
		
		/**
		 * Handles a failure message from the normative environment.
		 */
		protected void handleFailure(ACLMessage failure) {
			logErr("PANIC: the normative environment failed to start monitoring the contract :(");
		}
		
	} // end NEDelegationInit class

	/**
	 * Fills the contract with information from the results of the negotiation
	 * @param winningProposals the winning proposals
	 */
	private ContractWrapper fillContract(List winningProposals, AID sender, String contractType) {
		// using reflection to create the proper contract generator
		
		// create contract generator class name from contract type:
		// - the first character and all characters after hyphens ('-') or underscores ('_') will be uppercased
		// - hyphens and underscores will be removed
		StringBuffer contractGenerator = new StringBuffer("ei.contract.");
		boolean capitalizeNext = true;
		for(int i=0; i<contractType.length(); i++) {
			if(contractType.charAt(i) == '-' || contractType.charAt(i) == '_') {
				capitalizeNext = true;
			} else {
				if(capitalizeNext) {
					contractGenerator.append(Character.toUpperCase(contractType.charAt(i)));
					capitalizeNext = false;
				} else {
					contractGenerator.append(contractType.charAt(i));
				}
			}
		}
		contractGenerator.append("Generator");
		
		try {
			Constructor c = Class.forName(contractGenerator.toString()).getConstructor(new Class[]{AID.class, jade.util.leap.List.class});
			return ((AbstractContractGenerator) c.newInstance(sender, winningProposals)).generateContract(xsd_file);
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
 	}

	/**
	 * Inner class to implement the notary-registration initiator role.
	 */
	private class NotaryRegistrationInit extends SimpleAchieveREInitiator {
		private static final long serialVersionUID = 5486722326933397788L;
		
		AID notaryAID;
		ContractWrapper cw;

		NotaryRegistrationInit(Agent a, ContractWrapper cw, AID notaryAID) {
			super(a, new ACLMessage(ACLMessage.REQUEST));
			this.notaryAID = notaryAID;
			this.cw = cw;
		}

		protected ACLMessage prepareRequest(ACLMessage msg) {
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			// set the ontology of the request to be sent
			msg.setOntology(ei.ElectronicInstitution.NOTARY_REGISTRATION_ONTOLOGY);
			// add the notary as receiver of the request to be sent
			msg.addReceiver(notaryAID);
			// add the target contract as content of the request to be sent
			msg.setContent(cw.marshal(true));
			// return the message to use as request
			return msg;
		}

		/**
		 * Handles an agree message from the notary.
		 */
		protected void handleAgree(ACLMessage agree) {}

		/**
		 * Handles a refuse message from the notary.
		 */
		protected void handleRefuse(ACLMessage refuse) {}

		/**
		 * Handles an inform message from the notary.
		 */
		protected void handleInform(ACLMessage inform) {
			//((EIAgent) myAgent).log("Notary registered contract with id " + inform.getContent());
		}

		/**
		 * Handles a failure message from the notary.
		 */
		protected void handleFailure(ACLMessage failure) {
			((EIAgent) myAgent).logErr("PANIC: failed to register contract in the notary!");
		}

	} // end NotaryRegistrationInit class


	protected boolean createGUI() {
		gui = new QFNegotiationMediatorGUI(this);		
		return true;
	}
}
