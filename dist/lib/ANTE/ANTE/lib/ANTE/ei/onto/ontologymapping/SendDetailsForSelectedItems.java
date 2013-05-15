package ei.onto.ontologymapping;

import jade.content.AgentAction;

public class SendDetailsForSelectedItems implements AgentAction {

	private static final long serialVersionUID = 1L;

	private float price;
	private float price1;
	private float price2;
	
	public SendDetailsForSelectedItems(float price, float price1, float price2) {
		this.price = price;
		this.price1 = price1;
		this.price2 = price2;
	}
	
	public void setPrice(float price) {
		this.price = price;
	}
	
	public float getPrice() {
		return price;
	}
	
	public void setPrice1(float price1) {
		this.price1 = price1;
	}
	
	public float getPrice1() {
		return price1;
	}
	
	public void setPrice2(float price2) {
		this.price2 = price2;
	}
	
	public float getPrice2() {
		return price2;
	}
	
}
