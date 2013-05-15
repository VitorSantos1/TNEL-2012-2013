package ei.service.ctr;

import ei.service.ctr.OutcomeGenerator.Outcome;

/**
 * 
 * @author Joana Urbano - 30/09/2009
 * 		keep information of a contractual evidence. Attribute values are fuzzified as needed
 *
 */
public class ContractualHistory {
	public enum resultLabels {Fulfilled, Violated, DeadlineViolated, DeadlineViolatedFulfilled, DeadlineViolatedDeadlineViolated, DeadlineViolatedViolated, ViolatedFulfilled, ViolatedDeadlineViolated, ViolatedViolated, Null};
	private String supplierName;
	private String fabric;
	private Fuzzy.quantLabels quantity;
	private Fuzzy.deliveryTimeLabels deliveryTime;
	private Outcome outcome;
	private resultLabels result;
	private int numInstances;

	
	ContractualHistory () {
		this.supplierName = null;
		this.fabric = null;
		this.quantity = null;
		this.deliveryTime = null;
		this.outcome = null;
		
	}
	
	ContractualHistory (ContractEnactment contract, String product) {
		Fuzzy fuzzy = new Fuzzy ();

		this.supplierName = contract.getOwner();
		this.fabric = product;
		this.quantity = fuzzy.getQuantityLabel(contract.getContexts().get(0).getQuantity());
		this.deliveryTime = fuzzy.getDeliveryTimeLabel(contract.getContexts().get(0).getDeliveryTime());
		if(contract.getState().size()>0)
			this.outcome = contract.getState().get(0);
		else
			this.outcome = null;
	}
	
	String getSupplierName () {
		return this.supplierName;
	}
	
	String getFabric () {
		return this.fabric;
	}
	
	Outcome getOutcome () {
		return this.outcome;
	}
	
	resultLabels getResult () {
		return this.result;
	}

	Fuzzy.quantLabels getQuantity () {
		return this.quantity;
	}
	
	Fuzzy.deliveryTimeLabels getDeliveryTime () {
		return this.deliveryTime;
	}
	
	int getNumInstances () {
		return this.numInstances;
	}
	
	void setSupplierName (String supplierName) {
		this.supplierName = supplierName;
	}
	
	void setFabric (String fabric) {
		this.fabric = fabric;
	}
	
	void setOutcome (Outcome outcome) {
		this.outcome = outcome;
	}

	void setResult (resultLabels result) {
		this.result = result;
	}
	
	
	void setQuantity (Fuzzy.quantLabels quantity) {
		this.quantity = quantity;
	}
	
	void setDeliveryTime (Fuzzy.deliveryTimeLabels deliveryTime) {
		this.deliveryTime = deliveryTime;
	}
	
	public void setNumInstances (int numInstances) {
		this.numInstances = numInstances;
	}
	
	
	
	
	/**
	 * 07/10/2009
	 * @param contractualHistory
	 * @return numInstances of contractualHistory evidence or 0 if not similar
	 */
	public int isSimilar (ContractualHistory contractualHistory) {
		
		if (this.fabric != null && contractualHistory.getFabric() != null) { 
			if ((this.deliveryTime == contractualHistory.getDeliveryTime())
					&& (this.fabric.equals(contractualHistory.getFabric()))
					&& (this.quantity == contractualHistory.getQuantity())) {
					return contractualHistory.getNumInstances();
				}
			else return 0;
		}
		// at least the fabric of one of the stereotypes is null
		else {
			if ((this.deliveryTime == contractualHistory.getDeliveryTime())
					&& (this.quantity == contractualHistory.getQuantity())) {
					return contractualHistory.getNumInstances();
				}
			else return 0;
		}
	}
	
}