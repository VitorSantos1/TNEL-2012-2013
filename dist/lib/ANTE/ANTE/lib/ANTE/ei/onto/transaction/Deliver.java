package ei.onto.transaction;

import jade.content.AgentAction;
import jade.core.AID;

public class Deliver implements AgentAction {
	
	private static final long serialVersionUID = -5775213626217243146L;
	
	private String context;
	private String ref;
	private String product;
	private int quantity;
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

	public String getProduct() {
		return product;
	}
	
	public void setProduct(String product) {
		this.product = product;
	}
	
	public int getQuantity() {
		return quantity;
	}
	
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public AID getTo() {
		return to;
	}
	
	public void setTo(AID to) {
		this.to = to;
	}
	
	
}
