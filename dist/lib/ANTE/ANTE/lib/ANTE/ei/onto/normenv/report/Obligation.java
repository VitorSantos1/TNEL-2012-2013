package ei.onto.normenv.report;

public class Obligation extends IRE {
	
	private static final long serialVersionUID = 89917307384842696L;
	
	private String id = null;
	private String bearer = null;
	private String counterparty = null;
	private String fact = null;
	private Long liveline = null;
	private Long deadline = null;
	private Double fine = null;
	
	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getBearer() {
		return bearer;
	}
	
	public void setBearer(String bearer) {
		this.bearer = bearer;
	}
	
	public String getCounterparty() {
		return counterparty;
	}
	
	public void setCounterparty(String counterparty) {
		this.counterparty = counterparty;
	}
	
	public String getFact() {
		return fact;
	}
	
	public void setFact(String fact) {
		this.fact = fact;
	}
	
	public void setLiveline(Long liveline) {
		this.liveline = liveline;
	}

	public Long getLiveline() {
		return liveline;
	}

	public Long getDeadline() {
		return deadline;
	}
	
	public void setDeadline(Long deadline) {
		this.deadline = deadline;
	}

	public void setFine(Double fine) {
		this.fine = fine;
	}

	public Double getFine() {
		return fine;
	}

	public String toString() {
		return "obl (id " +  getId() + ") (when " + getWhen() + ") (bearer " + bearer + ") (counterparty " + counterparty +
				") (fact " + fact + ") (liveline " + liveline + ") (deadline " + deadline + ")";
	}
	
	public boolean equals(Object obj) {
		if(!this.getId().equals(((Obligation) obj).getId())) {
			return false;
		}
		if(!this.getContext().equals(((Obligation) obj).getContext())) {
			return false;
		}
		if(!(this.getWhen() == ((Obligation) obj).getWhen())) {
			return false;
		}
		if(!this.getBearer().equals(((Obligation) obj).getBearer())) {
			return false;
		}
		if(!this.getCounterparty().equals(((Obligation) obj).getCounterparty())) {
			return false;
		}
		if(!this.getFact().equals(((Obligation) obj).getFact())) {
			return false;
		}
		if(!(this.getLiveline() == ((Obligation) obj).getLiveline())) {
			return false;
		}
		if(!(this.getDeadline() == ((Obligation) obj).getDeadline())) {
			return false;
		}
		if(!(this.getFine() == ((Obligation) obj).getFine())) {
			return false;
		}

		return true;
	}
	
}
