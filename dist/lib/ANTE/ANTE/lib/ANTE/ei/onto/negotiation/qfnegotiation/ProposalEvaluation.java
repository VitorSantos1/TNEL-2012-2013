package ei.onto.negotiation.qfnegotiation;

import jade.util.leap.List;

/**
 * Represents a proposal. This class is composed of two parts. Part of the proposal is created by the proposing agent, and includes
 * the attribute values. Another part concerns the proposal's evaluation and feedback, which is provided by the negotiation mediator.
 */
public class ProposalEvaluation extends Proposal {
	private static final long serialVersionUID = -8703771178474588373L;
	
	private float evaluation; // the evaluation of this proposal
	
	public ProposalEvaluation() {
		super();
	}
	
	public ProposalEvaluation(Proposal proposal) {
		this();
		this.setIssuer(proposal.getIssuer());
		this.setNeedType(proposal.getNeedType());
		this.setFine(proposal.getFine());
	}

	public void setEvaluation(float ev){
		evaluation = ev;
	}
	
	public float getEvaluation(){
		return evaluation;
	}
	
	public List getAttributeValueEvaluations() {
		return getAttributeValues();
	}
	
	// ---
	
	public AttributeValueEvaluation getAttributeValueEvaluation(String name){
		for(int j=0;j<getAttributeValueEvaluations().size();j++) {
			AttributeValueEvaluation a = (AttributeValueEvaluation) getAttributeValueEvaluations().get(j);
			if(a.getName().equals(name))
              return a;
		}
		return null;
	}
	
	public String toString() {
		return "PropEval (" + getNeedType() + " " + getAttributeValues() + ")";
	}
	
}
