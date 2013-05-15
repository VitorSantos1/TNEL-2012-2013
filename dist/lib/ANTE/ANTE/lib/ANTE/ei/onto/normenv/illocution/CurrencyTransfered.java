package ei.onto.normenv.illocution;

/**
 * @author hlc
 */
public class CurrencyTransfered extends ThirdPartyIllocution {
	
	private static final long serialVersionUID = -5319691131362008935L;
	
	private String ref;
	private double amount;
	
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
	
}
