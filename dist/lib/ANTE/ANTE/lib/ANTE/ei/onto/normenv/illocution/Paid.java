package ei.onto.normenv.illocution;

/**
 * @author hlc
 */
public class Paid extends IssuerIllocution {
	
	private static final long serialVersionUID = 2144599611429220393L;
	
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
