package ei.proto.negotiation.qfnegotiation;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ei.agent.enterpriseagent.ontology.Need;
import ei.onto.util.Parameters;
import ei.service.negotiation.qfnegotiation.InterestedAgent;
import ei.service.negotiation.qfnegotiation.QFNegotiationParameters;

/**
 * Allows the parallel execution of several <code>QFNegotiationInit<code>, according to the number of needs.
 * 
 * @author pbn, hlc
 */
public class QFNegotiationParallelInits extends ParallelBehaviour {
	private static final long serialVersionUID = 1L;

	private java.util.ArrayList<QFNegotiationInit> qfNegotiationInitList = new java.util.ArrayList<QFNegotiationInit>();
	private Hashtable<Need,Vector<InterestedAgent>> agentsPerNeed = new Hashtable<Need,Vector<InterestedAgent>>();
	
	public String negotiationsID;
	private AID requester;
	private Parameters negotiationParameters;

	private List winningProposals  = new ArrayList();

	public QFNegotiationParallelInits(Agent agent, String negotiationsID, AID requester, Parameters negotiationParameters) {
		super(agent, ParallelBehaviour.WHEN_ALL);
		
		this.negotiationsID = negotiationsID;
		this.requester = requester;
		this.negotiationParameters = negotiationParameters;
	}

	public Hashtable<Need, Vector<InterestedAgent>> getAgentsPerNeed() {
		return agentsPerNeed;
	}

	public void setAgentsPerNeed(Hashtable<Need, Vector<InterestedAgent>> agentsPerNeed) {
		this.agentsPerNeed = agentsPerNeed;
	}

	public void onStart() {
		Enumeration<Need> needs = agentsPerNeed.keys();
		while(needs.hasMoreElements()) {
			Need need = needs.nextElement();
			QFNegotiationInit qFNegotiationInit = new QFNegotiationInit(myAgent, requester, need, agentsPerNeed.get(need), negotiationParameters);
			addSubBehaviour(qFNegotiationInit);
			qfNegotiationInitList.add(qFNegotiationInit);
		}
	}

	public int onEnd() {
		for (QFNegotiationInit qFNegotiationInit : qfNegotiationInitList) {
			if(!qFNegotiationInit.isSuccess()) {
				agentsPerNeed.put(qFNegotiationInit.getNeed(), new Vector<InterestedAgent>());
			} else {
				Vector<InterestedAgent> v = new Vector<InterestedAgent>();
				v.add(new InterestedAgent(qFNegotiationInit.getWinnerProposal().getIssuer()));
				agentsPerNeed.put(qFNegotiationInit.getNeed(), v);
				
				winningProposals.add(qFNegotiationInit.getWinnerProposal());
			}
		}
		
		return 0;
	}

	public List getWinningProposals() {
		return winningProposals;
	}

	public java.util.ArrayList<QFNegotiationInit> getQFNegotiationInitList() {
		return qfNegotiationInitList;
	}

	public boolean isUseTrustInPreselection() {
		return (negotiationParameters.get(QFNegotiationParameters.USE_TRUST_IN_PRESELECTION) != null) ? Boolean.parseBoolean(negotiationParameters.get(QFNegotiationParameters.USE_TRUST_IN_PRESELECTION)) : QFNegotiationParameters.USE_TRUST_IN_PRESELECTION_DEFAULT;
	}
	
	public boolean isUseTrustInProposalEvaluation() {
		return (negotiationParameters.get(QFNegotiationParameters.USE_TRUST_IN_PROPOSAL_EVALUATION) != null) ? Boolean.parseBoolean(negotiationParameters.get(QFNegotiationParameters.USE_TRUST_IN_PROPOSAL_EVALUATION)) : QFNegotiationParameters.USE_TRUST_IN_PROPOSAL_EVALUATION_DEFAULT;
	}
	
	public boolean isUseTrustInContractDrafting() {
		return (negotiationParameters.get(QFNegotiationParameters.USE_TRUST_IN_CONTRACT_DRAFTING) != null) ? Boolean.parseBoolean(negotiationParameters.get(QFNegotiationParameters.USE_TRUST_IN_CONTRACT_DRAFTING)) : QFNegotiationParameters.USE_TRUST_IN_CONTRACT_DRAFTING_DEFAULT;
	}

}
