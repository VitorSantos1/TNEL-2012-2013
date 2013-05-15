package ei.proto.ctr;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ei.EIAgent;
import ei.agent.enterpriseagent.ontology.Need;
import ei.service.ctr.OutcomeGenerator.MappingMethod;
import ei.service.negotiation.qfnegotiation.InterestedAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;

/**
 * The CTRParallelInits_SendTopAgents allows the parallel execution of several <code>CTRInit_SendTopAgents<code> protocols of according 
 * the number of needs. 
 * It executes its sub-behaviours (CTRInit_SendTopAgents) concurrently, and it terminates when a particular condition on its sub-behaviours 
 * is met i.e. when all them are done. 
 * @author pbn, hlc
  */
public class CTRParallelInits_SendTopAgents extends ParallelBehaviour {
	private static final long serialVersionUID = 1L;
	
	private java.util.ArrayList<CTRInit_SendTopAgents> ctrInit_SendTopAgentsList = new java.util.ArrayList<CTRInit_SendTopAgents>();		
	private Hashtable<Need,Vector<InterestedAgent>> agentsPerNeed = new Hashtable<Need,Vector<InterestedAgent>>();
	
	private int topNumberOfAgents;
	private boolean useContextual;
	private MappingMethod mapMethod;
	private AID requester;
	
	/**
	 * @param agent
	 * @param requester
	 * @param topNumberOfAgents
	 * @param useContextual
	 * @param mapMethod
	 */
	public CTRParallelInits_SendTopAgents(Agent agent, AID requester, int topNumberOfAgents, boolean useContextual, MappingMethod mapMethod) {
		super(agent, ParallelBehaviour.WHEN_ALL);
		
		this.topNumberOfAgents = topNumberOfAgents;
		this.useContextual = useContextual;
		this.mapMethod = mapMethod;
		this.requester = requester;
	}
	
	public void setAgentsPerNeed(Hashtable<Need, Vector<InterestedAgent>> agentsPerNeed) {
		this.agentsPerNeed = agentsPerNeed;
	}

	public Hashtable<Need, Vector<InterestedAgent>> getAgentsPerNeed() {
		return agentsPerNeed;
	}

	public void onStart() {
		Enumeration<Need> needs = agentsPerNeed.keys();
		while(needs.hasMoreElements()) {
			Need need = needs.nextElement();
			Vector<InterestedAgent> agents = agentsPerNeed.get(need);
			try {
				int quantity = 0;
				int dTime = 0;
				if (need.getAttribute("quantity") != null) {
					quantity = Integer.parseInt((String) need.getAttribute("quantity").getPreferredValue());
				}
				if (need.getAttribute("delivery") != null) {
					dTime = Integer.parseInt((String) need.getAttribute("delivery").getPreferredValue());
				}

				CTRInit_SendTopAgents requestTopAgents = new CTRInit_SendTopAgents(myAgent, requester, agents, need, topNumberOfAgents, useContextual, quantity, dTime, mapMethod);
				addSubBehaviour(requestTopAgents);
				ctrInit_SendTopAgentsList.add(requestTopAgents);
			} catch(Exception e) {
				((EIAgent) myAgent).logErr("Need " + need.getType() + " does not have attribute quantity and/or delivery");
				e.printStackTrace();
			}
		}
	}
	
	public int onEnd() {
		for (CTRInit_SendTopAgents requestTopAgents: ctrInit_SendTopAgentsList) {
			agentsPerNeed.put(requestTopAgents.getNeed(), requestTopAgents.getTopAgents());
		}
		
		return 0;
	}
	
}
