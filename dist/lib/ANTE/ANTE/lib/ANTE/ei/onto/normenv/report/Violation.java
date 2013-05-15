package ei.onto.normenv.report;

public class Violation extends IRE {
	
	private static final long serialVersionUID = 89917307384842696L;
	
	private Obligation obligation = null;
	
	public Violation() {	
	}
	
	public Violation(String context, Long when, Obligation obligation) {
		this.setContext(context);
		this.setWhen(when);
		this.setObligation(obligation);
	}
	
	public void setObligation(Obligation obligation) {
		this.obligation = obligation;
	}

	public Obligation getObligation() {
		return obligation;
	}

	public String toString() {
		return "viol (when " + getWhen() + ") (" + obligation + ")";
	}
	
}
