package ei.onto.normenv.report;

public class Fulfillment extends IRE {
	
	private static final long serialVersionUID = 89917307384842696L;
	
	private Obligation obligation = null;
	private IFact ifact = null;
	
	public Fulfillment() {	
	}
	
	public Fulfillment(String context, Long when, Obligation obligation, IFact ifact) {
		this.setContext(context);
		this.setWhen(when);
		this.obligation = obligation;
		this.ifact = ifact;
	}
	
	public void setObligation(Obligation obligation) {
		this.obligation = obligation;
	}

	public Obligation getObligation() {
		return obligation;
	}

	public void setIfact(IFact ifact) {
		this.ifact = ifact;
	}

	public IFact getIfact() {
		return ifact;
	}

	public String toString() {
		return "fulf (when " + getWhen() + ") (" + obligation + ")";
	}
	
}
