package ei.onto.notary;

import jade.content.Predicate;
import jade.core.AID;

/**
 * 
 * @author hlc
 */
public class ContractSigningFailed implements Predicate {
	
	private static final long serialVersionUID = -5775213626217243146L;
	
	private String contractId;
	private AID cause;
	
	public String getContractId() {
		return contractId;
	}
	
	public void setContractId(String contractId) {
		this.contractId = contractId;
	}
	
	public AID getCause() {
		return cause;
	}
	
	public void setCause(AID cause) {
		this.cause = cause;
	}
	
}
