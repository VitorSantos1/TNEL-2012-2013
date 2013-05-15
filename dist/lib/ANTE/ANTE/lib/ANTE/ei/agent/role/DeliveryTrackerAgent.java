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
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

/**
 * An agent performing the IR-delivery-tracker role.
 * <p>
 * The delivery-tracker agent implements the following interaction protocol roles:
 * <ul>
 * <li>the transaction responder role
 * </ul>
 * 
 * @author hlc
 */
public class DeliveryTrackerAgent extends ExternalAgent {
	
	private static final long serialVersionUID = -4068475690784366391L;
	
	protected Hashtable<String, Vector<Object>> transactions = new Hashtable<String, Vector<Object>>(); 
	
	/**
	 * Set up of the delivery-tracker agent.
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
	 * The delivery-tracker will handle requests concerning the delivery of products between different agents.
	 * 
	 * @author hlc
	 */
	protected class TransactionResp extends SimpleAchieveREResponder {
		
		private static final long serialVersionUID = -2108365618268954841L;
		
		// action received in the request message
		AgentAction request_action;
		
		private String context;
		private AID from;
		private String ref;
		private String product;
		private int quantity;
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
			
			if(request_action instanceof Deliver) {
				context = ((Deliver) request_action).getContext();
				from = request.getSender();
				ref = ((Deliver) request_action).getRef();
				product = ((Deliver) request_action).getProduct();
				quantity = ((Deliver) request_action).getQuantity();
				to = ((Deliver) request_action).getTo();
				// if requested action is a delivery, agree
				reply.setPerformative(ACLMessage.AGREE);
			} else
				reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			
			// return the created reply
			return reply;
		}
		
		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
			ACLMessage reply = request.createReply();
			
			if(request_action instanceof Deliver) {
				// TODO? For now I assume the delivery succeeds...
				// inform the envisaged agent through a message?
				
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent("done");
				
				// inform the normative environment
				ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
				inform.setLanguage(getSlCodec().getName());
				inform.setOntology(ElectronicInstitution.ILLOCUTION_ONTOLOGY);
				inform.addReceiver(fetchAgent(ElectronicInstitution.SRV_NORMATIVE_ENVIRONMENT, false));
				// inform the envisaged agent?
				//inform.addReceiver(to);
				
				Delivered d = new Delivered();
				d.setContext(context);
				d.setFrom_agent(from);
				d.setRef(ref);
				d.setProduct(product);
				d.setQuantity(quantity);
				d.setTo_agent(to);
				d.setWhen(System.currentTimeMillis());
				try {
					getContentManager().fillContent(inform, d);
				} catch(Codec.CodecException ce) {
					ce.printStackTrace();
					logErr("Error filling content with illocution");
				} catch(OntologyException oe) {
					oe.printStackTrace();
					logErr("Error filling content with illocution");
				}
				
				send(inform);
				
				//add transaction to table
				if (getGUI() != null) {
					((DeliveryTrackerAgentGUI) gui).addTransactionRow(from, to, context, product, quantity);
				}
				
				Vector<Object> trans = new Vector<Object>();
				trans.add(from);
				trans.add(to);
				trans.add(product);
				trans.add(quantity);
				transactions.put(context, trans);
				
			} else {
				logErr("BUG: This situation should never occur!");
				reply.setPerformative(ACLMessage.FAILURE);
			}
			
			// return the created reply
			return reply;
		}
		
	} // end TransactionResp class


	protected boolean createGUI() {
		gui = new DeliveryTrackerAgentGUI(this);
		return true;
	}
	
}
