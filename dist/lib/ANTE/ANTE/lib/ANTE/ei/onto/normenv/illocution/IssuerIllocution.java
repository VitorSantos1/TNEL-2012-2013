package ei.onto.normenv.illocution;

import jade.core.AID;

/**
 * @author hlc
 */
public class IssuerIllocution extends Illocution {
	
	private static final long serialVersionUID = -17797871992912233L;
	
	private AID to_agent;
	
	public AID getTo_agent() {
		return to_agent;
	}
	
	public void setTo_agent(AID ta) {
		to_agent = ta;
	}

}
