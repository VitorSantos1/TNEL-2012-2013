package ei.onto.ontologymapping;

import jade.content.AgentAction;

public class SendDetailsForItem implements AgentAction {

	private static final long serialVersionUID = 1L;

	private String item;

	public SendDetailsForItem(String item) {
		this.item = item;
	}
	
	public void setItem(String item) {
		this.item = item;
	}

	public String getItem() {
		return item;
	}
	
}
