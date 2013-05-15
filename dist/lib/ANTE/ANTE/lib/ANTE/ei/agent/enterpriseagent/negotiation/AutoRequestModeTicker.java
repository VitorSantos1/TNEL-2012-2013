package ei.agent.enterpriseagent.negotiation;

import ei.agent.enterpriseagent.EnterpriseAgent;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

/**
 * Implements the auto request mode: the agent will automatically issue requests for negotiations from time to time.
 * 
 * @author hlc
 */
public class AutoRequestModeTicker extends TickerBehaviour {
	private static final long serialVersionUID = 1L;

	public AutoRequestModeTicker(Agent a) {
		super(a, 3000);
	}

	protected void onTick() {
		((EnterpriseAgent) myAgent).requestNegotiate();
	}
	
}

