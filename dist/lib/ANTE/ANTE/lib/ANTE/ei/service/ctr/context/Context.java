package ei.service.ctr.context;

/**
 * Represents the Context for CTR Evaluation 
 */
public class Context {
	
	String name;
	int quantity;
	Long deliveryTime;
	String seller;
	Double fine;
	
	public Context(){
		
	}
	
	/**
	 * Sets the product name
	 * 
	 * @param name	the new good name
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * Gets the product name
	 * 
	 * @return	the good name
	 */
	public String getName(){
		return name;
	}

	/**
	 * Returs the quantity negotiated of this product
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * Sets the quantity negotiated of this product
	 * 
	 * @param quantity
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	/**
	 * Returns the delivery time for this product
	 */
	public Long getDeliveryTime() {
		return deliveryTime;
	}

	/**
	 * Sets the delivery time
	 * 
	 * @param deliverTime
	 */
	public void setDeliveryTime(Long deliverTime) {
		this.deliveryTime = deliverTime;
	}

	/**
	 * Sets the name of the seller in this contract
	 * @param seller the seller to set
	 */
	public void setSeller(String seller) {
		this.seller = seller;
	}

	/**
	 * Gets the name of the seller in this contract
	 * @return the seller
	 */
	public String getSeller() {
		return seller;
	}

	/**
	 * @return the fine
	 */
	public Double getFine() {
		return fine;
	}

	/**
	 * @param fine the fine to set
	 */
	public void setFine(Double fine) {
		this.fine = fine;
	}
}
