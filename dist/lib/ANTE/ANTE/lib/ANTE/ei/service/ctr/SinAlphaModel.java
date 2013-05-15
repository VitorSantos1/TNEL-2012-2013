/**
 * 
 */
package ei.service.ctr;

import java.util.Vector;

/**
 * Class that represents the Sin Alpha Model to calculate Trust
 * 
 * @author J.U.
 *
 */
public class SinAlphaModel {
	
	private String agent;
	int SUCCESS = 1;
	boolean useContextual;
	double value;
	private double alphaSin;
	
	// fraction that divides the circle into PI/fr portions
	static double OMEGA = Math.PI/12;
	// function zeros
	static double a = 0.1;
  	// y saturation
	static double by = 0.5;
	// increments of alpha in successful scenarios
	static double POSITIVE_INCREMENT = 1;
	// decrements of alpha in successful scenarios
	static double NEGATIVE_INCREMENT = -1.5;
	double alpha;
	
	public SinAlphaModel(String agent, boolean useContextual)
	{
		this.agent = agent;
		this.useContextual = useContextual;
	}
	
	/**
	 * Returns true if contextual will be use or false if it will not
	 */
	public boolean useContext()
	{
		return useContextual;
	}
	
	/**
	 * Gets the value of trust
	 */
	protected double getValue()
	{
		return value;
	}
	
	protected void initTrust(Vector<ContractEnactment> rr)
	{
		double lambda = 1;	// incremental factor
		alpha = 3*Math.PI/2;

		// current supplier has no classifications
		if (rr.size() == 0)  {
			value = 0.0;
			return;
		}
		// current supplier has some classifications
		// evaluate CFP
		// evaluate each one of the classifications of current partner
		for (int i=0; i < rr.size (); i++) {
			// determine the increment to alpha
			if(rr.get(i).getOutcome() == null)
				lambda = 0.0;
			else
				lambda = rr.get(i).getOutcome();
			/*if (rr.elementAt(i).getOutcome().equals(outcomes.Fulfilled)) {
				lambda = POSITIVE_INCREMENT;
			}
			else {
				lambda = NEGATIVE_INCREMENT;
			}*/

			
			// evaluate new alpha
			if (alpha + lambda*OMEGA <= 3*Math.PI/2)
				alpha = 3*Math.PI/2;
			else if (alpha + lambda*OMEGA >= 5*Math.PI/2)
				alpha = 5*Math.PI/2;
			else alpha = alpha + lambda*OMEGA; 
		} // for
			

/* ----------------------------- recency ---------------------------------		
		// get last N classifications (recency of results)
		int lastNClassf = 10;
		int oldClassf = classf.size () - lastNClassf;
		if (oldClassf > 0) {
			for (int c = 0; c < oldClassf; c++)
				classf.removeElementAt(0);
		}

		// compute weighted average of last N classifications
		double wi, wivi;
		double lambda = 5;
		double totalwi = 0, totalwivi = 0;
		double lastN = 0;
		for (int i = 0; i < classf.size(); i++) {
			wi = Math.exp(-(lastNClassf-1-i)/lambda);
			totalwi += wi;
			if (classf.elementAt(i) == 1)
				wivi = wi;
			else wivi = 0;
			totalwivi += wivi;
		}
		
		lastN = totalwivi / totalwi;
		
		// debugging to sinAlpha log
		utils.printResults("] - " + utils.round2Decimal(((by * Math.sin (alpha)+0.5)*lastN), 2), true, logFile);
		//utils.printResults("] - " + utils.round2Decimal((by * Math.sin (alpha)+0.5), 2), true, logFile);
		
		// 0.5 is added to get in [0, 1]
		return (((by * Math.sin (alpha)+0.5))*lastN);

 ----------------------------- recency --------------------------------- */

		// debugging to sinAlpha log
		// utils.printResults("] - " + utils.round2Decimal(((by * Math.sin (alpha)+0.5)), 2), true, logFile);
		
		// 0.5 is added to get in [0, 1]
		value = ((by * Math.sin (alpha)+0.5));
		alphaSin = alpha;
	}
	
	/**
	 * Updates the value of trust, with this new contract
	 * @param contract
	 */
	public void updateRep(ContractEnactment contract)
	{
		double lambda = 1;	// incremental factor

			// determine the increment to alpha
			/*if (contract.getOutcome().equals(outcomes.Fulfilled)) {
				lambda = POSITIVE_INCREMENT;
			}
			else {
				lambda = NEGATIVE_INCREMENT;
			}*/
			lambda = contract.getOutcome();
			// evaluate new alpha
			if (alphaSin + lambda*OMEGA <= 3*Math.PI/2)
				alphaSin = 3*Math.PI/2;
			else if (alphaSin + lambda*OMEGA >= 5*Math.PI/2)
				alphaSin = 5*Math.PI/2;
			else alphaSin = alphaSin + lambda*OMEGA;
			
			value = ((by * Math.sin (alphaSin)+0.5));
	}
	
	/**
	 * Returns the agent 
	 * 
	 * @return the agent
	 */
	public String getAgent() {
		return agent;
	}
	
}
