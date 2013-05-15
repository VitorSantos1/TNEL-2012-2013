package ei.onto.negotiation;

import ei.agent.enterpriseagent.ontology.Need;

import jade.content.Predicate;
import jade.core.AID;

public class InterestManifestationCall implements Predicate {

	private static final long serialVersionUID = 6617334951707344975L;
	
	private Need need;        // the requested need
	private String contractType;   // the contract type
	private AID requester;      // the agent who requested the need

	public void setNeed(Need need) {
		this.need = need;
	}

	public Need getNeed() {
		return need;
	}

	public void setContractType(String contractType) {
		this.contractType = contractType;
	}

	public String getContractType() {
		return contractType;
	}

	public void setRequester(AID requester) {
		this.requester = requester;
	}

	public AID getRequester() {
		return requester;
	}
	
}
