package ei.agent.role;

import java.util.Hashtable;
import java.util.Vector;

import ei.ElectronicInstitution;
import ei.agent.ExternalAgent;
import ei.onto.normenv.illocution.*;
import ei.onto.transaction.*;

import jade.content.AgentAction;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

/**
 * An agent performing the IR-messenger role.
 * <p>
 * The messenger agent implements the following interaction protocol roles:
 * <ul>
 * <li>the transaction responder role
 * </ul>
 * 
 * @author hlc
 */
public class MessengerAgent extends ExternalAgent {
	
	private static final long serialVersionUID = 7798318676100057101L;
	
	protected Hashtable<String, Vector<Object>> messages = new Hashtable<String, Vector<Object>>(); 	
	
	/**
	 * Set up of the messenger agent.
	 */
	protected void setup() {
		// the setup method at the ExternalAgent class prepares the arguments
		super.setup();
		
		// register additional ontologies
		getContentManager().registerOntology(TransactionOntology.getInstance());
		getContentManager().registerOntology(IllocutionOntology.getInstance());
		
		// responder role for TRANSACTION_ONTOLOGY
		addBehaviour(new TransactionResp(this));
	}
	
	
	/**
	 * Inner class to implement the transaction responder role.
	 * The messenger will handle requests concerning the delivery of messages between different agents.
	 * 
	 * @author hlc
	 */
	protected class TransactionResp extends SimpleAchieveREResponder {
		
		private static final long serialVersionUID = -2298041093897482663L;
		
		// action received in the request message
		AgentAction request_action;
		
		private String context;
		private AID from;
		private String ref;
		private String message;
		private AID to;
		
		TransactionResp(Agent agent) {
			super(agent, MessageTemplate.and(	SimpleAchieveREResponder.createMessageTemplate(InteractionProtocol.FIPA_REQUEST),
												MessageTemplate.MatchOntology(ElectronicInstitution.TRANSACTION_ONTOLOGY)));
		}
		
		protected ACLMessage prepareResponse(ACLMessage request) {
			ACLMessage reply = request.createReply();
			request_action = null;
			
			// check requested action
			try {
				// extract content
				ContentElement ce = getContentManager().extractContent(request);
				if(ce instanceof Action)
					request_action = (AgentAction) ((Action) ce).getAction();
			} catch(Codec.CodecException ce) {
				ce.printStackTrace();
			} catch(OntologyException oe) {
				oe.printStackTrace();
			}
			
			if(request_action instanceof DeliverMsg) {
				context = ((DeliverMsg) request_action).getContext();
				from = request.getSender();
				ref = ((DeliverMsg) request_action).getRef();
				message = ((DeliverMsg) request_action).getMsg();
				to = ((DeliverMsg) request_action).getTo();
				// if requested action is a delivery, agree
				reply.setPerformative(ACLMessage.AGREE);
			} else
				reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			
			// return the created reply
			return reply;
		}
		
		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
			ACLMessage reply = request.createReply();
			
			if(request_action instanceof DeliverMsg) {
				//check if 'to' exists
				DFAgentDescription template = new DFAgentDescription();
				template.setName(to);
				DFAgentDescription[] result = null;
		    	try {
					result = DFService.search(myAgent,template);
				} catch (FIPAException e) {
					e.printStackTrace();
				}
				
				//if 'to' exists send the message, otherwise reply with a failure
				if(result != null) {
					// deliver the message
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					// Protocol? Ontology?
					msg.addReceiver(to);
					// ... msg-delivery ?ctx ?fr $?msg
					msg.setContent("msg-delivery " + context + " " + from + " " + message);
					send(msg);   // receiver is doing nothing with this message
					
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent("done");
					
					// inform the normative environment
					ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
					inform.setLanguage(getSlCodec().getName());
					inform.setOntology(ElectronicInstitution.ILLOCUTION_ONTOLOGY);
					inform.addReceiver(fetchAgent(ElectronicInstitution.SRV_NORMATIVE_ENVIRONMENT, false));
					
					MsgDelivered md = new MsgDelivered();
					md.setContext(context);
					md.setFrom_agent(from);
					md.setRef(ref);
					md.setMsg(message);
					md.setTo_agent(to);
					md.setWhen(System.currentTimeMillis());
					try {
						getContentManager().fillContent(inform, md);
					} catch(Codec.CodecException ce) {
						ce.printStackTrace();
						logErr("Error filling content with illocution");
					} catch(OntologyException oe) {
						oe.printStackTrace();
						logErr("Error filling content with illocution");
					}
					
					send(inform);
					
					//add the message to the GUI
					if (getGUI() != null) {
						((MessengerAgentGUI) gui).addMessageRow(from, to, context, message);
					}
					
					Vector<Object> msgs = new Vector<Object>();
					msgs.add(from);
					msgs.add(to);
					msgs.add(message);
					messages.put(context, msgs);
				}
				else{
					reply.setPerformative(ACLMessage.FAILURE);
				}
				
			} else {
				logErr("BUG: This situation should never occur!");
				reply.setPerformative(ACLMessage.FAILURE);
			}
			
			// return the created reply
			return reply;
		}
		
	} // end TransactionResp class


	protected boolean createGUI() {
		gui = new MessengerAgentGUI(this);
		return true;
	}
	
}
