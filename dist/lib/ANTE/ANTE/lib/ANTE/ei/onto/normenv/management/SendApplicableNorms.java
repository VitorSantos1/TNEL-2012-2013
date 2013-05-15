package ei.onto.normenv.management;

import jade.content.AgentAction;

/**
 * This class represents the action "send-applicable-norms".
 * 
 * @author hlc
 */
public class SendApplicableNorms implements AgentAction {
	
	private static final long serialVersionUID = -5775213626217243146L;
	
	private String contractType;
	
	public String getContractType() {
		return contractType;
	}
	
	public void setContractType(String contractType) {
		this.contractType = contractType;
	}
	
}
