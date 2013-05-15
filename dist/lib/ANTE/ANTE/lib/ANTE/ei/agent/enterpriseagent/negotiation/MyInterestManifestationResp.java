package ei.agent.enterpriseagent.negotiation;

import jade.core.AID;
import jade.core.Agent;
import ei.agent.enterpriseagent.EnterpriseAgent;
import ei.agent.enterpriseagent.ontology.Need;
import ei.onto.negotiation.InterestManifestationCall;
import ei.proto.negotiation.InterestManifestationResp;

/**
 * 
 * @author pbn
 */
public class MyInterestManifestationResp extends InterestManifestationResp {
	private static final long serialVersionUID = 1L;

	public MyInterestManifestationResp(Agent agent) {
		super(agent);
	}

	/**
	 * Determines if this agent is interested in participating in a need negotiation.
	 * In this implementation, the agent is interested if he supplies the need, as given by <code>findMatchingCompetence</code>.
	 * Furthermore, if the agent is configured to use the ontology mapping service, this service will be used when no matching need
	 * is found. If a need mapping is provided by the ontology mapping service, then this agent will also be interested in the negotiation. 
	 */
	public boolean isInterestedInNegotiation(InterestManifestationCall interestManifestationCall) {
		Need need = interestManifestationCall.getNeed();
		AID requesterAgent = interestManifestationCall.getRequester();
		
		return ((EnterpriseAgent) myAgent).findCompetence(need, requesterAgent) != null;
	}
	
} // end MyInterestManifestationResp class

