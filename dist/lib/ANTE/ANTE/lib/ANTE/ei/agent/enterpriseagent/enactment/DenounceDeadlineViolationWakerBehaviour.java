package ei.agent.enterpriseagent.enactment;

import java.util.Vector;

import ei.agent.enterpriseagent.EnterpriseAgent;
import ei.onto.normenv.report.DeadlineViolation;
import ei.onto.normenv.report.Fulfillment;
import ei.onto.normenv.report.Report;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

/**
 * Waker behaviour for denouncing deadline violations after a certain amount of time.
 * 
 * @author hlc
 */
public class DenounceDeadlineViolationWakerBehaviour extends WakerBehaviour {

	private static final long serialVersionUID = -364141667667610381L;

	private DeadlineViolation dviol;

	/**
	 * 
	 * @param agent
	 * @param dviol	The deadline violation to denounce
	 * @param when	The delay to wait before denouncing
	 */
	public DenounceDeadlineViolationWakerBehaviour(Agent agent, DeadlineViolation dviol, long when) {
		super(agent, when);
		this.dviol = dviol;
	}

	protected void onWake() {
		// check if the obligation was not fulfilled in the mean time
		Vector<Report> reports = ((EnterpriseAgent) myAgent).getContractReports(dviol.getContext());
		for(Report r:reports) {
			if(r instanceof Fulfillment) {
				if(((Fulfillment) r).getObligation().equals(dviol.getObligation())) {
					return;
				}
			}
		}

		((EnactmentEnterpriseAgent) myAgent).denounceDeadlineViolation(dviol);
	}
	
}
