package ei.onto.normenv.report;

public class DeadlineViolation extends IRE {
	
	private static final long serialVersionUID = 89917307384842696L;
	
	private Obligation obligation = null;
	
	public DeadlineViolation() {	
	}
	
	public DeadlineViolation(String context, Long when, Obligation obligation) {
		this.setContext(context);
		this.setWhen(when);
		this.obligation = obligation;
	}
	
	public void setObligation(Obligation obligation) {
		this.obligation = obligation;
	}

	public Obligation getObligation() {
		return obligation;
	}

	public String toString() {
		return "deadline-viol (when " + getWhen() + ") (" + obligation + ")";
	}
	
}
