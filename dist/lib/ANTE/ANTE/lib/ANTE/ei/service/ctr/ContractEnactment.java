package ei.service.ctr;

import java.util.Collections;
import java.util.Vector;

import ei.onto.ctr.ObligationEvidence;
import ei.service.ctr.OutcomeGenerator.Outcome;
import ei.service.ctr.context.Context;

/**
 * This class represent the struct where a Contract's Enactment is recorded
 * @author Sérgio
 *
 */
public class ContractEnactment{
	
	/**
	 * Contract ended
	 */
	private boolean ended = false;
	
	/**
	 * The agents involved in this contract
	 */
	private Vector<String> agents;
	
	/**
	 * The owner of contract
	 */
	private String owner;
	
	/**
	 * Name of the contract based on Context
	 */
	private String contractID;
	
	/**
	 * Gives information if contract ended successfully
	 */
	private Float outcome;
	
	/**
	 * Saves all obligations of this contract
	 */
	private Vector<ObligationEvidence> obligations;
	
	/**
	 * Information about the transaction 
	 */
	private Vector<Context> products;
	
	/**
	 * Struct that saves index of obligations that were fulfilled
	 * Each Integer added to this vector is the index of the obligation fulfilled
	 */
	private Vector<Integer> fulfillments;
	
	/**
	 * Struct that saves index of obligations that were violated
	 * Each Integer added to this vector is the index of the obligation violated
	 */
	private Vector<Integer> violations;
	
	/**
	 * Struct that saves index of obligations whose deadline was violated
	 * Each Integer added to this vector is the index of the obligation
	 */
	private Vector<Integer> dLineViolations;
	
	/**
	 * Struct that saves an int for each obligation. 1 if obligation was fulfilled, -1 
	 * if was violated, 0 if deadline was violated, or null if it is still active
	 */
	private Vector<OutcomeGenerator.Outcome> state;
	
	/**
	 * Constructor
	 */
	public ContractEnactment()
	{
		this.contractID = "";
		this.outcome = null;
		this.obligations = new Vector<ObligationEvidence>();
		this.fulfillments = new Vector<Integer>();
		this.violations = new Vector<Integer>();
		this.dLineViolations = new Vector<Integer>();
		this.state = new Vector<OutcomeGenerator.Outcome>();
	}

	/**
	 * @return the result
	 */
	public Float getOutcome() {
		return outcome;
	}

	/**
	 * @param name the name to set
	 */
	public void setContractID(String name) {
		this.contractID = name;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return contractID;
	}

	/**
	 * @param outcome the result to set
	 */
	public void setOutcome(Float outcome) {
		this.outcome = outcome;
	}

	/**
	 * @param obligations the obligations to set
	 */
	public void setObligations(Vector<ObligationEvidence> obligations) {
		this.obligations = obligations;
	}

	/**
	 * @return the obligations
	 */
	public Vector<ObligationEvidence> getObligations() {
		return obligations;
	}

	/**
	 * Adds one obligation to obligations vector
	 * @param o
	 */
	public void addObligation(ObligationEvidence o)
	{
		obligations.add(o);
		state.add(null);
	}
	
	public void setFulfillments(Vector<Integer> fulfillments) {
		this.fulfillments = fulfillments;
	}

	public Vector<Integer> getFulfillments() {
		return fulfillments;
	}

	/**
	 * @param violations the violations to set
	 */
	public void setViolations(Vector<Integer> violations) {
		this.violations = violations;
	}

	/**
	 * @return the violations
	 */
	public Vector<Integer> getViolations() {
		return violations;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(Vector<OutcomeGenerator.Outcome> state) {
		this.state = state;
	}

	/**
	 * @return the state
	 */
	public Vector<OutcomeGenerator.Outcome> getState() {
		return state;
	}
	
	/**
	 * Adds a fulfillment
	 * @param ind
	 */
	public void addFulfillment(int ind)
	{
		fulfillments.add(ind);
		Collections.sort(fulfillments);
		if(state.get(ind)!=null && state.get(ind).equals(Outcome.DeadlineViolated))
		{
			return;	
		}
		else
			state.set(ind, OutcomeGenerator.Outcome.Fulfilled);
	}
	
	/**
	 * Adds a deadline violation
	 * @param ind
	 */
	public void addDeadlineViolation(int ind)
	{
		dLineViolations.add(ind);
		Collections.sort(dLineViolations);
		state.set(ind, OutcomeGenerator.Outcome.DeadlineViolated);
	}
	
	/**
	 * Adds a violation
	 * @param ind
	 */
	public void addViolation(int ind)
	{
		violations.add(ind);
		Collections.sort(violations);
		state.set(ind, OutcomeGenerator.Outcome.Violated);
	}

	public void computeOutcome(OutcomeGenerator.MappingMethod method)
	{
		OutcomeGenerator oG = new OutcomeGenerator();
		outcome = oG.computeOutcome(method, state);
	}
	
	public String toString() {
		return super.toString() + ": contract (type " + contractID + ")";
	}

	/**
	 * @param products the products to set
	 */
	public void setContexts(Vector<Context> products) {
		this.products = products;
	}

	/**
	 * @return the good
	 */
	public Vector<Context> getContexts() {
		return products;
	}
	
	/**
	 * @param agents the agents to set
	 */
	public void setAgents(Vector<String> agents) {
		this.agents = agents;
	}

	/**
	 * @return the agents
	 */
	public Vector<String> getAgents() {
		return agents;
	}
	
	/**
	 * Adds one agent to contractEnactment
	 * @param agent
	 */
	public void addAgent(String agent)
	{
		agents.add(agent);
	}
	

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param dLineViolations the dLineViolations to set
	 */
	public void setdLineViolations(Vector<Integer> dLineViolations) {
		this.dLineViolations = dLineViolations;
	}

	/**
	 * @return the dLineViolations
	 */
	public Vector<Integer> getdLineViolations() {
		return dLineViolations;
	}

	/**
	 * @param ended the ended to set
	 */
	public void setEnded(boolean ended) {
		this.ended = ended;
	}

	/**
	 * @return the ended
	 */
	public boolean isEnded() {
		return ended;
	}
	
	public long getFulfillmentTime(ObligationEvidence obl)
	{
		for(int i = 0; i < obligations.size(); i++)
		{
			if(obligations.get(i).getFact().equalsIgnoreCase(obl.getFact()))
			{
				return obligations.get(i).getFulfilled();
			}
		}
		return 0l;
	}
	
	public void fulfillObligation(ObligationEvidence obl, long when)
	{
		for(int i = 0; i < obligations.size(); i++)
		{
			if(obligations.get(i).getFact().equalsIgnoreCase(obl.getFact()))
			{
				obligations.get(i).setFulfilled(when);
				//System.out.println("::::::~~~~~~~~ "+when);
				return;
			}
		}
		return;
	}
}
