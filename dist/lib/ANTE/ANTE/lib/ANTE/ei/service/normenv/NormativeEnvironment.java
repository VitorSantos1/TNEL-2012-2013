/*
 * From [jade-develop] archives (First quarter 2007): Problem with responders Behaviour...
 * ---
 * Caire: A good approach is to have one responder per ontology-protocol pair. In this way you never have problems with behaviours
 * getting messages that should be processed by other behaviours.
 * --> If both responders serve actions of the same ontology and follow the same protocol (fipa-request), then you should take into
 * account the opportunity of merging them into a single behaviour.
 * ---
 * Jordi: If there are different actions with the same ontology and the same protocol, you can implement your own MessageTemplate
 * to distinguish them.
 * ---
 * 
 * Should there be only one ontology for all conversations where the NE has a responding role?
 * What about conversations that include content objects (e.g. ne-delegation)? Are they "groupable" with the other ontologies?
 * The thing is: they are also FIPA-REQUEST conversations!
 */
package ei.service.normenv;

import ei.ElectronicInstitution;
import ei.contract.ContractWrapper;
import ei.onto.normenv.management.*;
import ei.onto.normenv.illocution.IllocutionOntology;
import ei.onto.normenv.report.*;
import ei.service.PlatformService;
import ei.service.normenv.NormEnvBehaviour;

import jade.content.AgentAction;
import jade.content.ContentElement;
import jade.content.Predicate;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import jade.proto.SubscriptionInitiator;
import jade.proto.SubscriptionResponder;
import jade.proto.SimpleAchieveREResponder;
import jade.domain.FIPANames;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class represents the Electronic Institution's normative environment.
 * <br>
 * The normative environment contains a set of norms. The norms and the mechanisms to monitor and enforce them are
 * implemented using Jess. Contracts are also represented in a declarative fashion, including contractual norms that are
 * monitored by the normative environment.
 * <p>
 * The normative environment agent implements the following interaction protocol roles:
 * <ul>
 * <li>the DF-subscription initiator role
 * <li>the ne-management responder role
 * <li>the ne-delegation responder role
 * <li>the ne-report subscription responder role
 * </ul>
 * The agent also listens to inform messages of the ILLOCUTION_ONTOLOGY.
 * 
 * @author hlc
 */
public class NormativeEnvironment extends PlatformService {
	
	private static final long serialVersionUID = -6413660697023765596L;
	
	private String xsd_file;
	
	/**
	 * The normative environment main behaviour for running the Jess engine.
	 */
	protected NormEnvBehaviour normEnvBeh;
	
	// the reports regarding each contract (contract id --> reports)
	protected Hashtable<String, java.util.ArrayList<Report>> reportsByContract = new Hashtable<String, java.util.ArrayList<Report>>();
	
	// existing contracts for each type of contract (type --> contract id's)
	protected Hashtable<String, java.util.ArrayList<String>> contractsByType = new Hashtable<String, java.util.ArrayList<String>>();

	// the normative environment subscription responder manager
	private NormEnvSubscriptionResp normEnvSubscriptionResp;
	
	private NormEnvJessGUI normEnvJessGUI = null;	
	
	/**
	 * Behaviour for subscription with the DF in order to get information on newly registered agents;
	 * needed as global to cancel the subscription on takeDown().
	 */
	protected jade.proto.SubscriptionInitiator subscDF;
	
	/**
	 * Set up of the EI agent.
	 */
	protected void setup() {
		// the setup method at the AgentifiedService class handles registration with the DF and prepares the arguments
		super.setup();
		
		// register ontologies
		getContentManager().registerOntology(NEManagementOntology.getInstance());
		getContentManager().registerOntology(NEReportOntology.getInstance());
		getContentManager().registerOntology(IllocutionOntology.getInstance());
		
		// get the xsd_file argument
   		xsd_file = getConfigurationArguments().getProperty("contract_xsd");
   		
		//set the gui
		createGUI();
		
		// create the normative environment behaviour
		// check if a Jess filename was given as an argument to the agent
		String jessFile = getConfigurationArguments().getProperty("jess_file");
		// was a Jess filename found?
		if(jessFile != null) {
			normEnvBeh = new NormEnvBehaviour(this, jessFile, normEnvJessGUI);
		} else {
			normEnvBeh = new NormEnvBehaviour(this, normEnvJessGUI);
		}
		
		if(isInSynchronizedExperiment()) {
			normEnvBeh.setExecutionMode("sync-experiment");
		}
		addBehaviour(normEnvBeh);
		
		// responder role for NE_MANAGEMENT_ONTOLOGY
		addBehaviour(new NEManagementResp(this));
		
		// responder role for NE_DELEGATION_ONTOLOGY
		addBehaviour(new NEDelegationResp(this));
		
		// listening for inform messages of the ILLOCUTION_ONTOLOGY
		addBehaviour(new IllocutionListening());
		
		// subscription responder role for NE_REPORT_ONTOLOGY
		normEnvSubscriptionResp = new NormEnvSubscriptionResp(this);
		addBehaviour(normEnvSubscriptionResp);
		
		// subscribe the DF for updates on new agents/services
		subscDF = new DFSubscriptionInit(this);
		addBehaviour(subscDF);
	}
	
	/**
	 * When terminating, unsubscribes from the DF and saves the normative environment state.
	 */
	protected void takeDown() {
		// the takeDown method at the AgentifiedService class handles deregistration from the DF
		super.takeDown();
		// unsubscribe the DF
		subscDF.cancel(getDefaultDF(), true);
		// save the normative environment state (Jess engine)
		normEnvBeh.saveEnvState();
	}
	
	/**
	 * Handles a new report from the Jess engine. The report will be stored and interested parties will be notified.
	 * 
	 * @param rep	The report to notify
	 */
	protected void newReport(Report rep) {
		// store report in reportsByContract
		if(rep.getContext() != null) {   // probably will always be true...
			java.util.ArrayList<Report> reports = reportsByContract.get(rep.getContext());
			if(reports == null) {   // first report on this contract?
				reports = new java.util.ArrayList<Report>();
			}
			reports.add(rep);
			reportsByContract.put(rep.getContext(), reports);
		}

		// new contract report?
		if(rep instanceof NewContract) {
			// store new contract id
			java.util.ArrayList<String> contracts = contractsByType.get(((NewContract) rep).getType());
			if(contracts == null) {   // first contract of its type?
				contracts = new java.util.ArrayList<String>();
			}
			contracts.add(rep.getContext());
			contractsByType.put(((NewContract) rep).getType(), contracts);
			
			// send inform messages to involved agents
			List agents = ((NewContract) rep).getAgents();
			ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
			inform.setLanguage(getLeapCodec().getName());
			inform.setOntology(ElectronicInstitution.NE_REPORT_ONTOLOGY);
			try {
				getContentManager().fillContent(inform, rep);
			} catch(Codec.CodecException ce) {
				ce.printStackTrace();
			} catch(OntologyException oe) {
				oe.printStackTrace();
			}
			for(int i=0; i<agents.size(); i++) {
				inform.addReceiver(new AID((String) agents.get(i), false));
			}
			send(inform);
		} else {
			// if in synchronized-experiment mode, send inform messages to involved agents (no subscriptions needed)
			if(isInSynchronizedExperiment()) {
				List agents = ((NewContract) reportsByContract.get(rep.getContext()).get(0)).getAgents();
				ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
				inform.setLanguage(getLeapCodec().getName());
				inform.setOntology(ElectronicInstitution.NE_REPORT_ONTOLOGY);
				try {
					getContentManager().fillContent(inform, rep);
				} catch(Codec.CodecException ce) {
					ce.printStackTrace();
				} catch(OntologyException oe) {
					oe.printStackTrace();
				}
				for(int i=0; i<agents.size(); i++) {
					inform.addReceiver(new AID((String) agents.get(i), false));
				}
				send(inform);
			}
		}

		// notify interested agents
		normEnvSubscriptionResp.notify(rep);
		
		// update gui
		if(getGUI() != null) {
			if(rep instanceof ContractEnd) {
				((NormativeEnvironmentGUI) getGUI()).new DoUpdateValues(0.8);
			}
		}
	}
	
	
	/**
	 * Inner class to implement the ne-report subscription responder role.
	 * <br>
	 * This subscription facility should be used with the LEAP language (LEAPCodec) because the SL language does not properly support
	 * optional slots.
	 * 
	 * @author hlc
	 */
	private class NormEnvSubscriptionResp extends SubscriptionResponder {
		
		private static final long serialVersionUID = 1419793260299440645L;
		
		NormEnvSubscriptionResp(Agent a) {
			super(a, MessageTemplate.and(
										MessageTemplate.or(	MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),
															MessageTemplate.MatchPerformative(ACLMessage.CANCEL)),
										MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE),
															MessageTemplate.MatchOntology(ElectronicInstitution.NE_REPORT_ONTOLOGY))));
		}
		
		protected ACLMessage handleCancel(ACLMessage cancel) {
			//System.out.println("*****************"+cancel.getSender().getLocalName());
			try {
				return super.handleCancel(cancel);
			} catch (FailureException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		/**
		 * When handling a new subscription request, the permissions of the subscriber concerning the subscribed content is checked.
		 * Namely, in general agents are only allowed to get reports regarding their own contracts. An exception is the reputation
		 * agent, which may subscribe to any content.
		 * If a contract id is provided in the subscription report template, this method also sends previous reports regarding that
		 * contract to the subscribing agent.
		 */
		protected ACLMessage handleSubscription(ACLMessage subscription_msg) {
			// get subscription template
			Report subscription_template = null;
			try {
				// extract content
				ContentElement ce = myAgent.getContentManager().extractContent(subscription_msg);
				if(ce instanceof Report)
					// return the reports' context
					subscription_template = (Report) ce;
			} catch(Codec.CodecException cex) {
				cex.printStackTrace();
			} catch(OntologyException oe) {
				oe.printStackTrace();
			}
			
			// prepare reply
			ACLMessage reply = subscription_msg.createReply();
			
			if(subscription_template == null) {
				// reply with NOT_UNDERSTOOD
				reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
				return reply;
			}
			
			// check permissions
			if(!subscriptionAllowed(subscription_msg.getSender(), subscription_template)) {
				// reply with REFUSE
				reply.setPerformative(ACLMessage.REFUSE);
				return reply;
			}
			
			// if subscription is ok, create it
			Subscription sub = createSubscription(subscription_msg);
			
			// print das subscrições efectuadas
			//System.out.println("~~>" + subscription_msg.getSender().getLocalName() + " subscreveu " + subscription_template.getContext());
			
			// if a contract id is included, send all previous reports about that contract to the subscribing agent
			String contract_id = subscription_template.getContext();
			if(contract_id != null) {
				java.util.ArrayList<Report> reports = reportsByContract.get(contract_id);
				if(reports != null)
					for(int i=0; i<reports.size(); i++) {
						notifyIfInterested(reports.get(i), sub);
					}
			}
			
			// reply with AGREE
			//reply.setPerformative(ACLMessage.AGREE);
			//return reply;
			return null;
		}
		
		/**
		 * Checks if a subscription template is allowed for an agent. That is, checks if the agent is authorized to listen for all
		 * reports matching the template.
		 * 
		 * FIXME: maybe the subscription permissions could be coded declaratively in Jess...
		 * 
		 * @param agent					The subscribing agent
		 * @param subscription_template	The subscription template
		 * @return						<code>true</code> if allowed, <code>false</code> otherwise
		 */
		private boolean subscriptionAllowed(AID agent, Report subscription_template) {
			// if reputation agent, allow any subscription
			if(providesService(agent, ElectronicInstitution.SRV_CTR))
				return true;
			
			// if agent is part of the indicated contract, allow
			String contract_id = subscription_template.getContext();
			if(contract_id != null) {
				java.util.ArrayList<String> contractual_agents = normEnvBeh.getContractualAgents(contract_id);
				if(contractual_agents!= null && contractual_agents.contains(agent.getLocalName()))
					return true;
			}
			
			// TODO - add more allowed subscription cases
			// CHECK: if I do not specify a contract, can I get events from any contract?!
			
			// if none of the above applies, subscription is not allowed
			return false;
		}
		
		/**
		 * Notifies the subscriptions that subscribe for reports such as the parameter.
		 * <br>
		 * Each subscription includes, in its ACLMessage content, a template for report predicates.
		 * That is, an instance of a subclass of Report where some of its fields might be undefined, and therefore can match any value
		 * in the report parameter's fields.
		 * 
		 * @param rep	The report to notify
		 */
		protected void notify(Report rep) {
			// go through every subscription
			Vector subs = getSubscriptions();
			
			for(int i=0; i<subs.size(); i++) {
				// notify if this subscription is interested in this notification
				notifyIfInterested(rep, (SubscriptionResponder.Subscription) subs.elementAt(i));
			}
		}
		
		/**
		 * Notifies a subscription if it is interested in the report.
		 * 
		 * @param rep	The report
		 * @param sub	The subscription
		 */
		private void notifyIfInterested(Report rep, SubscriptionResponder.Subscription sub) {
			ACLMessage subscription_message = sub.getMessage();
			// get subscription template
			Report subscription_template = null;
			try {
				// extract content
				ContentElement ce = myAgent.getContentManager().extractContent(subscription_message);
				if(ce instanceof Report)
					subscription_template = (Report) ce;
			} catch(Codec.CodecException cex) {
				cex.printStackTrace();
			} catch(OntologyException oe) {
				oe.printStackTrace();
			}
			
			// compare the predicate's class with the subscription_template's class
			if(subClassOf(rep.getClass(), subscription_template.getClass())) {
				// check if template restrictions match object contents
				if(contentsMatch(rep, subscription_template)) {
					ACLMessage notification = subscription_message.createReply();
					notification.addUserDefinedParameter(ACLMessage.IGNORE_FAILURE, "true");
					notification.setPerformative(ACLMessage.INFORM);
					try {
						myAgent.getContentManager().fillContent(notification, rep);
					} catch(Codec.CodecException ce) {
						ce.printStackTrace();
					} catch(OntologyException oe) {
						oe.printStackTrace();
					}
					sub.notify(notification);
				}
			}
		}
		
		/**
		 * Checks if the contents of a predicate match the contents of a predicate template. The template can have getter methods
		 * that return <code>null</code>. Every getter method of the predicate is invoked on the template. If the getter method is
		 * defined for the template and a non-null value is returned, the return value obtained by invoking the method on the
		 * predicate must be the same (via an <code>equals()</code> comparison).
		 * <br>
		 * Note that this method does not check if a relation exists between the predicate and the template: subClassOf() should be
		 * invoked before with the classes of both the predicate and the template.
		 * 
		 * @param p			The predicate
		 * @param template	The predicate template
		 * @return			<code>true</code> if the predicate matches the template, <code>false</code> otherwise
		 */
		private boolean contentsMatch(Predicate p, Predicate template) {
			Method[] methods = p.getClass().getMethods();
			for(int i=0; i<methods.length; i++) {
				if(methods[i].getName().startsWith("get") && !methods[i].getName().equals("getClass")) {
															// FIXME: insufficient if super-classes have further "get" methods
															// (in that case maybe a general solution is not possible)
					try {
						Object ret = methods[i].invoke(template);
						if(ret != null && !ret.equals(methods[i].invoke(p)))
										// FIXME: if a getter returns a List I should check if the template list is a sublist of the predicate list
							return false;
					} catch(Exception e) {
					}
				}
			}
			return true;
		}
		
		/**
		 * Checks if a Class is a subclass of another Class. Equal classes are considered subclasses of one another.
		 * 
		 * @param sub	The sub-class
		 * @param sup	The super-class
		 * @return		<code>true</code> if sub is a subclass of or equal to sup, <code>false</code> otherwise 
		 */
		private boolean subClassOf(Class<?> sub, Class<?> sup) {
			while(sub != null && sub != sup)
				sub = sub.getSuperclass();
			return sub == sup;
		}
		
	} // end NormEnvSubscriptionResp class
	
	
	/**
	 * Inner class to implement the DF-subscription initiator role.
	 * The normative environment subscribes the DF for updates on existing agents and their services (roles).
	 * 
	 * @author hlc
	 */
	private class DFSubscriptionInit extends SubscriptionInitiator {
		
		private static final long serialVersionUID = 6944274811217174827L;
		
		/**
		 * Creates a <code>SubscribeDFInit</code> instance
		 *
		 * @param agent The agent that adds the behaviour.
		 */
		DFSubscriptionInit(Agent agent) {
			super(agent, DFService.createSubscriptionMessage(agent, getDefaultDF(), new DFAgentDescription(), null));
		}
		
		/**
		 * Handles an inform message from the DF.
		 */
		protected void handleInform(ACLMessage inform) {
			// decode the message
			try {
				DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
				// inform the normative environment
				normEnvBeh.newAgents(dfds);
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
		
	} // end DFSubscriptionInit class
	
	
	/**
	 * Inner class to implement the ne-management responder role.
	 * 
	 * TODO: add the possibility of retrieving the norms applicable to a specific contract (at all contexts from the contract's to
	 *		 INSTITUTIONAL-NORMS, since we only know which norms are actually applied at run-time).
	 * 
	 * @author hlc
	 */
	private class NEManagementResp extends SimpleAchieveREResponder {
		
		private static final long serialVersionUID = 9045748436076436037L;
		
		// action received in the request message
		AgentAction request_action;
		
		/**
		 * Creates a <code>NEManagementResp</code> instance
		 *
		 * @param agent	The agent that adds the behaviour.
		 */
		NEManagementResp(Agent agent) {
			super(agent, MessageTemplate.and(
//					MessageTemplate.or(
							SimpleAchieveREResponder.createMessageTemplate(FIPA_REQUEST),
//							SimpleAchieveREResponder.createMessageTemplate(FIPA_QUERY)),
							// not using FIPA_QUERY: actions start with "send-"
					MessageTemplate.MatchOntology(ElectronicInstitution.NE_MANAGEMENT_ONTOLOGY)));
		}
		
		/**
		 * Prepares an appropriate response.
		 */
		protected ACLMessage prepareResponse(ACLMessage request) {
			ACLMessage reply = request.createReply();
			request_action = null;
			
			// check requested action
			try {
				// extract content
				ContentElement ce = myAgent.getContentManager().extractContent(request);
				if(ce instanceof Action)
					request_action = (AgentAction) ((Action) ce).getAction();
			} catch(Codec.CodecException ce) {
				ce.printStackTrace();
			} catch(OntologyException oe) {
				oe.printStackTrace();
			}
			
			if(request_action instanceof SendContractTypes || request_action instanceof SendApplicableNorms /* || request_action instanceof ... || ... */) {
				// if requested action is known, agree
				//reply.setPerformative(ACLMessage.AGREE);
				//return reply;
				return null;
			} else {
				// if not, reply with not-understood
				reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
				return reply;
			}
		}
		
		/**
		 * Prepares an appropriate reply.
		 */
		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
			ACLMessage reply = request.createReply();
			
			// check requested action
			if(request_action instanceof SendContractTypes) {
				// send the available predefined contract types
				reply.setPerformative(ACLMessage.INFORM);
				ContractTypes cTs = new ContractTypes();
				cTs.setTypes(new ArrayList(normEnvBeh.getPredefinedContractTypes()));
				try {
					myAgent.getContentManager().fillContent(reply, cTs);
				} catch(Codec.CodecException ce) {
					ce.printStackTrace();
				} catch(OntologyException oe) {
					oe.printStackTrace();
				}
			} else if(request_action instanceof SendApplicableNorms) {
				// send the applicable norms concerning a specific contract type
				reply.setPerformative(ACLMessage.INFORM);
				ApplicableNorms aNs = new ApplicableNorms();
				aNs.setNorms(new ArrayList(normEnvBeh.getApplicableNorms(((SendApplicableNorms) request_action).getContractType())));
				try {
					myAgent.getContentManager().fillContent(reply, aNs);
				} catch(Codec.CodecException ce) {
					ce.printStackTrace();
				} catch(OntologyException oe) {
					oe.printStackTrace();
				}
			}
			// return the created reply
			return reply;
		}
		
	} // end NEManagementResp class
	
	
	/**
	 * Inner class to implement the illocution listening role.
	 * Receives inform messages of the illocution ontology.
	 * 
	 * @author hlc
	 */
	private class IllocutionListening extends CyclicBehaviour {
		
		private static final long serialVersionUID = 2130794273613266404L;
		
		private MessageTemplate mt =
			MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchOntology(ElectronicInstitution.ILLOCUTION_ONTOLOGY));
		
		public void action() {
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// inform the normative environment; *for now*, don't care what it returns (TODO?)
				normEnvBeh.newIllocution(msg);
			} else
				block();
		}
	} // end IllocutionListening class
	
	
	/**
	 * Inner class to implement the ne-delegation responder role.
	 * The normative environment will accept contracts to monitor requested by a notary service.
	 * 
	 * @author hlc
	 */
	private class NEDelegationResp extends SimpleAchieveREResponder {
		private static final long serialVersionUID = 7405618360737605794L;
		
		/**
		 * Creates a <code>NEDelegationResp</code> instance
		 *
		 * @param agent	The agent that adds the behaviour.
		 */
		NEDelegationResp(Agent agent) {
			super(agent, MessageTemplate.and(	SimpleAchieveREResponder.createMessageTemplate(FIPA_REQUEST),
												MessageTemplate.MatchOntology(ElectronicInstitution.NE_DELEGATION_ONTOLOGY)));
		}
		
		/**
		 * Prepares an appropriate response, based on the sender role.
		 */
		protected ACLMessage prepareResponse(ACLMessage request) {
			ACLMessage reply = request.createReply();

			if(!isInSynchronizedExperiment()) {
				// check if the sender is a notary service
				if(providesService(request.getSender(), ElectronicInstitution.SRV_NOTARY)) {
					// if sender is a notary, agree
					//reply.setPerformative(ACLMessage.AGREE);
					return null;
				} else {
					// if not, refuse
					reply.setPerformative(ACLMessage.REFUSE);
					return reply;
				}
			} else {
				// synchronized experiment: accept any delegation
				return null;
			}
		}
		
		/**
		 * Prepares an appropriate reply.
		 */
		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
			ACLMessage reply = request.createReply();
			
			// get contract from content
			ContractWrapper cw = new ContractWrapper(((NormativeEnvironment) myAgent).xsd_file);
			cw.unmarshal(request.getContent(), true);
			
			// parse contract and insert into jess
			if(normEnvBeh.newContract(cw)) {
				// if ok, send inform message
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent("will monitor");
			} else {
				// if not ok, send failure message
				reply.setPerformative(ACLMessage.FAILURE);
				reply.setContent("invalid contract");
			}
			
			// return the created reply
			return reply;
		}
		
	} // end NEDelegationResp class
	
	
	protected boolean createGUI() {
		gui = new NormativeEnvironmentGUI(this);
		
		normEnvJessGUI = new NormEnvJessGUI(getGUI());
		return true;
	}

	protected NormEnvJessGUI getNormEnvJessGui() {
		return normEnvJessGUI;
	}
	
} // end NormativeEnvironment class
