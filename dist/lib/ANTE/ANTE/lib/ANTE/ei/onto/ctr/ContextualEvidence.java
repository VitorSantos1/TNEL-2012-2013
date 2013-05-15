package ei.onto.ctr;

import jade.content.Term;

/**
 * This class represents a Context to be used in CTR to trust evaluation.
 * 
 * @author Sérgio
 *
 */
public class ContextualEvidence implements Term {

	private static final long serialVersionUID = 1611501663252871384L;
	
	private int quantity = 0;
	private long deliveryTime = 0l; 
	private String needName = "";

	/**
	 * Sets the delivery time of contract
	 * @param deliveryTime
	 */
	public void setDeliveryTime(long deliveryTime) {
		this.deliveryTime = deliveryTime;
	}

	/**
	 * Returns the delivery time of contract
	 */
	public long getDeliveryTime() {
		return deliveryTime;
	}

	/**
	 * Sets the quantity of contract
	 * @param quantity
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	/**
	 * Returns the quantity of contract
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * Sets the product's name of contract
	 * @param name
	 */
	public void setNeedName(String name) {
		this.needName = name;
	}

	/**
	 * Returns the product's name of contract
	 */
	public String getNeedName() {
		return needName;
	}
}