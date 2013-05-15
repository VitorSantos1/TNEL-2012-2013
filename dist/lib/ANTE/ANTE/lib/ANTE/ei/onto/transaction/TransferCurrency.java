package ei.onto.transaction;

import jade.content.AgentAction;
import jade.core.AID;

public class TransferCurrency implements AgentAction {
	
	private static final long serialVersionUID = -5775213626217243146L;
	
	private String context;
	private String ref;
	private double amount;
	private AID to;
	
	public String getContext() {
		return context;
	}
	
	public void setContext(String context) {
		this.context = context;
	}
	
	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getRef() {
		return ref;
	}

	public double getAmount() {
		return amount;
	}
	
	public void setAmount(double amount) {
		this.amount = amount;
	}
	
	public AID getTo() {
		return to;
	}
	
	public void setTo(AID to) {
		this.to = to;
	}
	
	
}
