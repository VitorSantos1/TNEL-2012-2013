package ei.service.negotiation.qfnegotiation;

import jade.core.AID;

public class InterestedAgent {
	
	private AID agent;
	private Double ctrEvaluation;
	private Double contextualEvaluation;
	
	public InterestedAgent(AID agent)
	{
		this.agent = agent;
	}
	
	public void setAgent(AID agent) {
		this.agent = agent;
	}
	
	public AID getAgent() {
		return agent;
	}
	
	public void setCtrEvaluation(double ctrEvaluation) {
		this.ctrEvaluation = ctrEvaluation;
	}
	
	public Double getCtrEvaluation() {
		return ctrEvaluation;
	}
	
	public void setContextualEvaluation(double contextualEvaluation) {
		this.contextualEvaluation = contextualEvaluation;
	}
	
	public Double getContextualEvaluation() {
		return contextualEvaluation;
	}

}
