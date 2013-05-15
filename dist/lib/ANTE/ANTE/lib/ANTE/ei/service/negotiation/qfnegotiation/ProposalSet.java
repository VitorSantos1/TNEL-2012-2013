package ei.service.negotiation.qfnegotiation;

import jade.lang.acl.ACLMessage;
import java.util.Vector;

/**
 * Represents the set of proposals made by all the agents for a certain component during a round of the negotiation.
 * @author PBN 
 */
public class ProposalSet {
	private Vector<ACLMessage> msgProposals = new Vector<ACLMessage>();;  
	private ACLMessage bestMsgProposal; //the best proposal of the set
	
	public ProposalSet(Vector<ACLMessage> receivedProposals) {
		this.msgProposals = receivedProposals;
		this.bestMsgProposal = null;
	}
	
	public Vector<ACLMessage> getMsgProposals() {
		return msgProposals;
	}
	public void setMsgProposals(Vector<ACLMessage> msgProposals) {
		this.msgProposals = msgProposals;
	}
	public ACLMessage getBestMsgProposal() {
		return bestMsgProposal;
	}
	public void setBestMsgProposal(ACLMessage bestProposal) {
		this.bestMsgProposal = bestProposal;
	}
}
