package ei.onto.normenv.illocution;

import jade.content.Predicate;

/**
 * @author hlc
 */
public class Illocution implements Predicate {
	
	private static final long serialVersionUID = 8131689847207424694L;
	
	private String context;
	private long when;
	
	public String getContext() {
		return context;
	}
	
	public void setContext(String context) {
		this.context = context;
	}
	
	public long getWhen(){
		return when;
	}
	
	public void setWhen(long when){
		this.when = when;
	}
	
}
