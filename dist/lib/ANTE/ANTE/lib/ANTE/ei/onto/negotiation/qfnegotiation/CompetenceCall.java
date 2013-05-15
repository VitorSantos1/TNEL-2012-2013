package ei.onto.negotiation.qfnegotiation;

import ei.agent.enterpriseagent.ontology.Need;
import jade.content.Predicate;
import jade.core.AID;

/**
 * This class represents the action "mediate-negotiation". It is used by an enterprise agent to ask the negotiation mediator to
 * mediate a negotiation concerning the composition of a given good.
 */
public class CompetenceCall implements Predicate {

	private static final long serialVersionUID = 6617334951707344975L;
	
	private Need need;       // the requested need
	private String contractType;  // the contract type
	private AID requester;   // the agent who requested the need
	private double fine = 0.0;

	public void setContractType(String ct) {
		contractType = ct;
	}

	public String getContractType() {
		return contractType;
	}

	public void setNeed(Need need) {
		this.need = need;
	}

	public Need getNeed() {
		return need;
	}

	public void setRequester(AID requester) {
		this.requester = requester;
	}

	public AID getRequester() {
		return this.requester;
	}

	public void setFine(double fine) {
		this.fine = fine;
	}

	public double getFine() {
		return fine;
	}
	
	public String toString() {
		return "CompetenceCall (" + need + ") (contractType " + contractType + ") (requester " + requester.getLocalName() + ") (fine " + fine + ")";
	}
	
}
