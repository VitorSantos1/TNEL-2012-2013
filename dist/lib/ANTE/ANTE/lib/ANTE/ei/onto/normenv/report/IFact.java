package ei.onto.normenv.report;

public class IFact extends IRE {
	
	private static final long serialVersionUID = 89917307384842696L;
	
	private String fact = null;
	
	public String getFact() {
		return fact;
	}
	
	public void setFact(String fact) {
		this.fact = fact;
	}
	
	public String toString() {
		return "ifact (when " + getWhen() + ") (" + fact + ")";
	}
	
}
