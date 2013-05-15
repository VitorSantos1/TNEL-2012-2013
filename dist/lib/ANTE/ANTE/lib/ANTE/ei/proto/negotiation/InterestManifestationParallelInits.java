package ei.proto.negotiation;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ei.agent.enterpriseagent.ontology.Need;
import ei.service.negotiation.qfnegotiation.InterestedAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;

/**
 * Allows the parallel execution of several <code>InterestManifestationInit<code>, according to the number of needs.
 * 
 * @author pbn, hlc
  */
public class InterestManifestationParallelInits extends ParallelBehaviour {
	private static final long serialVersionUID = 1L;
	
	private java.util.ArrayList<InterestManifestationInit> interestManifestationInitList = new java.util.ArrayList<InterestManifestationInit>();		
	private Hashtable<Need,Vector<InterestedAgent>> agentsPerNeed = new Hashtable<Need,Vector<InterestedAgent>>();
	
	private String contractType;
	private AID requester;
	
	/**
	 * @param agent
	 * @param contractType
	 * @param requester
	 */
	public InterestManifestationParallelInits(Agent agent, String contractType, AID requester) {
		super(agent, ParallelBehaviour.WHEN_ALL);
		this.contractType = contractType;
		this.requester = requester;
	}

	public Hashtable<Need, Vector<InterestedAgent>> getAgentsPerNeed() {
		return agentsPerNeed;
	}

	public void setAgentsPerNeeed(Hashtable<Need, Vector<InterestedAgent>> agentsPerNeed) {
		this.agentsPerNeed = agentsPerNeed;
	}

	public void onStart() {
		Enumeration<Need> needs = agentsPerNeed.keys();
		while(needs.hasMoreElements()) {
			Need need = needs.nextElement();
			Vector<InterestedAgent> agents = agentsPerNeed.get(need);
			InterestManifestationInit interestManifestationInit = new InterestManifestationInit(myAgent, need, contractType, requester, agents);
			addSubBehaviour(interestManifestationInit);
			interestManifestationInitList.add(interestManifestationInit);
		}
	}
	
	public int onEnd() {
		for (InterestManifestationInit interestManifestationInit: interestManifestationInitList) {
			agentsPerNeed.put(interestManifestationInit.getNeed(), interestManifestationInit.getInterestedEnterpriseAgents());
		}

		return 0;
	}
	
}
