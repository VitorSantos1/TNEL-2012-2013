package ei.onto.ctr;

import jade.content.Concept;

/**
 * This class represents the concept of Obligation
 * @author Sérgio
 *
 */
public class ObligationEvidence implements Concept{
	
	private static final long serialVersionUID = 89917307384842696L;
	
	private String bearer = null;
	private String counterparty = null;
	private String fact = null;
	private Long liveline = null;
	private Long deadline = null;
	private long fulfilled = 0;
	
	public ObligationEvidence(ei.onto.normenv.report.Obligation o)
	{
		this.bearer = o.getBearer();
		this.counterparty = o.getCounterparty();
		this.fact = o.getFact();
		this.liveline = o.getLiveline();
		this.deadline = o.getDeadline();
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

	/**
	 * @param fulfilled the fulfilled to set
	 */
	public void setFulfilled(long fulfilled) {
		this.fulfilled = fulfilled;
	}

	/**
	 * @return the fulfilled
	 */
	public long getFulfilled() {
		return fulfilled;
	}
	
}
