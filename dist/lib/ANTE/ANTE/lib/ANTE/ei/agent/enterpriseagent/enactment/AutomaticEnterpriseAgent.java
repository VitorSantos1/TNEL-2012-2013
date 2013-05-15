package ei.agent.enterpriseagent.enactment;

import ei.agent.enterpriseagent.gui.AutomaticEnterpriseAgentGUI;
import ei.onto.normenv.report.*;

/**
 * This agent will automatically respond to contract events, according to several parameters
 * that can be used to adjust its stochastic contractual behavior.
 * This behavior does not distinguish among different types of obligations.
 * 
 * @author hlc
 */
public class AutomaticEnterpriseAgent extends EnactmentEnterpriseAgent {
	
	private static final long serialVersionUID = -5772457651326537140L;
	
	/*
	 * liveline_violations_denounce_probability: [0,1]
	 * liveline_violations_denounce_min_time: [0,1+]
	 * liveline_violations_denounce_max_time: [0,1+]
	 * 
	 * deadline_violations_denounce_probability: [0,1]
	 * deadline_violations_denounce_min_time: [0,1+]
	 * deadline_violations_denounce_max_time: [0,1+]
	 * 
	 * obligation_fulfillment_probability: [0,1]
	 * obligation_delayed_fulfillment_probability: [0,1]
	 * obligation_fulfillment_min_time: [0,1+]
	 * obligation_fulfillment_max_time: [0,1+], a value >1 means possible delay (even when delayed probability is set to 0)
	 */
	
	private double liveline_violations_denounce_probability = 1.0;
	private double liveline_violations_denounce_min_time = 0.0;
	private double liveline_violations_denounce_max_time = 1.0;
	
	private double deadline_violations_denounce_probability = 1.0;
	private double deadline_violations_denounce_min_time = 0.0;
	private double deadline_violations_denounce_max_time = 1.0;
	
	private double obligation_fulfillment_probability = 1.0;
	private double obligation_delayed_fulfillment_probability = 0.0;
	private double obligation_fulfillment_min_time = 0.0;
	private double obligation_fulfillment_max_time = 1.0;
	
	protected void setup() {
		super.setup();
		
		if(getConfigurationArguments().containsKey("liveline_violations_denounce_probability")) {
			this.liveline_violations_denounce_probability = Double.parseDouble(getConfigurationArguments().get("liveline_violations_denounce_probability").toString());
		}
		if(getConfigurationArguments().containsKey("liveline_violations_denounce_min_time")) {
			this.liveline_violations_denounce_min_time = Double.parseDouble(getConfigurationArguments().get("liveline_violations_denounce_min_time").toString());
		}
		if(getConfigurationArguments().containsKey("liveline_violations_denounce_max_time")) {
			this.liveline_violations_denounce_max_time = Double.parseDouble(getConfigurationArguments().get("liveline_violations_denounce_max_time").toString());
		}

		if(getConfigurationArguments().containsKey("deadline_violations_denounce_probability")) {
			this.deadline_violations_denounce_probability = Double.parseDouble(getConfigurationArguments().get("deadline_violations_denounce_probability").toString());
		}
		if(getConfigurationArguments().containsKey("deadline_violations_denounce_min_time")) {
			this.deadline_violations_denounce_min_time = Double.parseDouble(getConfigurationArguments().get("deadline_violations_denounce_min_time").toString());
		}
		if(getConfigurationArguments().containsKey("deadline_violations_denounce_max_time")) {
			this.deadline_violations_denounce_max_time = Double.parseDouble(getConfigurationArguments().get("deadline_violations_denounce_max_time").toString());
		}
		
		if(getConfigurationArguments().containsKey("obligation_fulfillment_probability")) {
			this.obligation_fulfillment_probability = Double.parseDouble(getConfigurationArguments().get("obligation_fulfillment_probability").toString());
		}
		if(getConfigurationArguments().containsKey("obligation_delayed_fulfillment_probability")) {
			this.obligation_delayed_fulfillment_probability = Double.parseDouble(getConfigurationArguments().get("obligation_delayed_fulfillment_probability").toString());
		}
		if(getConfigurationArguments().containsKey("obligation_fulfillment_min_time")) {
			this.obligation_fulfillment_min_time = Double.parseDouble(getConfigurationArguments().get("obligation_fulfillment_min_time").toString());
		}
		if(getConfigurationArguments().containsKey("obligation_fulfillment_max_time")) {
			this.obligation_fulfillment_max_time = Double.parseDouble(getConfigurationArguments().get("obligation_fulfillment_max_time").toString());
		}
	}
	
	public double getLiveline_violations_denounce_probability() {
		return liveline_violations_denounce_probability;
	}

	public void setLiveline_violations_denounce_probability(
			double liveline_violations_denounce_probability) {
		this.liveline_violations_denounce_probability = liveline_violations_denounce_probability;
	}

	public double getLiveline_violations_denounce_min_time() {
		return liveline_violations_denounce_min_time;
	}

	public void setLiveline_violations_denounce_min_time(
			double liveline_violations_denounce_min_time) {
		this.liveline_violations_denounce_min_time = liveline_violations_denounce_min_time;
	}

	public double getLiveline_violations_denounce_max_time() {
		return liveline_violations_denounce_max_time;
	}

	public void setLiveline_violations_denounce_max_time(
			double liveline_violations_denounce_max_time) {
		this.liveline_violations_denounce_max_time = liveline_violations_denounce_max_time;
	}

	public double getDeadline_violations_denounce_probability() {
		return deadline_violations_denounce_probability;
	}

	public void setDeadline_violations_denounce_probability(
			double deadline_violations_denounce_probability) {
		this.deadline_violations_denounce_probability = deadline_violations_denounce_probability;
	}

	public double getDeadline_violations_denounce_min_time() {
		return deadline_violations_denounce_min_time;
	}

	public void setDeadline_violations_denounce_min_time(
			double deadline_violations_denounce_min_time) {
		this.deadline_violations_denounce_min_time = deadline_violations_denounce_min_time;
	}

	public double getDeadline_violations_denounce_max_time() {
		return deadline_violations_denounce_max_time;
	}

	public void setDeadline_violations_denounce_max_time(
			double deadline_violations_denounce_max_time) {
		this.deadline_violations_denounce_max_time = deadline_violations_denounce_max_time;
	}

	public double getObligation_fulfillment_probability() {
		return obligation_fulfillment_probability;
	}

	public void setObligation_fulfillment_probability(double obligation_fulfillment_probability) {
		this.obligation_fulfillment_probability = obligation_fulfillment_probability;
	}

	public double getObligation_delayed_fulfillment_probability() {
		return obligation_delayed_fulfillment_probability;
	}

	public void setObligation_delayed_fulfillment_probability(double obligation_delayed_fulfillment_probability) {
		this.obligation_delayed_fulfillment_probability = obligation_delayed_fulfillment_probability;
	}

	public double getObligation_fulfillment_min_time() {
		return obligation_fulfillment_min_time;
	}

	public void setObligation_fulfillment_min_time(
			double obligation_fulfillment_min_time) {
		this.obligation_fulfillment_min_time = obligation_fulfillment_min_time;
	}

	public double getObligation_fulfillment_max_time() {
		return obligation_fulfillment_max_time;
	}

	public void setObligation_fulfillment_max_time(
			double obligation_fulfillment_max_time) {
		this.obligation_fulfillment_max_time = obligation_fulfillment_max_time;
	}

	/**
	 * Handles a contract start report. In this implementation, this method just returns <code>false</code>.
	 * 
	 * @param contractStart
	 * @return
	 */
	protected boolean handleContractStartReport(ContractStart contractStart) {
		return false;
	}
	
	/**
	 * Handles a contract end report. In this implementation, this method just returns <code>false</code>.
	 * 
	 * @param contractEnd
	 * @return
	 */
	protected boolean handleContractEndReport(ContractEnd contractEnd) {
		return false;
	}

	/**
	 * Handles an obligation report. In this implementation, if this agent is the obligation's bearer, it will consider fulfilling
	 * according to the following optional arguments:
	 * <ul>
	 * 	<li> obligation_fulfillment_probability: a value in [0,1], default is 1.0
	 * 	<li> obligation_delayed_fulfillment_probability: a value in [0,1], default is 0.0
	 * 	<li> obligation_fulfillment_min_time: a value in [0,1+], default is 0.0
	 * 	<li> obligation_fulfillment_max_time: a value in [0,1+], default is 1.0
	 * </ul>
	 * 
	 * @param obl	The obligation report
	 * @return		<code>true</code> if this agent is the obligation's bearer and chooses to fulfill the obligation;
	 * 				<code>false</code> otherwise
	 */
	protected boolean handleObligationReport(Obligation obl) {
		// check if this agent is the obligation's bearer
		if(!obl.getBearer().equals(this.getLocalName())) {
			return false;
		}
		
		if(Math.random() < this.obligation_fulfillment_probability) {
			long relative_deadline = obl.getDeadline() - System.currentTimeMillis();
			double min_bound = relative_deadline * obligation_fulfillment_min_time;   // min bound of interval
			double max_bound = relative_deadline * obligation_fulfillment_max_time;   // max bound of interval
			double fulfillment_time = Math.random() * (max_bound - min_bound) + min_bound;
			
			if(Math.random() < this.obligation_delayed_fulfillment_probability) {
				// delayed fulfillment
				fulfillment_time += relative_deadline;
			}
			
			addBehaviour(new FulfillObligationWakerBehaviour(this, obl, (long) fulfillment_time));
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Handles a liveline violation report. In this implementation, if this agent is the obligation's counterparty, it will consider denouncing
	 * according to the following optional arguments:
	 * <ul>
	 * 	<li> liveline_violations_denounce_probability: a value in [0,1], default is 1.0
	 * 	<li> liveline_violations_denounce_min_time: a value in [0,1+], default is 0.0
	 * 	<li> liveline_violations_denounce_max_time: a value in [0,1+], default is 1.0
	 * </ul>
	 * 
	 * @param lviol	The liveline violation to denounce
	 * @return		<code>true</code> if this agent is the obligation's counterparty and chooses to denounce the liveline violation;
	 * 				<code>false</code> otherwise
	 */
	protected boolean handleLivelineViolationReport(LivelineViolation lviol) {
		// check if this agent is the obligation's counterparty
		if(!lviol.getObligation().getCounterparty().equals(this.getLocalName())) {
			return false;
		}
		
		if(Math.random() < this.liveline_violations_denounce_probability) {
			// take as a reference the time left for the liveline
			long time_for_liveline = lviol.getObligation().getLiveline() - System.currentTimeMillis();
			double min_bound = time_for_liveline * liveline_violations_denounce_min_time;   // min bound of interval
			double max_bound = time_for_liveline * liveline_violations_denounce_max_time;   // max bound of interval
			double denounce_time = Math.random() * (max_bound - min_bound) + min_bound;

			addBehaviour(new DenounceLivelineViolationWakerBehaviour(this, lviol, (long) denounce_time));
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Handles a deadline violation report. In this implementation, if this agent is the obligation's counterparty, it will consider denouncing
	 * according to the following optional arguments:
	 * <ul>
	 * 	<li> deadline_violations_denounce_probability: a value in [0,1], default is 1.0
	 * 	<li> deadline_violations_denounce_min_time: a value in [0,1+], default is 0.0
	 * 	<li> deadline_violations_denounce_max_time: a value in [0,1+], default is 1.0
	 * </ul>
	 * 
	 * @param dviol	The deadline violation to denounce
	 * @return		<code>true</code> if this agent is the obligation's counterparty and chooses to denounce the deadline violation;
	 * 				<code>false</code> otherwise
	 */
	protected boolean handleDeadlineViolationReport(DeadlineViolation dviol) {
		// check if this agent is the obligation's counterparty
		if(!dviol.getObligation().getCounterparty().equals(this.getLocalName())) {
			return false;
		}
		
		if(Math.random() < this.deadline_violations_denounce_probability) {
		// take as a reference the size of the window for obligation fulfillment
		long obligation_window = dviol.getObligation().getDeadline() - dviol.getObligation().getWhen();
		
		double min_bound = obligation_window * deadline_violations_denounce_min_time;   // min bound of interval
		double max_bound = obligation_window * deadline_violations_denounce_max_time;   // max bound of interval
		double denounce_time = Math.random() * (max_bound - min_bound) + min_bound;
		
		addBehaviour(new DenounceDeadlineViolationWakerBehaviour(this, dviol, (long) denounce_time));
		return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Handles a denounce report. In this implementation, this method just returns <code>false</code>.
	 * 
	 * @param denounce
	 * @return
	 */
	protected boolean handleDenounceReport(Denounce denounce) {
		return false;
	}
	
	/**
	 * Handles a violation report. In this implementation, this method just returns <code>false</code>.
	 * 
	 * @param violation
	 * @return
	 */
	protected boolean handleViolationReport(Violation violation) {
		return false;
	}
	
	/**
	 * Handles a fulfillment report. In this implementation, this method just returns <code>false</code>.
	 * 
	 * @param fulf	The fulfillment report
	 * @return		<code>true</code> if this agent does something in response to this report;
	 * 				<code>false</code> otherwise
	 */
	protected boolean handleFulfillmentReport(Fulfillment fulf) {
		return false;
	}

	/**
	 * A GUI that adds a new tab with the agent's enactment configuration
	 */
	protected boolean createGUI() {
		gui = new AutomaticEnterpriseAgentGUI(this);
		gui.setLocationRelativeTo(null);

		return true;
	}

}
