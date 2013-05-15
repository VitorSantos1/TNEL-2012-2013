package ei.service.normenv;

import jade.core.Agent;

import jess.Context;
import jess.Funcall;
import jess.JessException;
import jess.Userfunction;
import jess.Value;
import jess.ValueVector;

/**
 * This class implements the Jess userfunction to schedule time alerts.
 * It can be used by Jess by using the name <code>schedule-time-alert</code>.
 * 
 * @author hlc
 */
public class JessScheduleTimeAlert implements Userfunction {
	
	Agent myAgent;
	
	public JessScheduleTimeAlert(Agent a) {
		myAgent = a;
	}
	
	// Name by which the function appears in Jess
	public String getName() {
		return ("schedule-time-alert");
	}
	
	// Called when (schedule-time-alert ...) is encountered
	public Value call(ValueVector vv, Context context) throws JessException {
		// JESS calls (schedule-time-alert ?ctx ?w)
		// get the ?ctx (first argument) as a String
		String ctx = vv.get(1).symbolValue(context);
		// get the ?w (second argument) as a long
		long w = vv.get(2).longValue(context);
		
		// schedule new behaviour
		myAgent.addBehaviour(new TimeAlertBehaviour(myAgent, ctx, w));
		
		return Funcall.TRUE;
	}
	
} // end JessScheduleTimeAlert class
