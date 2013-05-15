package ei.onto.notary;

import jade.content.Predicate;

/**
 * 
 * @author hlc
 */
public class ContractSigned implements Predicate {
	
	private static final long serialVersionUID = -5775213626217243146L;
	
	private String contractId;
	
	public String getContractId() {
		return contractId;
	}
	
	public void setContractId(String contractId) {
		this.contractId = contractId;
	}
	
}
