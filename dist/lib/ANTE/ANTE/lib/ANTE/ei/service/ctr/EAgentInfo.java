package ei.service.ctr;

import java.util.Vector;

import ei.onto.ctr.ObligationEvidence;
import ei.onto.normenv.report.Obligation;
import ei.service.ctr.EvidenceInfo;
import ei.service.ctr.OutcomeGenerator.MappingMethod;
import ei.service.ctr.context.Context;

/**
 * Class that provides a structure to record Agent's CTR information (Number of Contracts, trust value, contracts' enactment, obligations,...) 
 * 
 * @author Sérgio
 *
 */
public class EAgentInfo {
	// the agent's AID
	private String agent;
	// number of signed contracts
	private int nContracts;
	// number of obligations
	private int nObl;
	// number of fulfilled obligations
	private int nFulfObl;
	// number of violated obligations
	private int nViolObl;
	// number of violated deadlines
	private int nDeadlineViol;

	private double sinAlphaValue;
	
	public static final double INITIAL_TRUSTWORTHINESS_VALUE = 0.2; // was 0.0 

	//All contracts with this agent
	private Vector<ContractEnactment> contractHistoric;

	//All reports that interest for trust calculation
	private Vector<EvidenceInfo> evidencesInfo;

	public EAgentInfo(String agent) {
		this.agent = agent;
		nContracts = 0;
		nFulfObl = 0;
		nViolObl = 0;
		contractHistoric = new Vector<ContractEnactment>();
		sinAlphaValue = INITIAL_TRUSTWORTHINESS_VALUE;
		evidencesInfo = new Vector<EvidenceInfo>();
	}


	/**
	 * Returns the name of agent whose information belongs to
	 * @return
	 */
	public String getAgent() {
		return agent;
	}

	/**
	 * Sets the number of contracts this agent did
	 * @param nContracts
	 */
	public void setNContracts(int nContracts) {
		this.nContracts = nContracts;
	}

	/**
	 * Adds one more contract to contracts' list and increments the number of contracts of this agent
	 * @param c
	 */
	public void addContract(ContractEnactment c) {
		contractHistoric.add(c);
	}

	/**
	 * Returns the number of contracts this agent did
	 * @return
	 */
	public int getNContracts() {
		return nContracts;
	}

	/**
	 * Sets the number of contracts this agent fulfilled
	 * @param nFulfObl
	 */
	public void setNFulfObl(int nFulfObl) {
		this.nFulfObl = nFulfObl;
	}

	/**
	 * Adds one obligation to the list of fulfilled obligations and increments the number of fulfilled obligations
	 * @param o
	 * @param contract
	 */
	public void addFulfObl(Obligation o, String contract, Long when) {
		ObligationEvidence oE = new ObligationEvidence(o);
		for(int i = 0; i < contractHistoric.size(); i++)
		{
			if(contractHistoric.get(i).getName().equalsIgnoreCase(contract))
			{
				ContractEnactment c = contractHistoric.get(i);
				Vector<ObligationEvidence> vec = c.getObligations();
				int n = -1;
				for(int j = 0; j < vec.size(); j++)
				{
					if(vec.get(j).getFact().equalsIgnoreCase(o.getFact()))
						n = j;
				}

				if(n == -1)
				{
					contractHistoric.get(i).addObligation(oE);
					contractHistoric.get(i).addFulfillment(vec.size()-1);
				}
				else
					contractHistoric.get(i).addFulfillment(n);

				c.fulfillObligation(oE, when);
			}
		}
		nFulfObl++;
	}

	/**
	 * Adds one obligation to the list of deadline violated obligations and increments the number of deadline violated obligations
	 * @param o
	 * @param contract
	 */
	public void addDlineViolObl(Obligation o, String contract) {
		ObligationEvidence oE = new ObligationEvidence(o);
		for(int i = 0; i < contractHistoric.size(); i++)
		{
			if(contractHistoric.get(i).getName().equalsIgnoreCase(contract))
			{
				ContractEnactment c = contractHistoric.get(i);
				Vector<ObligationEvidence> vec = c.getObligations();
				int n = -1;
				for(int j = 0; j < vec.size(); j++)
				{
					if(vec.get(j).getFact().equalsIgnoreCase(o.getFact()))
						n = j;
				}

				if(n == -1)
				{
					contractHistoric.get(i).addObligation(oE);
					contractHistoric.get(i).addDeadlineViolation(vec.size()-1);
				}
				else
					contractHistoric.get(i).addDeadlineViolation(n);
			}
		}
		nDeadlineViol++;
	}

	/**
	 * Adds one obligation to contract
	 * @param o
	 */
	public void addObligation(Obligation o)
	{
		ObligationEvidence oE = new ObligationEvidence(o);
		String cont = o.getContext();
		for(int i = 0; i < contractHistoric.size(); i++)
		{
			if(contractHistoric.get(i).getName().equalsIgnoreCase(cont))
				contractHistoric.get(i).addObligation(oE);
		}
	}

	/**
	 * Returns the number of obligations this agent fulfilled
	 * @return
	 */
	public int getNFulfObl() {
		return nFulfObl;
	}

	/**
	 * Sets the number of obligations this agent violated
	 * @param nViolObl
	 */
	public void setNViolObl(int nViolObl) {
		this.nViolObl = nViolObl;
	}

	/**
	 * Adds one obligation to the list of violated obligations and increments the number of violated obligations
	 * @param o
	 * @param contract
	 */
	public void addViolObl(Obligation o, String contract) {
		for(int i = 0; i < contractHistoric.size(); i++)
		{
			if(contractHistoric.get(i).getName().equalsIgnoreCase(contract))
			{
				ContractEnactment c = contractHistoric.get(i);
				Vector<ObligationEvidence> vec = c.getObligations();
				int n = 0;
				for(int j = 0; j < vec.size(); j++)
				{
					if(vec.get(j).getFact().equalsIgnoreCase(o.getFact()))
						n = j;
				}
				contractHistoric.get(i).addViolation(n);
			}
		}
		nViolObl++;
	}

	/**
	 * Returns the number of obligations this agent violated
	 * @return
	 */
	public int getNViolObl() {
		return nViolObl;
	}

	/**
	 * Returns the value of trust for this conditions(option, context)
	 * @param contextual
	 * @param c
	 * @param ARFF
	 * @return
	 */
	public double getCTREval(boolean contextual, ei.onto.ctr.ContextualEvidence c, String ARFF) {
		if(contextual) {
			double handicap = hasHandicap(c, ARFF);
			return sinAlphaValue*handicap;
		} else {
			return sinAlphaValue;
		}
	}

	/**
	 * Returns if this agent has handicap in this context (c)
	 * @param c
	 * @param ARFF
	 * @return
	 */
	public double hasHandicap(ei.onto.ctr.ContextualEvidence c, String ARFF)
	{
		Vector<ContractualHistory> vec = getContractualHistory(contractHistoric);
		Stereotype s = new Stereotype(vec, ARFF);
		double d = 0;
		try {
			d = s.extractStereotype(c, agent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return d;
	}

	/**
	 * Returns all contracts' enactments to calculate the value of contextual trust
	 * @param rR
	 * @return
	 */
	private Vector<ContractualHistory> getContractualHistory(Vector<ContractEnactment> rR) 
	{
		Vector<ContractualHistory> vec = new Vector<ContractualHistory>();
		for(int i = 0; i < rR.size(); i++)
		{
			Vector<Context> needs = rR.get(i).getContexts();
			Vector<String> products = new Vector<String>();
			for(int k = 0; k < needs.size(); k++)
			{
				products.add(needs.get(k).getName());
			}
			for(int j = 0; j < products.size(); j++)
			{
				ContractualHistory c = new ContractualHistory(rR.get(i), products.get(j));
				vec.add(c);
			}
		}
		return vec;
	}

	/**
	 * Sets the list of contracts' enactments
	 * @param contractHistoric
	 */
	public void setContractHistoric(Vector<ContractEnactment> contractHistoric) {
		this.contractHistoric = contractHistoric;
	}

	/**
	 * Returns the list of contracts' enactments
	 * @return
	 */
	public Vector<ContractEnactment> getContractHistoric() {
		return contractHistoric;
	}

	/**
	 * Updates the trust value with this mapping method
	 * @param mapMet
	 */
	public void refresh(OutcomeGenerator.MappingMethod mapMet)
	{	
		if(contractHistoric.size()>0)
		{
			EvidenceInfo eI = new EvidenceInfo();
			eI.setContracID(contractHistoric.get(contractHistoric.size()-1).getName());
			computeOutcome(mapMet);
			SinAlphaModel sinAlpha = new SinAlphaModel(agent ,false);
			sinAlpha.initTrust(contractHistoric);

			sinAlphaValue = sinAlpha.getValue();
			eI.setTrustValue(sinAlphaValue);
			evidencesInfo.add(eI);
		}
	}

	private void computeOutcome(MappingMethod mapMet) {
		for(ContractEnactment cE: contractHistoric)
		{
			if(cE.isEnded())
			{
				cE.computeOutcome(mapMet);
			}
		}
	}


	/**
	 * Sets the number of obligations
	 * @param nObl
	 */
	public void setNObl(int nObl) {
		this.nObl = nObl;
	}

	/**
	 * Returns the number of obligations
	 * @return
	 */
	public int getNObl() {
		return nObl;
	}

	/**
	 * Sets the list of evidences to build evolution graphs on GUI
	 * @param r
	 */
	public void setReports(Vector<EvidenceInfo> r)
	{
		this.evidencesInfo = r;
	}

	/**
	 * Returns the list of evidences to build evolution graphs on GUI
	 * @return
	 */
	public Vector<EvidenceInfo> getReports()
	{
		return this.evidencesInfo;
	}

	/**
	 * Adds one evidence
	 */
	public void addReport(EvidenceInfo r)
	{
		evidencesInfo.add(r);
	}


	/**
	 * Sets the number of deadline violated obligations
	 * @param nDlineViolObl the nDlineViolObl to set
	 */
	public void setnDlineViolObl(int nDlineViolObl) {
		this.nDeadlineViol = nDlineViolObl;
	}


	/**
	 * Returns the number of deadline violated obligations
	 * @return the nDlineViolObl
	 */
	public int getnDlineViolObl() {
		return nDeadlineViol;
	}

	public void addNContracts()
	{
		nContracts++;
	}
}