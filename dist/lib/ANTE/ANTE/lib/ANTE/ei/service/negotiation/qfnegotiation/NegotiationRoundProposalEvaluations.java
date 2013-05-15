package ei.service.negotiation.qfnegotiation;

import java.util.Vector;

import ei.onto.negotiation.qfnegotiation.ProposalEvaluation;

public class NegotiationRoundProposalEvaluations {
	private Vector<ProposalEvaluation> proposalEvaluations = new Vector<ProposalEvaluation>();
	private ProposalEvaluation bestProposalEvaluation; 
	private int atRound;
	
	public NegotiationRoundProposalEvaluations(Vector<ProposalEvaluation> ProposalEvaluation, ProposalEvaluation bestProposalEvaluation, int round) {
		this.proposalEvaluations = ProposalEvaluation;
		this.setAtRound(round);
		this.bestProposalEvaluation = bestProposalEvaluation;
	}
	public Vector<ProposalEvaluation> getProposalEvaluations() {
		return proposalEvaluations;
	}
	public void setProposalEvaluations(Vector<ProposalEvaluation> proposalEvaluations) {
		this.proposalEvaluations = proposalEvaluations;
	}
	
	public ProposalEvaluation getBestProposalEvaluation() {
		return bestProposalEvaluation;
	}
	public void setBestProposalEvaluation(ProposalEvaluation bestProposalEvaluation) {
		this.bestProposalEvaluation = bestProposalEvaluation;
	}
	public void setAtRound(int atRound) {
		this.atRound = atRound;
	}
	public int getAtRound() {
		return atRound;
	}
}
