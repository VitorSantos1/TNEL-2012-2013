package ei.service.normenv;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

import java.util.Date;

import ei.service.normenv.NormativeEnvironment;

/**
 * Waker behaviour for scheduling time alerts.
 * <br>
 * When waking, this behaviour will assert a time event into the Jess engine.
 * 
 * @author hlc
 */
class TimeAlertBehaviour extends WakerBehaviour {
	
	private static final long serialVersionUID = 292280720279367637L;
	
	String context;
	long when;
	
	/**
	 * 
	 * @param agent
	 * @param ctx
	 * @param wakeupDate
	 */
	protected TimeAlertBehaviour(Agent agent, String context, long when) {
		super(agent, new Date(when));
		
		this.context = context;
		this.when = when;	//if the date is past, this behaviour will fire immediately
	}
	
	protected void onWake() {
		((NormativeEnvironment) myAgent).normEnvBeh.newTimeEvent(context, when);
	}
	
} // end TimeAlertBehaviour class
