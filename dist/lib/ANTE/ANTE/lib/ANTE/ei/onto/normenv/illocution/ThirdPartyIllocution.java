package ei.onto.normenv.illocution;

import jade.core.AID;

/**
 * @author hlc
 */
public class ThirdPartyIllocution extends Illocution {
	
	private static final long serialVersionUID = -3642499243871852159L;
	
	private AID from_agent;
	private AID to_agent;
	
	public AID getFrom_agent() {
		return from_agent;
	}
	
	public void setFrom_agent(AID fa) {
		from_agent = fa;
	}

	public AID getTo_agent() {
		return to_agent;
	}
	
	public void setTo_agent(AID ta) {
		to_agent = ta;
	}

}
