package ei.onto.normenv.illocution;

/**
 * @author hlc
 */
public class Collected extends AddresseeIllocution {
	
	private static final long serialVersionUID = 1910171538583320129L;
	
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
