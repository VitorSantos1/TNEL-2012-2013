package ei.onto.normenv.report;

public class ContractStart extends IRE {
	
	private static final long serialVersionUID = -5775213626217243146L;
	
	public String toString() {
		return "contract-start (when " + getWhen() + ")";
	}
	
}
