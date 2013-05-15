package ei.onto.ctr;

import java.util.List;

import ei.service.ctr.ContractEnactment;
import ei.service.ctr.EvidenceInfo;

import jade.content.Predicate;

/**
 * This class represents the information needed to show in Enterprise GUI
 * 
 * @author Sérgio
 */
public class Info implements Predicate {
	
	private static final long serialVersionUID = 89917307384842696L;
	
	private String agent;
	private int nContracts;
	private int nObl;
	private int nFulfilledObl;
	private int nViolatedObl;
	private List<ContractEnactment> contracts;
	private List<EvidenceInfo> evidences;
	private Double ctrEvaluation;
	
	/**
	 * Sets number of Contracts of this Agent
	 * @param nContracts the nContracts to set
	 */
	public void setNContracts(int nContracts) {
		this.nContracts = nContracts;
	}

	/**
	 * Returns number of Contracts of this Agent
	 * @return the nContracts
	 */
	public int getNContracts() {
		return nContracts;
	}

	/**
	 * Sets number of Fulfilled Obligations of this Agent
	 * @param nFulfilledObl the nFulfilledObl to set
	 */
	public void setNFulfilledObl(int nFulfilledObl) {
		this.nFulfilledObl = nFulfilledObl;
	}

	/**
	 * Returns number of Fulfilled Obligations of this Agent
	 * @return the nFulfilledObl
	 */
	public int getNFulfilledObl() {
		return nFulfilledObl;
	}

	/**
	 * Sets number of Obligations of this Agent
	 * @param nObl the nObl to set
	 */
	public void setNObl(int nObl) {
		this.nObl = nObl;
	}

	/**
	 * Returns number of Obligations of this Agent
	 * @return the nObl
	 */
	public int getNObl() {
		return nObl;
	}

	/**
	 * Sets number of Violated Obligations of this Agent
	 * @param nViolatedObl the nViolatedObl to set
	 */
	public void setNViolatedObl(int nViolatedObl) {
		this.nViolatedObl = nViolatedObl;
	}

	/**
	 * Returns number of Violated Obligations of this Agent
	 * @return the nViolatedObl
	 */
	public int getNViolatedObl() {
		return nViolatedObl;
	}

	/**
	 * Sets the list of contracts
	 * @param contracts the contracts to set
	 */
	public void setContracts(List<ContractEnactment> contracts) {
		this.contracts = contracts;
	}

	/**
	 * Returns the list of contracts
	 * @return the contracts
	 */
	public List<ContractEnactment> getContracts() {
		return contracts;
	}

	/**
	 * Sets a trust value
	 * @param ctrEvaluation the ctrEvaluation to set
	 */
	public void setCtrEvaluation(Double ctrEvaluation) {
		this.ctrEvaluation = ctrEvaluation;
	}

	/**
	 * Returns the trust value
	 * @return the ctrEvaluation
	 */
	public Double getCtrEvaluation() {
		return ctrEvaluation;
	}

	/**
	 * Sets a list of Evidences to build graphs
	 * @param evidences the evidences to set
	 */
	public void setEvidences(List<EvidenceInfo> evidences) {
		this.evidences = evidences;
	}

	/**
	 * Returns a list of Evidences to build graphs
	 * @return the evidences
	 */
	public List<EvidenceInfo> getEvidences() {
		return evidences;
	}

	/**
	 * Sets the name of the Agent
	 * @param agent the agent to set
	 */
	public void setAgent(String agent) {
		this.agent = agent;
	}

	/**
	 * Returns the name of the Agent
	 * @return the agent
	 */
	public String getAgent() {
		return agent;
	}
}
