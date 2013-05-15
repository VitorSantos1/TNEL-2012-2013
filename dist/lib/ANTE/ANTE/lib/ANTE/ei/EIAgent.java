package ei;

import ei.ElectronicInstitution; 
import ei.onto.management.*;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SimpleAchieveREResponder;
import jade.content.AgentAction;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.leap.LEAPCodec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;

import java.awt.Frame;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

/**
 * This is the base class for JADE agents in the Electronic Institution Platform.
 * <p>
 * Subclasses should override the setup() method to create agent behaviour(s), and invoke <code>super.setup()</code>.
 * <p>
 * The <code>EIAgent</code> class includes a set of methods that are helpful for registering/deregistering the agent
 * and its services with the DF.
 * <p>
 * The EIAgent implements the following interaction protocol roles:
 * <ul>
 * <li>ei-management responder role
 * </ul>
 * 
 * @author hlc
 */
public abstract class EIAgent extends Agent {
	private static final long serialVersionUID = -87736987810913178L;

	/**
	 * The configuration arguments provided to the agent.
	 */
	private Properties configurationArguments;

	public Properties getConfigurationArguments() {
		return configurationArguments;
	}
	
	/**
	 * Flag indicating if the agent is currently registered with the DF or not.
	 */
	protected boolean isRegisteredWithDF = false;

	/**
	 * This agent's GUI.
	 */
	protected Frame gui = null;
	
	// public key for this agent
	private PublicKey publicKey;
	// private key for this agent
	private PrivateKey privateKey;
	
	private Codec leapCodec = new LEAPCodec();
	private Codec slCodec = new SLCodec();
	
	private boolean logging = false;
	private boolean inSynchronizedExperiment = false;
	private String synchronizer = null;
	
	// acquainted institutional agents (1 per service)
	private HashMap<String, AID> acquaintedAgents = new HashMap<String, AID>();   // DF service --> agent
	
	/**
	 * This setup method:
	 * <ul>
	 * <li> iterates through the arguments provided to the agent to build the <code>Properties</code> object accordingly
	 * <li> registers the SL and Leap languages
	 * <li> registers the ei-management ontology
	 * <li> generates the public/private key pair for this agent
	 * <li> creates a responder behaviour for the EI_MANAGEMENT_ONTOLOGY
	 * </ul>
	 * When extending this class, override this method to create agent behaviour(s) and invoke <code>super.setup()</code>.
	 */
	protected void setup() {
		
		// prepare the arguments Properties
		configurationArguments = new Properties();
		Object[] args = getArguments();
		if(args != null) {
			// iterate through the arguments
			for(int i=0; i<args.length; i++) {
				String arg = args[i].toString();
				// set this argument as a property
				configurationArguments.setProperty(arg.substring(0, arg.indexOf('=')), arg.substring(arg.indexOf('=')+1));
			}
			
			// set the logging attribute, if defined
			if(configurationArguments.containsKey("logging")) {
				setLogging(Boolean.parseBoolean(configurationArguments.getProperty("logging")));
			}
			
	   		// in synchronized experiment?
			if(configurationArguments.containsKey("synchronizer")) {
				setSynchronizer((String) configurationArguments.get("synchronizer"));
				setInSynchronizedExperiment(true);
			} else {
				setInSynchronizedExperiment(false);
			}
		}
		
		// register languages
		getContentManager().registerLanguage(leapCodec);
		getContentManager().registerLanguage(slCodec);
		// register ontologies
		getContentManager().registerOntology(EIManagementOntology.getInstance());
		
		// generate private and public keys
		publicKey = null;
		privateKey = null;
		try {
			// Generate a 1024-bit Digital Signature Algorithm (DSA) key pair
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
			keyGen.initialize(1024);
			KeyPair keypair = keyGen.genKeyPair();
			privateKey = keypair.getPrivate();
			publicKey = keypair.getPublic();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		// responder role for EI_MANAGEMENT_ONTOLOGY
		addBehaviour(new EIManagementResp(this));
	}
	
	/**
	 * When terminating, deregisters the agent from the DF if it is registered.
	 */
	protected void takeDown() {
		// deregister from the DF
		deregisterAgentFromDF();
	}
	
	/**
	 * Registers the agent at the DF.
	 */
	protected void registerAgentAtDF() {
		// check if the agent is not already registered
		if(!isRegisteredWithDF) {
			// prepare agent description
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			
			try {
				// register
				DFService.register(this, dfd);
				// update registration flag
				isRegisteredWithDF = true;
			} catch(FIPAException fe){
				fe.printStackTrace();
			}
		}
	}
	
	/**
	 * Registers the agent at the DF with the provided service.
	 */
	protected void registerAgentAtDF(ServiceDescription sd) {
		// check if the agent is not already registered
		if(!isRegisteredWithDF) {
			// prepare agent description
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			
			// add the service
			dfd.addServices(sd);
			
			try {
				// register
				DFService.register(this, dfd);
				// update registration flag
				isRegisteredWithDF = true;
			} catch(FIPAException fe){
				fe.printStackTrace();
			}
		}
	}
	
	/**
	 * Registers the agent at the DF with the provided services.
	 */
	protected void registerAgentAtDF(Vector<ServiceDescription> sds) {
		// check if the agent is not already registered
		if(!isRegisteredWithDF) {
			// prepare agent description
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			
			if(sds != null) {
				// add the services
				for(int i=0; i<sds.size(); i++) {
					dfd.addServices(sds.get(i));
				}
			}
			
			try {
				// register
				DFService.register(this, dfd);
				// update registration flag
				isRegisteredWithDF = true;
			} catch(FIPAException fe){
				fe.printStackTrace();
			}
		}
	}
	
	/**
	 * Unregisters the agent from the DF.
	 */
	protected void deregisterAgentFromDF() {
		// check if the agent is registered
		if(isRegisteredWithDF) {
			try {
				// deregister
				DFService.deregister(this);
			} catch(FIPAException fe) {
				fe.printStackTrace();
			}
			// update registration flag
			isRegisteredWithDF = false;
		}
	}
	
	/**
	 * Registers a service for the agent at the DF. Note that previously registered services at the DF for this agent will be removed.
	 * In case this agent is not already registered at the DF, it will be registered with the provided service.
	 */
	protected void registerServiceAtDF(ServiceDescription sd) {
		// check if the agent is registered
		if(!isRegisteredWithDF) {
			registerAgentAtDF(sd);
			return;
		}
		
		// prepare the agent and service description
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		// add the service
		dfd.addServices(sd);
		try {
			// register the service
			DFService.modify(this, dfd);
		} catch(FIPAException fe){
			fe.printStackTrace();
		}
	}
	
	/**
	 * Registers services for the agent at the DF. Note that previously registered services at the DF for this agent will be removed.
	 * In case this agent is not already registered at the DF, it will be registered with the provided services.
	 */
	protected void registerServicesAtDF(Vector<ServiceDescription> sds) {
		// check if the agent is registered
		if(!isRegisteredWithDF) {
			registerAgentAtDF(sds);
			return;
		}
		
		// prepare the agent and service description
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		// add the services
		for(int i=0; i<sds.size(); i++) {
			dfd.addServices(sds.get(i));
		}
		try {
			// register the services
			DFService.modify(this, dfd);
		} catch(FIPAException fe){
			fe.printStackTrace();
		}
	}
	
	public Codec getLeapCodec() {
		return leapCodec;
	}

	public Codec getSlCodec() {
		return slCodec;
	}

	/**
	 * Removes an agent from the acquainted list.
	 * 
	 * @param service
	 * @param aid
	 */
	public void forgetAgent(String service, AID aid) {
		acquaintedAgents.remove(service);
	}
	
	/**
	 * Gets an agent providing the requested service.
	 * If one such agent is already acquainted, returns it. Otherwise, or if the forceDF flag is set, searches the DF.
	 * Note that service here is a DF term, not necessarily an institutional service.
	 * 
	 * @param service	The service to search for
	 * @param forceDF	A flag indicating if a DF search should be always executed or only if necessary
	 * @return			The AID of an agent providing the requested service
	 */
	public AID fetchAgent(String service, boolean forceDF) {
		AID acquainted;
		if(!forceDF) {
			acquainted = acquaintedAgents.get(service);
			if(acquainted != null) {
				return acquainted;
			}
		} // no else here!
		// if either there is no known agent or forceDF is activated, fetch an agent through the DF
		acquainted = fetchAgent(service);
		acquaintedAgents.put(service, acquainted);
		return acquainted;
	}
	
	/**
	 * Gets an agent providing the requested service. If more than one is available, returns the first one.
	 * Note that service here is a DF term, not necessarily an institutional service.
	 * 
	 * @param service	The service to be discovered
	 * @return			The AID for the agent providing the service
	 */
	public AID fetchAgent(String service) {
		DFAgentDescription[] dfads = fetchAgents(service);
		if(dfads.length == 0) {
			return null;
		} else {
        	Random random = new Random();
            int x = random.nextInt(dfads.length);
			//System.out.println(x + " - " + dfads.length + " --> suggesting " + dfads[x].getName().getLocalName() + " as " + service + " to " + ag.getLocalName());
			return dfads[x].getName();
		}
	}
	
	/**
	 * Searches the DF for agents providing the requested service, as registered at the DF.
	 * Note that service here is a DF term, not necessarily an institutional service.
	 * 
	 * @param service	The service to be discovered
	 * @return			An array with all the DFAgentDescriptions obtained from the DF
	 */
	public DFAgentDescription[] fetchAgents(String service) {
		// prepare the template
		DFAgentDescription template = new DFAgentDescription();
		// add the intended institutional service
		ServiceDescription sd = new ServiceDescription();
		sd.setType(service);
		template.addServices(sd);
		// get all agents providing the intended service
		try {
			return DFService.search(this, template);
		} catch(FIPAException fe) {
			fe.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Checks if a specific agent provides a specific service.
	 * 
	 * @param ag	The requester, needed for DFService
	 * @param agent	The agent to check the service for
	 * @param srv	The service
	 * @return		<code>true</code> if the agent provides the service; <code>false</code> otherwise
	 */
	public boolean providesService(AID agent, String srv) {
		// prepare the template
		DFAgentDescription template = new DFAgentDescription();
		template.setName(agent);
		// get the agent record
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			if(result.length == 0)
				return false;
			// iterate through the services of the agent
			Iterator it = result[0].getAllServices();
			while(it.hasNext()) {
				if( ( (ServiceDescription) it.next()).getType().equals(srv) )
					return true;
			}
		} catch(FIPAException fe) {
			fe.printStackTrace();
		}		
		return false;
	}
	
	/**
	 * Gets this agent's private key.
	 * 
	 * @return	The private key
	 */
	public PrivateKey getPrivateKey() {
		return privateKey;
	}
	
	/**
	 * Gets this agent's public key.
	 * 
	 * @return	The public key
	 */
	protected PublicKey getPublicKey() {
		return publicKey;
	}
	
	public Frame getGUI() {
		return gui;
	}

	/**
	 * Show the agent gui.
	 * @return	<code>true</code> if ok; <code>false</code> if the GUI object is null
	 */
	protected boolean showGUI() {
		if(gui == null && !createGUI()) {
			return false;
		} else {
			gui.setVisible(true);
			return true;
		}
	}
	
	protected abstract boolean createGUI();
	
	/**
	 * Retrieve an agent's public-key.
	 * @param id	The AID of the agent to get the public-key from
	 * @return		The agent's public key
	 * @throws Exception
	 */
	protected PublicKey retrievePublicKey(AID id) {
		// request the public-key to the agent
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		request.setOntology(ElectronicInstitution.EI_MANAGEMENT_ONTOLOGY);
		request.setLanguage(slCodec.getName());
		request.addReceiver(id);
		// add the send-public-key command as content of the request to be sent
		SendPublicKey spk = new SendPublicKey();
		Action act = new Action(id, spk);
		try {
			getContentManager().fillContent(request, act);
		} catch(OntologyException oe) {
			oe.printStackTrace();
		} catch(Codec.CodecException ce) {
			ce.printStackTrace();
		}
		// send message and get reply - TODO this way of making a FIPA-Request is discouraged by JADE...
		try {
			ACLMessage reply = jade.domain.FIPAService.doFipaRequestClient(this, request, 10000);
			if(reply.getPerformative() == ACLMessage.INFORM)
				try {
					publicKey = (PublicKey) reply.getContentObject();
					return publicKey;
				} catch(UnreadableException ue) {
					ue.printStackTrace();
				}
		} catch(FIPAException fe) {
		}
		return null;
	}

	/**
	 * Sets if the system is running in synchronized experiment mode.
	 */
	public void setInSynchronizedExperiment(boolean inSynchronizedExperiment) {
		this.inSynchronizedExperiment = inSynchronizedExperiment;
	}

	/**
	 * Determines if the system is running in synchronized experiment mode.
	 */
	public boolean isInSynchronizedExperiment() {
		return inSynchronizedExperiment;
	}

	public void setSynchronizer(String synchronizer) {
		this.synchronizer = synchronizer;
	}

	public String getSynchronizer() {
		return synchronizer;
	}

	/**
	 * Sets the logging mode.
	 * @param logging
	 */
	protected void setLogging(boolean logging) {
		this.logging = logging;
	}
	
	/**
	 * If logging is enabled, prints a log message at <code>System.out</code>.
	 * Adds the name of the agent to the beginning of the message.
	 * 
	 * @param msg	The message to print
	 */
	public void log(String msg) {
		if(logging)
			System.out.println(getLocalName() + ": " + msg);
	}
	
	/**
	 * Prints a log message at <code>System.err</code>.
	 * Adds the name of the agent to the beginning of the message.
	 * 
	 * @param msg	The message to print
	 */
	public void logErr(String msg) {
		System.err.println(getLocalName() + ": " + msg);
	}
	
	
	/**
	 * Inner class to implement the ei-management responder role.
	 */
	private class EIManagementResp extends SimpleAchieveREResponder {
		
		private static final long serialVersionUID = -7790013180220558709L;
		
		// action received in the request message
		AgentAction request_action;
		
		/**
		 * Creates an <code>EIManagementResp</code> instance
		 *
		 * @param agent	The agent that adds the behaviour.
		 */
		EIManagementResp(Agent agent) {
			super(agent, MessageTemplate.and(
//					MessageTemplate.or(
							SimpleAchieveREResponder.createMessageTemplate(FIPA_REQUEST),
//							SimpleAchieveREResponder.createMessageTemplate(FIPA_QUERY)),
							// not using FIPA_QUERY: actions start with "send-"
					MessageTemplate.MatchOntology(ElectronicInstitution.EI_MANAGEMENT_ONTOLOGY)));
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
				ContentElement ce = getContentManager().extractContent(request);
				if(ce instanceof Action)
					request_action = (AgentAction) ((Action) ce).getAction();
			} catch(Codec.CodecException cex){
				cex.printStackTrace();
			} catch(OntologyException oe){
				oe.printStackTrace();
			}
			
			if(request_action instanceof SendPublicKey || request_action instanceof ShowGui /* || request_action instanceof ... || ... */) {
				// if requested action is known, agree
				reply.setPerformative(ACLMessage.AGREE);
			} else {
				// if not, reply with not-understood
				reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			}
			
			// return the created reply
			return reply;
		}
		
		/**
		 * Prepares an appropriate reply.
		 */
		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
			ACLMessage reply = request.createReply();
			
			// check requested action
			if(request_action instanceof SendPublicKey) {
				// send the agent's public key
				try {
					reply.setContentObject(((EIAgent) myAgent).getPublicKey());
					reply.setPerformative(ACLMessage.INFORM);
				} catch(IOException ue) {
					reply.setPerformative(ACLMessage.FAILURE);
				}
			} else if(request_action instanceof ShowGui) {
				// show the agent's gui
				if(((EIAgent) myAgent).showGUI()) {
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent("done");
				} else {
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("error showing gui");
				}
			}
			
			// return the created reply
			return reply;
		}
		
	} // end EIManagementResp class
	
}
