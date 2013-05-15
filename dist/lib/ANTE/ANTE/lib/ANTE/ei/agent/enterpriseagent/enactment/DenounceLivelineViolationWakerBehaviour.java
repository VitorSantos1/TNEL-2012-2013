package ei.agent.enterpriseagent.enactment;

import ei.onto.normenv.report.LivelineViolation;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

/**
 * Waker behaviour for denouncing liveline violations after a certain amount of time.
 * 
 * @author hlc
 */
public class DenounceLivelineViolationWakerBehaviour extends WakerBehaviour {

	private static final long serialVersionUID = -364141667667610381L;

	private LivelineViolation lviol;

	/**
	 * 
	 * @param agent
	 * @param lviol	The liveline violation to denounce
	 * @param when	The delay to wait before denouncing
	 */
	public DenounceLivelineViolationWakerBehaviour(Agent agent, LivelineViolation lviol, long when) {
		super(agent, when);
		this.lviol = lviol;
	}

	protected void onWake() {
		((EnactmentEnterpriseAgent) myAgent).denounceLivelineViolation(lviol);
	}
	
}
