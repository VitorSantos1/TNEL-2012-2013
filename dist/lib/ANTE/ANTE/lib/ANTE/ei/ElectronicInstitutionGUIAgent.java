package ei;

import javax.swing.JFrame;

import ei.onto.management.ShowGui;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;
import jade.proto.SubscriptionInitiator;
import jade.wrapper.ControllerException;
import jade.domain.FIPANames;
import jade.content.onto.basic.Action;

/**
 * Agent allowing the interface to communicate with other agents.
 */
public class ElectronicInstitutionGUIAgent extends EIAgent {
	
	private static final long serialVersionUID = 8168833476115890159L;
	
	/**
	 * The Electronic Institution GUI
	 */
	protected ElectronicInstitutionGUI eiGUI;
	
	/**
	 * Behaviour for subscription with the DF in order to get information on newly registered agents;
	 * needed as global to cancel the subscription on takeDown().
	 */
	private SubscribeDFInit subscDF;
	
	private Codec slCodec = new SLCodec();
	
	/**
	 * Constructor.
	 */
	protected ElectronicInstitutionGUIAgent() {
		super();
		JFrame.setDefaultLookAndFeelDecorated(true);		
		// create the ElectronicInstitution gui
		eiGUI = new ElectronicInstitutionGUI(this);
		eiGUI.setVisible(true);
	}
	
	/**
	 * Set up of the GUI2JadeAgent.
	 */
	protected void setup() {
		super.setup();
		
		// subscribe the DF for updates on new agents/services
		subscDF = new SubscribeDFInit(this);
		addBehaviour(subscDF);
	}
	
	/**
	 * Shuts down the Electronic Institution Platform.
	 */
	protected void shutDownPlatform() {
		try {
//			RequestFIPAServiceBehaviour sdb = AMSService.getNonBlockingBehaviour(this, actionName);
//			addBehaviour(sdb);
			this.getContainerController().getPlatformController().kill();
			/**/System.exit(0);
		} catch(ControllerException ce) {
			ce.printStackTrace();
		}
	}
	
	/**
	 * Asks an agent to show his GUI.
	 */
	protected void askShowGUI(String agent) {
		addBehaviour(new EIManagementInit_ShowGUI(this, new AID(agent, false)));
	}
	
	/**
	 * When terminating, unsubscribes from the DF.
	 */
	protected void takeDown() {
		// unsubscribe the DF
		subscDF.cancel(getDefaultDF(), true);
	}
	
	/**
	 * Inner class that allows the ElectronicInstitutionGuiAgent to subscribe the DF for updates on existing agents and their services.
	 * <br>
	 * Implemented as a subscription initiator role.
	 */
	protected class SubscribeDFInit extends SubscriptionInitiator {
		
		private static final long serialVersionUID = 6409919370339049090L;
		
		/**
		 * Creates a <code>SubscribeDFInit</code> instance
		 *
		 * @param agent The agent that adds the behaviour.
		 */
		SubscribeDFInit(Agent agent) {
			super(agent, DFService.createSubscriptionMessage(agent, getDefaultDF(), new DFAgentDescription(), null));
		}
		
		/**
		 * Handles an inform message from the DF.
		 */
		protected void handleInform(ACLMessage inform) {
			// decode the message
			try {
				DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
				// update the GUI
				eiGUI.updateGUI(dfds);
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
		
	} // end SubscribeDFInit class
	
	
	/**
	 * Inner class to implement the show-gui command of the ei-management protocol initiator role.
	 * <br>
	 * Implemented as an initiator role.
	 */
	protected class EIManagementInit_ShowGUI extends SimpleAchieveREInitiator {
		
		private static final long serialVersionUID = -2598399170412378742L;
		
		private AID to;
		
		/**
		 * Creates an <code>EIManagementInit_ShowGUI</code> instance.
		 *
		 * @param agent	The agent that adds the behaviour
		 * @param to	The agent to send the show-gui command to
		 */
		EIManagementInit_ShowGUI(Agent agent, AID to) {
			super(agent, new ACLMessage(ACLMessage.REQUEST));
			
			this.to = to;
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
			msg.setOntology(ElectronicInstitution.EI_MANAGEMENT_ONTOLOGY);
			// set the language of the request to be sent
			msg.setLanguage(slCodec.getName());
			// add the envisaged agent as receiver of the request to be sent
			msg.addReceiver(to);
			// add the show-gui command as content of the request to be sent
			ShowGui eim = new ShowGui();
			Action act = new Action(to, eim);
			try{
				getContentManager().fillContent(msg,act);
			}
			catch(OntologyException oe){
				oe.printStackTrace();
			}
			catch(Codec.CodecException ce){
				ce.printStackTrace();
			}
			// return the message to use as request
			return msg;
		}
		
		/**
		 * Handles an agree message from the envisaged agent.
		 */
		protected void handleAgree(ACLMessage agree) {
		}
		
		/**
		 * Handles a refuse message from the envisaged agent.
		 */
		protected void handleRefuse(ACLMessage refuse) {
			logErr(to.getLocalName() + " refused to show-gui");
		}
		
		/**
		 * Handles an inform message from the envisaged agent.
		 */
		protected void handleInform(ACLMessage inform) {
		}
		
		/**
		 * Handles a failure message from the envisaged agent.
		 */
		protected void handleFailure(ACLMessage failure) {
			logErr(to.getLocalName() + " failed to show-gui");
		}
		
	} // end EIManagementInit_ShowGUI class


	protected boolean createGUI() {
		return false;
	}
	
}
