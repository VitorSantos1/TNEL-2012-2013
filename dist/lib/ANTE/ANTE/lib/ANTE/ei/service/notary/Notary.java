package ei.service.notary;

import ei.ElectronicInstitution;
import ei.contract.ContractWrapper;
import ei.onto.notary.*;
import ei.service.PlatformService;

import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREInitiator;
import jade.proto.SimpleAchieveREResponder;
import jade.domain.FIPANames;

import java.util.List;
import java.util.Vector;
import java.util.Hashtable;
import java.security.Signature;
import java.security.SignedObject;
import java.security.PublicKey;

/**
 * This class provides the Notary service. The notary receives requests for validating and registering contracts, asks contractual
 * agents for signatures and sends contracts to the normative environment for monitoring purposes.
 * <p>
 * The notary implements the following interaction protocol roles:
 * <ul>
 * <li>the notary-registration responder role
 * <li>the contract-signing initiator role
 * <li>the ne-delegation initiator role
 * </ul>
 * 
 * @author hlc
 */
public class Notary extends PlatformService {
	
	private static final long serialVersionUID = 4704036859575023354L;
	
	private String xsd_file;
	private String contract_dir;
	
	// the known public-keys
	private Hashtable<AID,PublicKey> publicKeyHashTable = new Hashtable<AID,PublicKey>();
	
	/**
	 * Set up of the notary agent.
	 */
	protected void setup() {
		super.setup();
		
		// register ontologies
		getContentManager().registerOntology(ContractSigningOntology.getInstance());
		
		// get the xsd_file argument
   		xsd_file = getConfigurationArguments().getProperty("contract_xsd");
   		
		// check if a contract directory was given as an argument to the agent
		contract_dir = getConfigurationArguments().getProperty("contract_dir");
		if(contract_dir == null) contract_dir = "";
		
		// responder role for NOTARY_REGISTRATION_ONTOLOGY
		addBehaviour(new NotaryRegistrationResp(this));
	}
	
	
	/**
	 * Inner class to implement the notary-registration responder role.
	 * 
	 * @author hlc
	 */
	private class NotaryRegistrationResp extends SimpleAchieveREResponder {
		
		private static final long serialVersionUID = 4934775773092673544L;
		
		/**
		 * Creates a <code>NotaryRegistrationResp</code> instance
		 *
		 * @param agent The agent that adds the behaviour.
		 */
		NotaryRegistrationResp(Agent agent) {
			super(agent, MessageTemplate.and(
					SimpleAchieveREResponder.createMessageTemplate(FIPA_REQUEST),
					MessageTemplate.MatchOntology(ElectronicInstitution.NOTARY_REGISTRATION_ONTOLOGY)));
		}
		
	    protected ACLMessage prepareResponse(ACLMessage request) {
	    	return null;
        }
		
		/**
		 * Prepares an appropriate reply.
		 */
		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
			ACLMessage reply = request.createReply();
			
			// get contract to register
			ContractWrapper cw = new ContractWrapper(((Notary) myAgent).xsd_file);
			cw.unmarshal(request.getContent(), true);
			/*
			 TODO verify the contract contents... e.g.:
			    - according to <type>, check that all the required <contractual-info> is there --> THIS SHOULD BE MADE BY NORM-ENV
			    *** That is, should contact norm-env *before* accepting to register contract in the notary and initiating the signing process...
				  (maybe should have one validator for each type of contract)
				- check that none of the involved agents is forbidden to create contracts (ask the EI/NE?)
				- ...
			*/
			if(/**/true) {
				// if ok, provide contract with an ID and send inform message
				String id = ((cw.getType() != null) ? cw.getType() : "") + "_" + System.currentTimeMillis() + Math.random();
				cw.setId(id);
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent(id);
				
				// start contract-signing initiator role
				addBehaviour(new ContractSigningInit_Based_on_ContractNet(myAgent, cw));
			} else {
				// if not ok, send failure message
				reply.setPerformative(ACLMessage.FAILURE);
				reply.setContent("invalid contract");
			}
			
			// return the created reply
			return reply;
		}
		
	} // end NotaryRegistrationResp class
	
	
	/**
	 * Inner class to implement the contract-signing initiator role.
	 * TODO For now this role is based on the ContractNet with the following semantics:
	 * Original sequence			ContractNet sequence
	 * -----------------			--------------------
	 * REQUEST						CFP
	 * INFORM(signature)			PROPOSE
	 * CONFIRM/DISCONFIRM			ACCEPT_PROPOSAL/REJECT_PROPOSAL
	 * 								INFORM
	 * Should define a new protocol.
	 * 
	 * @author hlc
	 */
	private class ContractSigningInit_Based_on_ContractNet extends jade.proto.ContractNetInitiator {
		
		private static final long serialVersionUID = 6077718769749511737L;
		
		private ContractWrapper cw;
		
		ContractSigningInit_Based_on_ContractNet(Agent agent, ContractWrapper cw) {
			super(agent, new ACLMessage(ACLMessage.CFP));
			
			this.cw = cw;
			log("Start of signing process for " + cw.getId());
		}
		
		protected Vector<ACLMessage> prepareCfps(ACLMessage msg) {
			// set the protocol of the request to be sent
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
			// set the ontology of the request to be sent
			msg.setOntology(ElectronicInstitution.CONTRACT_SIGNING_ONTOLOGY);
			// add the contract as content of the request to be sent
			msg.setContent(cw.marshal(true));
			// set reply-by to the starting date of the contract
			// TODO - TESTAR!!! - está em comentário pq ainda não testei!
//			try {
//				msg.setReplyByDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(contract.getDate()));
//			} catch (ParseException pe) {
//				pe.printStackTrace();
//			}
			
			// add each contract participant as receiver of a request to be sent
			Vector<ACLMessage> v = new Vector<ACLMessage>();
			List<String> ags = cw.getContract().getHeader().getWho().getAgent();
			ACLMessage msg_clone;
			for(int i=0; i<ags.size(); i++) {
				msg_clone = (ACLMessage) msg.clone();
				msg_clone.addReceiver(new AID(ags.get(i), false));
				v.add(msg_clone);
			}
			
			// return the vector of requests
			return v;
		}
		
		protected void handlePropose(ACLMessage propose, Vector acceptances) {
			log(cw.getId() + ": Received signature from " + propose.getSender());
		}
		
		protected void handleRefuse(ACLMessage refuse) {
			log(cw.getId() + ": Received refuse from " + refuse.getSender());
		}
		
		protected void handleAllResponses(Vector responses, Vector acceptances) {
			// check if all results are proposes and are signed
			AID problem = null;
			for(int i=0; i<responses.size() && problem == null; i++) {
				ACLMessage msg = (ACLMessage) responses.elementAt(i);
				
				if(msg.getPerformative() != ACLMessage.PROPOSE || !checkContractSignature(msg))
					// problem with one of the signatures - signing process failed
					problem = msg.getSender();
			}
			
			if(problem != null) {
				// prepare reject message to send to each contractual agent
				for(int i=0; i<responses.size(); i++) {
					ACLMessage reply = ((ACLMessage) responses.elementAt(i)).createReply();
					reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
					reply.setLanguage(getSlCodec().getName());
					ContractSigningFailed csf = new ContractSigningFailed();
					csf.setContractId(cw.getId());
					csf.setCause(problem);
					try {
						myAgent.getContentManager().fillContent(reply, csf);
					} catch(Codec.CodecException ce) {
						ce.printStackTrace();
					} catch(OntologyException oe) {
						oe.printStackTrace();
					}
					acceptances.add(reply);
				}
			} else {
				// no problem: prepare accept message to send to each contractual agent
				for(int i=0; i<responses.size(); i++) {
					ACLMessage reply = ((ACLMessage) responses.elementAt(i)).createReply();
					reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					reply.setLanguage(getSlCodec().getName());
					ContractSigned cs = new ContractSigned();
					cs.setContractId(cw.getId());
					try {
						myAgent.getContentManager().fillContent(reply, cs);
					} catch(Codec.CodecException ce) {
						ce.printStackTrace();
					} catch(OntologyException oe) {
						oe.printStackTrace();
					}
					acceptances.add(reply);
				}
				
				// save a copy of the contract
//				cw.save(contract_dir + java.io.File.separator + cw.getId() + ".xml", true);
				
				// start ne-delegation initiator role
				try {
					myAgent.addBehaviour(new NEDelegationInit(myAgent, cw));
				} catch(Exception e) {
					logErr("Error: no normative environment agent found");
				}
			}
		}
		
		protected void handleAllResultNotifications(Vector resultNotifications) {
			log("End of signing process for " + cw.getId());
		}
		
		private boolean checkContractSignature(ACLMessage msg) {
			// get public-key
			PublicKey publicKey = publicKeyHashTable.get(msg.getSender());
			if(publicKey == null) {
				publicKey = retrievePublicKey(msg.getSender());
				if(publicKey == null) {
					return false;
				}
				// store public-key in hashtable
				publicKeyHashTable.put(msg.getSender(), publicKey);
			}
			
			// verify contract signature
			try {
				Signature sig = Signature.getInstance(publicKey.getAlgorithm());
				// verify the signed object
				SignedObject so = (SignedObject) msg.getContentObject();
				if(so.verify(publicKey, sig)) {
					log(cw.getId() + ": VALID SIGNATURE FROM AGENT " + msg.getSender().getLocalName());
					ContractWrapper cw2 = new ContractWrapper(((Notary) myAgent).xsd_file);
					cw2.unmarshal((String) so.getObject(), true);
					// TODO is cw2 equal to cw? That is, is the signed contract the one expected to be signed?
				} else {
					// problem with one of the signatures - signing process failed
					log(cw.getId() + ": INVALID SIGNATURE FROM AGENT " + msg.getSender().getLocalName());
					return false;
				}
			} catch (Exception e) {
				return false;
			}
			return true;
		}
		
	} // end ContractSigningInit_Based_on_ContractNet class
	
	
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

	protected boolean createGUI() {
		return false;
	}
}
