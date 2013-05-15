package ei.service.negotiation.qfnegotiation;

import jade.core.AID;
import jade.util.leap.Serializable;

public class NegotiationOutcome implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private double utility;
	private double fine;	
	private double riskTolerance;
	private int numberOfRefusedSuppliers;
	
	private double lastRoundAvgUtility;
	private double lastRoundUtilityStDev;
	
	private AID winner;
	private String contractId;
	
	public NegotiationOutcome() {
	}

	public NegotiationOutcome(double utility, double fine, int numberOfRefusedSuppliers, double lastRoundAvgUtility, double lastRoundUtilityStDev, AID winner, String contractId, double riskTolerance) {

		this.utility = utility;
		this.fine = fine;
		this.numberOfRefusedSuppliers = numberOfRefusedSuppliers;
		this.lastRoundAvgUtility = lastRoundAvgUtility;
		this.lastRoundUtilityStDev = lastRoundUtilityStDev;		
		this.winner = winner;
		this.contractId = contractId;
		this.riskTolerance = riskTolerance;
	}
	
	public double getUtility() {
		return utility;
	}
	public void setUtility(double utility) {
		this.utility = utility;
	}
	public double getLastRoundAvgUtility() {
		return lastRoundAvgUtility;
	}

	public void setFine(double fine) {
		this.fine = fine;
	}

	public double getFine() {
		return fine;
	}

	public void setLastRoundAvgUtility(double lastRoundAvgUtility) {
		this.lastRoundAvgUtility = lastRoundAvgUtility;
	}
	
	public double getLastRoundUtilityStDev() {
		return lastRoundUtilityStDev;
	}

	public void setLastRoundUtilityStDev(double lastRoundUtilityStDev) {
		this.lastRoundUtilityStDev = lastRoundUtilityStDev;
	}

	public AID getWinner() {
		return winner;
	}
	
	public void setWinner(AID winner) {
		this.winner = winner;
	}
	
	public void setContractId(String contractId) {
		this.contractId = contractId;
	}
	
	public String getContractId() {
		return contractId;
	}
	
	public void setRiskTolerance(double riskTolerance) {
		this.riskTolerance = riskTolerance;
	}

	public double getRiskTolerance() {
		return riskTolerance;
	}

	public void setNumberOfRefusedSuppliers(int numberOfRefusedSuppliers) {
		this.numberOfRefusedSuppliers = numberOfRefusedSuppliers;
	}

	public int getNumberOfRefusedSuppliers() {
		return numberOfRefusedSuppliers;
	}
}
