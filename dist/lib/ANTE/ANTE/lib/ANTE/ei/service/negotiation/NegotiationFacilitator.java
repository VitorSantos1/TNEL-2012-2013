package ei.service.negotiation;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.SSResponderDispatcher;
import ei.ElectronicInstitution;
import ei.onto.negotiation.Negotiate;
import ei.onto.negotiation.NegotiationOntology;
import ei.service.PlatformService;

/**
 * The generic class for negotiation facilitator services. This class includes a behaviour for handling negotiate requests.
 * 
 * @author hlc
 */
public abstract class NegotiationFacilitator extends PlatformService {
	private static final long serialVersionUID = 1L;

	protected void setup(){
		super.setup();

		// register additional ontologies
		getContentManager().registerOntology( NegotiationOntology.getInstance() );

		// responder role for NEGOTIATION_ONTOLOGY
		addBehaviour(new NegotiationRespDispatcher(this));
	}
	
	private class NegotiationRespDispatcher extends SSResponderDispatcher {
		private static final long serialVersionUID = 1L;
		
		private NegotiationRespDispatcher(Agent agent) {
			super(agent, MessageTemplate.and(	AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST),
												MessageTemplate.MatchOntology(ElectronicInstitution.NEGOTIATION_ONTOLOGY)));
		}
		
		protected Behaviour createResponder(ACLMessage message) {
			try {
				ContentElement contentElement = myAgent.getContentManager().extractContent(message);
				if(contentElement instanceof Action) {
					Action act = (Action) contentElement;
					if(act.getAction() instanceof Negotiate) {
						Negotiate negotiate = (Negotiate) act.getAction();
						return ((NegotiationFacilitator) myAgent).createResponderForNegotiateActionRequest(myAgent, message, negotiate);
					} else {
						ACLMessage response = message.createReply();
						response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
						send(response);
						return null;
					}
				}
			} catch(Codec.CodecException cex) {
				cex.printStackTrace();
			} catch(OntologyException oe) {
				oe.printStackTrace();
			}
			
			ACLMessage response = message.createReply();
			response.setPerformative(ACLMessage.FAILURE);
			send(response);
			return null;
		}
		
	} // end NegotiationRespDispatcher
	
	
	/**
	 * This method should return a behaviour capable of handling a negotiate action request.
	 * In particular, such a behaviour should answer appropriately to that request.
	 * A typical use is to start a negotiation process, and reply to the request according to the outcome of that process, by implementing onEnd().
	 * 
	 * @param agent
	 * @param request
	 * @param negotiate
	 * @return
	 */
	protected abstract Behaviour createResponderForNegotiateActionRequest(Agent agent, ACLMessage request, Negotiate negotiate);
	
}
