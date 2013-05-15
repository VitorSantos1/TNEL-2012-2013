package ei.agent.enterpriseagent.sync;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import ei.EIAgent;
import ei.ElectronicInstitution;
import ei.agent.enterpriseagent.EnterpriseAgent;
import ei.agent.enterpriseagent.negotiation.QFNegotiationInit_Negotiate;
import ei.onto.synchronization.StartEpisode;

/**
 * This class is for SYNCHRONIZATION purposes.
 * 
 * @author hlc
 */
public class Episode_NegotiationResp extends AchieveREResponder {

	private static final long serialVersionUID = 8580885752577252965L;

	protected ACLMessage request;

	public Episode_NegotiationResp(Agent agent) {
		super(agent, MessageTemplate.and(	AchieveREResponder.createMessageTemplate(FIPA_REQUEST),
				MessageTemplate.MatchOntology(ElectronicInstitution.SYNCHRONIZATION_ONTOLOGY)));
	}

	protected ACLMessage handleRequest(ACLMessage request) {
		this.request = request;

		ContentElement contentElement = null;
		StartEpisode startEpisode = null;
		try {
			contentElement = myAgent.getContentManager().extractContent(request);
			if(contentElement instanceof Action) {
				Action act = (Action) contentElement;
				startEpisode = (StartEpisode) act.getAction();
			}
		} catch(Codec.CodecException cex) {
			cex.printStackTrace();
		} catch(OntologyException oe) {
			oe.printStackTrace();
		}

		AID negotiationMediator = ((EIAgent) myAgent).fetchAgent(ElectronicInstitution.SRV_NEGOTIATION_FACILITATOR, false);

		QFNegotiationInit_Negotiate negotiationInit_Negotiate =
			new QFNegotiationInit_Negotiate(myAgent, negotiationMediator, ((EnterpriseAgent) myAgent).createListOfNeedsToBeNegotiated(), ((EnterpriseAgent) myAgent).fillInNegotiationParameters(startEpisode.getParameters()));
		this.registerPrepareResultNotification(new NegotiationInit_NegotiateWrapper(negotiationInit_Negotiate, this));

		//ACLMessage answer = request.createReply();
		//answer.setPerformative(ACLMessage.AGREE);
		//return answer;
		return null;
	}

} // end Episode_NegotiationResp class

