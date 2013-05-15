package ei.onto.normenv.report;

import jade.content.Predicate;

public class Report implements Predicate {
	
	private static final long serialVersionUID = -5775213626217243146L;
	
	private String context = null;
	private Long when = null;
	
	public String getContext() {
		return context;
	}
	
	public void setContext(String context) {
		this.context = context;
	}
	
	public Long getWhen() {
		return when;
	}
	
	public void setWhen(Long when) {
		this.when = when;
	}
	
	public String toString() {
		return context;
	}
	
}
