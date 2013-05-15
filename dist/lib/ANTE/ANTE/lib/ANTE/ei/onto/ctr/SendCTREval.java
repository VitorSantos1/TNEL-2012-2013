package ei.onto.ctr;

import ei.service.ctr.OutcomeGenerator.MappingMethod;


/**
 * This class represents the action "send-ctr-eval".
 * 
 * @author Sérgio
 */
public class SendCTREval extends CTRAgentAction {
	
	private static final long serialVersionUID = -5775213626217243146L;
	private boolean context;
	private ContextualEvidence contract;
	private MappingMethod mapMet;
	
	/**
	 * Sets if Context is used or not
	 * @param context the context to set
	 */
	public void setContext(boolean context) {
		this.context = context;
	}
	
	/**
	 * Returns if Context is used or not
	 * @return the context
	 */
	public boolean isContext() {
		return context;
	}
	
	/**
	 * Sets the Context to trust evaluation
	 * @param contract the contract to set
	 */
	public void setContract(ContextualEvidence contract) {
		this.contract = contract;
	}
	
	/**
	 * Sets the Context to trust evaluation
	 * @return the contract
	 */
	public ContextualEvidence getContract() {
		return contract;
	}

	/**
	 * Sets the mapping method to be used in CTR evaluation
	 * @param mapMet the mapMet to set
	 */
	public void setMapMet(MappingMethod mapMet) {
		this.mapMet = mapMet;
	}

	/**
	 * Returns the mapping method to be used in CTR evaluation
	 * @return the mapMet
	 */
	public MappingMethod getMapMet() {
		return mapMet;
	}
}
