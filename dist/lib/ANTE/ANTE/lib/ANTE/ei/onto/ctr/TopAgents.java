package ei.onto.ctr;

import java.util.Vector;

import jade.content.Predicate;
import jade.core.AID;

/**
 * This class represents the predicate "top-agents".
 * It is used to senda response to a request for most trustworthy suppliers
 * 
 * @author hlc
 */
public class TopAgents implements Predicate {
	
	private static final long serialVersionUID = 89917307384842696L;

	private boolean useContextual;
	private Vector<AID> agents;
	private Vector<Double> ctrEvaluations;
	private Vector<Double> ctrContextualEvaluations;
	
	/**
	 * Gets a sorted list of Agents
	 */
	public Vector<AID> getAgents() {
		return agents;
	}
	
	/**
	 * Sets a sorted list of Agents
	 * @param agents
	 */
	public void setAgents(Vector<AID> agents) {
		this.agents = agents;
	}

	/**
	 * Sets if contextual fitness is used or not
	 * @param useContextual the useContextual to set
	 */
	public void setUseContextual(boolean useContextual) {
		this.useContextual = useContextual;
	}

	/**
	 * Returns if Contextual Fitness is used or not
	 * @return the useContextual
	 */
	public boolean isUseContextual() {
		return useContextual;
	}

	/**
	 * Sets a list of sorted evaluations of trust
	 * @param ctrEvaluations
	 */
	public void setCtrEvaluations(Vector<Double> ctrEvaluations) {
		this.ctrEvaluations = ctrEvaluations;
	}

	/**
	 * Returns a list of sorted evaluations of trust
	 * @return the eval
	 */
	public Vector<Double> getCtrEvaluations() {
		return ctrEvaluations;
	}

	/**
	 * Sets a list of sorted contextual evaluations of trust
	 * @param ctrContextualEvaluations the ctrContextualEvaluations to set
	 */
	public void setCtrContextualEvaluations(Vector<Double> ctrContextualEvaluations) {
		this.ctrContextualEvaluations = ctrContextualEvaluations;
	}

	/**
	 * Returns a list of sorted contextual evaluations of trust
	 * @return the ctrContextualEvaluations
	 */
	public Vector<Double> getCtrContextualEvaluations() {
		return ctrContextualEvaluations;
	}
}
