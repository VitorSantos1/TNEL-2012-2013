package ei.sync;

import java.util.Vector;

import ei.service.ctr.OutcomeGenerator;

import jade.util.leap.Serializable;

public class EnactmentOutcome implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String contractId;
	private Vector<String> agents;
	private Vector<OutcomeGenerator.Outcome> outcome;
	
	public EnactmentOutcome(String contractId) {
		this.setContractId(contractId);
		agents = new Vector<String>();
	}

	public void setContractId(String contractId) {
		this.contractId = contractId;
	}

	public String getContractId() {
		return contractId;
	}

	public void setAgents(Vector<String> agents) {
		this.agents = agents;
	}

	public Vector<String> getAgents() {
		return agents;
	}
	
	public void addAgent(String agent) {
		agents.add(agent);
	}

	/**
	 * @param enactment the enactment to set
	 */
	public void setOutcome(Vector<OutcomeGenerator.Outcome> enactment) {
		this.outcome = enactment;
	}

	/**
	 * @return the enactment
	 */
	public Vector<OutcomeGenerator.Outcome> getEnactment() {
		return outcome;
	}

}
