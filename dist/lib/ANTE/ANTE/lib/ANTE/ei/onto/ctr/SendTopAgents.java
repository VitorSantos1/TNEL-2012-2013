package ei.onto.ctr;

import java.util.Vector;

import ei.service.ctr.OutcomeGenerator.MappingMethod;


import jade.content.AgentAction;
import jade.core.AID;

/**
 * This class represents the action "send-top-agents".
 * It is used to request a list of the most trustworthy agents to CTR service
 * 
 * @author SergioMoura
 */
public class SendTopAgents implements AgentAction {
	
	private static final long serialVersionUID = -5775213626217243146L;
	
	public static final int ALL = -1;
	public static final int NO_HANDICAP = -2;
	
	private int topNumberOfAgents;
	private boolean useContextual;
	private MappingMethod mapMet;
	private ContextualEvidence contract;
	private Vector<AID> interestedAgents; 
	private AID requester;
	/**

	 * Sets if Contextual Fitness is used or not
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
	 * Sets the Context for Trust evaluation
	 * @param contract the contract to set
	 */
	public void setContract(ContextualEvidence contract) {
		this.contract = contract;
	}

	/**
	 * Returns the Context for Trust evaluation
	 * @return the contract
	 */
	public ContextualEvidence getContract() {
		return contract;
	}

	/**
	 * Sets the filter
	 * @param topNumberOfAgents the topNumberOfAgents to set
	 */
	public void setTopNumberOfAgents(int topNumberOfAgents) {
		this.topNumberOfAgents = topNumberOfAgents;
	}

	/**
	 * Returns the filter 
	 * @return the topNumberOfAgents
	 */
	public int getTopNumberOfAgents() {
		return topNumberOfAgents;
	}

	/**
	 * Sets the list of agents to filter
	 * @param interestedAgents
	 */
	public void setInterestedAgents(Vector<AID> interestedAgents) {
		this.interestedAgents = interestedAgents;
	}

	/**
	 * Returns the list of agents to filter
	 */
	public Vector<AID> getInterestedAgents() {
		return interestedAgents;
	}

	/**
	 * Sets the mapping method to trust evaluation
	 * @param mapMet the mapMet to set
	 */
	public void setMapMet(MappingMethod mapMet) {
		this.mapMet = mapMet;
	}

	/**
	 * Returns the mapping method to trust evaluation
	 * @return the mapMet
	 */
	public MappingMethod getMapMet() {
		return mapMet;
	}

	/**
	 * Sets the AID of Requester
	 * @param requester
	 */
	public void setRequester(AID requester) {
		this.requester = requester;
	}

	/**
	 * Returns the AID of Requester
	 */
	public AID getRequester() {
		return requester;
	}
	
}
