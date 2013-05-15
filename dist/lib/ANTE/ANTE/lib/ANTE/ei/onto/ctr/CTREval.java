package ei.onto.ctr;

import jade.content.Predicate;

/**
 * This class represents the predicate "ctr-eval".
 * It is used in response to requests of trust value of a supplier
 * 
 * @author Sérgio
 */
public class CTREval implements Predicate {
	
	private static final long serialVersionUID = 89917307384842696L;
	
	private String agent;
	private double value;
	
	/**
	 * Returns the name of the supplier
	 */
	public String getAgent() {
		return agent;
	}
	
	/**
	 * Sets the name of the supplier
	 * 
	 * @param agent
	 */
	public void setAgent(String agent) {
		this.agent = agent;
	}
	
	/**
	 * Gets the value of trust
	 * 
	 * @return the value
	 */
	public double getValue(){
		return value;
	}
	
	/**
	 * Sets the value of trust
	 * 
	 * @param value
	 */
	public void setValue(double value){
		this.value = value;
	}

}
