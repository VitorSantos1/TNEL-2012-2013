package ei.agent.enterpriseagent.enactment;

import ei.onto.normenv.report.Obligation;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

/**
 * Waker behaviour for fulfilling obligations after a certain amount of time.
 * 
 * @author hlc
 */
public class FulfillObligationWakerBehaviour extends WakerBehaviour {

	private static final long serialVersionUID = -364141667667610381L;

	private Obligation obligation;

	/**
	 * 
	 * @param agent
	 * @param obligation	The obligation to fulfill
	 * @param when			The delay to wait before fulfilling the obligation
	 */
	public FulfillObligationWakerBehaviour(Agent agent, Obligation obligation, long when) {
		super(agent, when);

		this.obligation = obligation;
	}

	protected void onWake() {
		((EnactmentEnterpriseAgent) myAgent).fulfillObligation(obligation);
	}

}
