package ei.onto.transaction;

import jade.content.AgentAction;
import jade.core.AID;

public class DeliverMsg implements AgentAction {
	
	private static final long serialVersionUID = -5775213626217243146L;
	
	private String context;
	private String ref;
	private String msg;
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

	public String getMsg() {
		return msg;
	}
	
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public AID getTo() {
		return to;
	}
	
	public void setTo(AID to) {
		this.to = to;
	}
	
	
}
