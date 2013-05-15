package ei.onto.ctr;

import jade.content.AgentAction;

/**
 * This abstract class represents an agent action for the CTR ontology.
 * It includes a reference to the agent about whom the reputation concerns.
 * 
 * @author hlc
 */
public abstract class CTRAgentAction implements AgentAction {
	
	private static final long serialVersionUID = -5775213626217243146L;
	
	private String agent;
	
	public String getAgent() {
		return agent;
	}
	
	public void setAgent(String agent) {
		this.agent = agent;
	}
	
}
