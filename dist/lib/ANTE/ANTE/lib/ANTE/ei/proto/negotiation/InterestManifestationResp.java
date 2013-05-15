package ei.proto.negotiation;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;
import ei.ElectronicInstitution;
import ei.onto.negotiation.InterestManifestationCall;

/**
 * Implements the interest manifestation responder role. Seller EnterpriseAgents use this role in order to decide on participating in negotiations.
 * 
 * @author pbn
 */
public abstract class InterestManifestationResp extends SimpleAchieveREResponder {
	private static final long serialVersionUID = -6036479596208732022L;
	
	private InterestManifestationCall interestManifestationCall;

	public InterestManifestationResp(Agent agent) {
		super(agent, MessageTemplate.and(	SimpleAchieveREResponder.createMessageTemplate(FIPA_QUERY),
											MessageTemplate.MatchOntology( ElectronicInstitution.NEGOTIATION_ONTOLOGY) ) );
	}

	protected ACLMessage prepareResponse(ACLMessage query) {
		try{
			ContentElement ce = myAgent.getContentManager().extractContent(query);
			if(ce instanceof InterestManifestationCall) {
				interestManifestationCall = (InterestManifestationCall) ce;
			}
		}catch(OntologyException oe){
			oe.printStackTrace();
		}catch(Codec.CodecException ce){
			ce.printStackTrace();
		}
		
		ACLMessage response = query.createReply();
		if(interestManifestationCall != null) {
			//response.setPerformative(ACLMessage.AGREE);
			//return response;
			return null;
		} else {
			response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			return response;
		}
	}
	
	protected  ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
		ACLMessage result = request.createReply();
		if(isInterestedInNegotiation(interestManifestationCall)) {
			result.setPerformative(ACLMessage.INFORM);
		} else {
			result.setPerformative(ACLMessage.FAILURE);
		}
		return result;
	}
	
	/**
	 * Determines if this agent is interested in participating in a need negotiation.
	 */
	public abstract boolean isInterestedInNegotiation(InterestManifestationCall interestManifestationCall);
	
}
