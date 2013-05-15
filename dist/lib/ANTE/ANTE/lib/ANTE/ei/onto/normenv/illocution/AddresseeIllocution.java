package ei.onto.normenv.illocution;

import jade.core.AID;

/**
 * @author hlc
 */
public class AddresseeIllocution extends Illocution {
	
	private static final long serialVersionUID = 8333355445600154364L;
	
	private AID from_agent;
	
	public AID getFrom_agent() {
		return from_agent;
	}
	
	public void setFrom_agent(AID fa) {
		from_agent = fa;
	}

}
