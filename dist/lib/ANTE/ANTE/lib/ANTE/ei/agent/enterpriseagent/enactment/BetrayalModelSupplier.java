package ei.agent.enterpriseagent.enactment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import ei.agent.enterpriseagent.ontology.Attribute;
import ei.agent.enterpriseagent.ontology.Competence;
import ei.contract.ContractWrapper;
import ei.contract.xml.Contract;
import ei.onto.negotiation.qfnegotiation.CompetenceCall;
import ei.onto.normenv.report.*;


/**
 * This supplier agent responds to contract events according to the
 * Betrayal Model, described in paper: Joana Urbano, Henrique Lopes Cardoso, 
 * Ana Paula Rocha, Eugénio Oliveira, “Trust and Normative Control in Multi-Agent 
 * Systems”, Advances in Distributed Computing and Artificial Intelligence 
 * Journal, Vol. I, No. 1, July 2012, pp. 43-52 (2012)
 * 
 * @author Patricia Alves
 * 		   Nov 2012 - Jan 2013
 *
 */
public class BetrayalModelSupplier extends AutomaticEnterpriseAgent {
	
	
	/** ***************************************************************
	 * 							ATTRIBUTES	
	 * ****************************************************************/

	private static final long serialVersionUID = 5927957828601037615L;
	
	/**
	 * The supplier's integrity (low: 0.1, medium: 0.5, high: 0.9). Corresponds
	 * to delta in the paper.
	 */
	private float integrity = 0.9f;
	
	/**
	 * The supplier's ability ([0.5, 1]).
	 */
	private float ability = 0.7f;
	
	/**
	 * The supplier's betray propensity (if integrity is low: 0.5, 
	 * if integrity is medium: 0.3, if integrity is high: 0.1). 
	 * Must be between 0 and 1, and corresponds to
	 * ro in paper, and is inversely proportional to his integrity.
	 */
	private float betrayPropensity;
	
	/**
	 * The contract currently selected to betray.
	 */
	private Contract contractToBetray = null;
	
	/**
	 * Percentage of the utility associated with the new proposal from
	 * which the sanction is considered irrelevant. Corresponds to gamma in
	 * the paper (gamma1: 0.0).
	 */
	private float sanctionIrrelevant1 = 0.0f;
	
	/**
	 * Percentage of the utility associated with the new proposal from
	 * which the sanction is considered irrelevant. Corresponds to gamma in
	 * the paper (gamma2: 0.2).
	 */
	private float sanctionIrrelevant2 = 0.2f;
	
	/**
	 * Minimum number of past contracts with the potencial victim
	 * needed to assess the relationship. Corresponds to lambda in the
	 * paper (2 by default).
	 */
	private int minNumPastContracts = 2;
	
	/**
	 * Last units of time in ms. Corresponds to sigma in the paper.
	 * See class AutoRequestModeTicker on 
	 * /ANTE_Platform/src/ei/agent/enterpriseagent/AutomaticClient.java	 * 
	 */
	private long lastTimeUnits = 10000; //30 days in ms (43200000)
	
	
	/**
	 * Minimum of active contracts in which the supplier needs to be
	 * engaged to assess the relationship. See equation 6 in the paper
	 * (1 by default).
	 */
	private int minNumActiveContracts = 1;

	
	/**
	 * Supplier betray probalities, based on the comparison of the
	 * relationship values and the betrayal value (check paper pp. 47,
	 * table 4).
	 */
													//		BetrayalBenefit
													//	High	Medium	Low		// RelationshipValue
	private static float[][] betrayProbabilities = {{	0.5f, 	0.0f, 	0.0f	},	// High
													 {	1.0f, 	0.2f, 	0.0f	},	// Medium
													 {	1.0f, 	0.5f, 	0.0f	}};	// Low
	
	
	public enum ValueBetrayal {High, Medium, Low};
	public enum ValueRelationship {High, Medium, Low};
	
	
	/** ***************************************************************
	 * 								METHODS	
	 * ****************************************************************/
	
	protected void setup() {
		
		super.setup();

		if (getConfigurationArguments().containsKey("integrity")) {
			integrity = Float.parseFloat(getConfigurationArguments().getProperty("integrity"));
		}
		
		//calculates the suppliers betray propensity according to his integrity
		calcBetrayPropensity();
		
		if (getConfigurationArguments().containsKey("minNumPastContracts")) {
			minNumPastContracts = Integer.parseInt(getConfigurationArguments().getProperty("minNumPastContracts"));
		}
		
		if (getConfigurationArguments().containsKey("minNumActiveContracts")) {
			minNumActiveContracts = Integer.parseInt(getConfigurationArguments().getProperty("minNumActiveContracts"));
		}
		
		if (getConfigurationArguments().containsKey("ability")) {
			ability = Float.parseFloat(getConfigurationArguments().getProperty("ability"));
		}
		
		if (getConfigurationArguments().containsKey("sanctionIrrelevant1")) {
			sanctionIrrelevant1 = Float.parseFloat(getConfigurationArguments().getProperty("sanctionIrrelevant1"));
		}
		
		if (getConfigurationArguments().containsKey("sanctionIrrelevant2")) {
			sanctionIrrelevant2 = Float.parseFloat(getConfigurationArguments().getProperty("sanctionIrrelevant2"));
		}
		
		if (getConfigurationArguments().containsKey("lastTimeUnits")) {
			lastTimeUnits = Long.parseLong(getConfigurationArguments().getProperty("lastTimeUnits"));
		}
		

	} //setup
	
	
	/**
	 * Empty Constructor
	 */
	/*BetrayalModelSupplier()
	{
	}*/
	
	
	/** OK!
	 * Calculates the supplier's betray propensity.
	 * (if integrity is low: 0.5, if integrity is medium: 0.3, if 
	 * integrity is high: 0.1). Corresponds to ro in paper.
	 */
	public void calcBetrayPropensity()
	{
		if (integrity == 0.1) //low integrity
			betrayPropensity = 0.5f;
		else if (integrity == 0.5) //medium integrity
			betrayPropensity = 0.3f;
		else
			betrayPropensity = 0.1f; //high integrity
	}
	
	
	/** OK!
	 * Returns an hashmap with the supplier's active contracts.
	 */
	public HashMap<String, ContractWrapper> getActiveContracts() {
	
		HashMap<String, ContractWrapper> activeContracts =
				new HashMap<String, ContractWrapper>();
		
		for (Entry<String, Vector<Report>> e : contractReports.entrySet())
		{
			Vector<Report> reports = e.getValue(); 
			
			if (reports.elementAt(0) instanceof NewContract) 
			{
				//if the last vector element isnt a contractEnd, then 
				//the contract is still active
				if ( !(reports.elementAt(reports.size()-1) instanceof ContractEnd) )
				{
					activeContracts.put(e.getKey(), contracts.get(e.getKey()));
				}
			}
		}				
		
		return activeContracts;
		
	} //getActiveContracts
	
	
	
	
	/** OK!
	 * Returns the utility of the active contract with the id "contractID".
	 * @param contractID - active contract ID to be assessed.
	 * @return Active contract utility.
	 */
	public float contractUtility(String contractID) {
		
		//obtains the active contracts
		HashMap<String, ContractWrapper> activeContracts = getActiveContracts();
		
		//gets the contract using the id received as argument
		Contract c = activeContracts.get(contractID).getContract();
		
		float quantity = 0; //quantity in the contract
		float unitPrice = 0; //price per unit
		String productName = "";
		
		
		//gets the contract information
		List<Contract.Header.ContractualInfo> cinfos = c.getHeader().getContractualInfo();
		String contractType = "";
		
		for (int i = 0; i < cinfos.size(); i++)
		{
			contractType =  cinfos.get(i).getName(); System.out.println("contractType: " + contractType);
			
			//TODO attention because it may not be "contract-of-sale-data"
			//if ( cinfos.get(i).getName().equals("contract-of-sale-data") )
			if ( contractType.equals("asaig-reg-data") )
			{
				List<Contract.Header.ContractualInfo.Slot> slots = 
														cinfos.get(i).getSlot();
				
				for (int j = 0; j < slots.size(); j++) 
				{
					if (slots.get(j).getName().equals("quantity")) 
					{
						quantity = Float.parseFloat(slots.get(j).getValue());						
					}
					
					if (slots.get(j).getName().equals("unit-price")) 
					{
						unitPrice = Float.parseFloat(slots.get(j).getValue());						
					}
					
					if (slots.get(j).getName().equals("product")) 
					{
						productName = slots.get(j).getValue();						
					}
				}
			}
		}
		
		
		Vector<Competence> competences = getCompetences();
		int stock = 0;
		float maxPrice = 0;
		
		//gets the available stock (the one not committed to other contracts)
		//for the product
		for (int i = 0; i < competences.size(); i++)
		{
			if ( competences.get(i).getType().equals(productName) )
			{
				stock = competences.get(i).getStock();
				
				Attribute priceRange = competences.get(i).getAttribute("price");
				
				maxPrice = (Float)priceRange.getContinuousDomainMax();
				
			}
		}

		if ( contractType.equals("asaig-reg-data") )
			return (quantity / stock) * (unitPrice / maxPrice);
		else
			return 0; //FIXME ver o que acontece qdo sao outros contractos
		
				
	} //contractUtility
		

	
	/** OK!
	 * Returns the new proposal utility (received in the cfp).
	 * @param proposal - the new proposal.
	 * @return New proposal utility.
	 */
	public float proposalUtility(CompetenceCall compCall, Competence competence){
	
		//the stock is obtained from the suppliers corresponding competence
		int stock = competence.getStock();

		Attribute quantity = compCall.getNeed().getAttribute("quantity");
		float quant = Float.parseFloat(quantity.getPreferredValue().toString());
		
		Attribute priceRange = compCall.getNeed().getAttribute("price");
		float unitPrice = Float.parseFloat(priceRange.getPreferredValue().toString());
		float maxPrice = (Float)priceRange.getContinuousDomainMax();
		
		return (quant / stock) * (unitPrice / maxPrice);
				
	} //proposalUtility
	
	
	
	/**
	 * OK!
	 * Gets the active contract sanction value (fine).
	 * This value is calculated in <code>computeFine</code>
	 * /ANTE_Platform/src/ei/proto/negotiation/qfnegotiation/QFNegotiationInit.java
	 * @param contractID
	 * @return The active contract sanction value.
	 */
	public float getActiveContractSanction(String contractID) {
		
		//obtains this supplier's active contracts
		HashMap<String, ContractWrapper> activeContracts = getActiveContracts();
		
		//gets the contract using the id received as argument
		Contract c = activeContracts.get(contractID).getContract();
		
		float sanction = 0; //sanction to be applied in the contract
		
		
		//gets the contract information
		List<Contract.Header.ContractualInfo> cinfos = c.getHeader().getContractualInfo();
		
		for (int i = 0; i < cinfos.size(); i++)
		{
			//passou a ser o asaig-reg-data pq e' ai que esta a ser usada a fine (sanction)
			//if ( cinfos.get(i).getName().equals("contract-of-sale-data") )
			if ( cinfos.get(i).getName().equals("asaig-reg-data") ) //FIXME (e se for outro tipo de contracto?)
			{
				List<Contract.Header.ContractualInfo.Slot> slots = 
														cinfos.get(i).getSlot();
				
				for (int j = 0; j < slots.size(); j++) 
				{
					if (slots.get(j).getName().equals("fine")) 
					{
						sanction = Float.parseFloat(slots.get(j).getValue());						
					}
				}
			}
		}
		
		return sanction;
		
	} //getActiveContractSanction
	
	

	/** 
	 * The supplier assesses the current situation everytime he identifies
	 * a new opportunity (when he receives a new proposal), and therefore
	 * will consider betraying one of his active contracts, according to
	 * his betrayPropensity. This implies assessing the benefits of betraying 
	 * (<code>betrayalBenefit()</code>) and the relationship (<code>relationshipValue()</code>).
	 * Returns true id the contract is deemed to betray, false otherwise.
	 */
	public boolean assessCurrentSituation(CompetenceCall compCall, Competence competence, String contractID) {
		
		ValueBetrayal betrayBenefit;
		ValueRelationship valueRelationship;
		float betrayProbability = 0;

		//Assesses the benefits of betraying the contract and the relationship 
		//with the corresponding bearer. The contract with higher probability is
		//chosen for betray.
		betrayBenefit = valueBetrayal(contractID, compCall, competence);
		valueRelationship = valueRelationship(contractID);
		
		betrayProbability = betrayProbabilities[valueRelationship.ordinal()][betrayBenefit.ordinal()];
		
		//if the random value is less than the betrayProbability
		//then will betray that contract
		if (Math.random() <= betrayProbability) 
		{
			//betrays the contract
			return true;
		}

		return false;	
		
	} //assessCurrentSituation
	
	
	
	/** 
	 * Calculates the (benefit) value of betraying the active contract,
	 * according to the new proposal received in the cfp.
	 * @return BetrayalBenefit.Low if low, BetrayalBenefit.Medium if medium, 
	 * BetrayalBenefit.High if high. By default returns BetrayalBenefit.Low.
	 */
	public ValueBetrayal valueBetrayal(String contractID, CompetenceCall compCall, Competence competence) {
		
		float sanction = getActiveContractSanction(contractID);
		
		float res = contractUtility(contractID) - sanction - proposalUtility(compCall, competence);
		ValueBetrayal valueBetrayal = ValueBetrayal.Low;
		
		//low benefit
		if ( (sanctionIrrelevant1 / (1 - integrity)) < res )
		{
			valueBetrayal = ValueBetrayal.Low;
		}
		//medium benefit
		else if ( (sanctionIrrelevant1 / (1 - integrity)) < res &&
				res < (sanctionIrrelevant2 / (1 - integrity))) 
		{
			valueBetrayal = ValueBetrayal.Medium;
		}
		//high benefit
		else if ( (sanctionIrrelevant2 / (1 - integrity)) < res)
		{
			valueBetrayal = ValueBetrayal.High;
		}

		return valueBetrayal;
		
	} //betrayalBenefit
	
	
	/** OK!
	 * Returns the number of past contracts with the potencial victim
	 * in the last units of time.
	 * @param agentName - name of the proponing client agent.
	 * @return Number of past contracts in the last units of time.
	 */
	public int numPastContracts(String agentName) {
		
		int numPastContracts = 0;
		
		//iterates through all the reports related to each contract the supplier made
		//searches for contracts already ended with the client passed as argument
		for (Vector<Report> reports : contractReports.values()) 
		{
			if (reports.elementAt(0) instanceof NewContract) 
			{
				if (((NewContract) reports.elementAt(0)).getAgents().contains(agentName) &&
						reports.elementAt(reports.size()-1) instanceof ContractEnd &&
					//((NewContract) reports.elementAt(0)).getWhen() == (System.currentTimeMillis() - lastTimeUnits) )
					((NewContract) reports.elementAt(0)).getWhen() <= (System.currentTimeMillis() - lastTimeUnits) )
				{
					numPastContracts++; System.out.println("numPastContracts: " + numPastContracts);

				}
			}
		}	
		
		return numPastContracts;
		
	} //numPastContracts
	
	
	/** OK!
	 * Calculates the continuity perspective with the potencial victim based 
	 * on the number of past contracts.
	 * If the numPastContracts is equal or greater than the minNumPastContracts returns true
	 * (high perspective). Otherwise returns false (not high).
	 * @return
	 */
	public boolean continuityPerspective(String agentName) {
		
		/*
		 * Searches for the past contracts in the last units of time
		 * and compares to the wanted minimum number of past contracts.
		 */
		if ( numPastContracts(agentName) >= minNumPastContracts )
		{
			return true;
		}
		
		return false;		
		
	} //continuityPerspective
	
	
	/**
	 * Checks if the supplier has at least minNumActiveContracts active contracts.
	 * @return True if has at least minNumActiveContracts active contracts. 
	 * False otherwise.
	 */
	public boolean hasMinActiveContracts() {
		
		int numActiveContracts = 0;
		//obtains the active contracts
		HashMap<String, ContractWrapper> activeContracts = getActiveContracts();
		
		numActiveContracts = activeContracts.size();

		if (numActiveContracts >= minNumActiveContracts)
			return true;
				
		return false;
		
	} //hasMinActiveContracts
	
	
	/**
	 * Calculates the relationship value of the active contract with the potential, 
	 * victim according to equations 5 and 6, and table 1.
	 * @return RelationshipValue.low if the relationship value is low, 
	 * RelationshipValue.medium if medium, RelationshipValue.high if high.
	 * By default returns RelationshipValue.high;
	 */
	public ValueRelationship valueRelationship(String contractID) {
		
		//obtains the active contracts
		HashMap<String, ContractWrapper> activeContracts = getActiveContracts();
		
		//gets the potiential victim contract using the id received as argument
		Contract c = activeContracts.get(contractID).getContract();
		
		//checks if the supplier has a minimum number
		//of active contracts, according to minNumActiveContracts
		//corresponds to HasOtherContracts in the paper
		boolean hasMinActiveContracts = this.hasMinActiveContracts();
		ValueRelationship relValue = ValueRelationship.High;
		
		//gets the agents involved in the contract (it assumes there are only 2)
		List<String> involvedAgents = c.getHeader().getWho().getAgent();
		String clientName = "";
		
		for (int i = 0; i < involvedAgents.size(); i++)
		{
			//the client name can't be this agent name
			if (! involvedAgents.get(i).equals(this.getLocalName()))
				clientName = involvedAgents.get(i);
		}		
		
		if ( continuityPerspective(clientName) && hasMinActiveContracts )
			relValue = ValueRelationship.Medium;
		else if ( continuityPerspective(clientName) && !hasMinActiveContracts)
			relValue = ValueRelationship.High;
		else if ( !continuityPerspective(clientName) && hasMinActiveContracts)
			relValue = ValueRelationship.Low;
		else if ( !continuityPerspective(clientName) && !hasMinActiveContracts)
			relValue = ValueRelationship.Medium;
		
		return relValue;

	} //relationshipValue
	
	
	
	/**
	 * To compare contract's utility in ascending order.
	 */
	public class MyContractsUtilityComparable implements Comparator<String>{
		 
	    @Override
	    public int compare(String c1, String c2) {
	        
	    	return (contractUtility(c1) < contractUtility(c2) ? -1 : 
	    		(contractUtility(c1) == contractUtility(c2) ? 0 : 1));
	    }
	    
	} //MyContractsUtilityComparable
	
	
	
	
	
	/**
	 * Sorts the contracts by increasing utility.
	 */
	public List<String> orderContractsByUtility(Set<String> activeContracts) {
		
		Iterator<String> it = activeContracts.iterator();
		
		List<String> contracts = new ArrayList<String>();
		
		while (it.hasNext())
		{
			contracts.add(it.next());
		}
		
		Collections.sort(contracts, new MyContractsUtilityComparable());

		return contracts;
		
	} //orderContractsByUtility
	
	
	
	/**
	 * Returns the allocated resources for the specified contract
	 * @param contractID
	 * @return Allocated resources for the specified contract
	 */
	public int allocatedResources(Contract c) {
		
		
		
		return 0;
	}
	
	
	/**
	 * Decides if the supplier agent enters the negotiation based on the 
	 * received proposal: business oportunity. The agent will probabilistically
	 * consider betraying one of his active contracts: betray propensity (ro).
	 * With a probability
	 * This decision is based on the assessment made by calling the functions 
	 * <code>assessCurrentSituation</code>.
	 */
	public boolean decideOnEnteringNegotiation(CompetenceCall compCall, Competence competence) {
		
		//first the supplier evaluates the free resources for the competence 
		//(competence.getStock()) and compares with the resources of each 
		//active contract and with the proposal needed resources
		int freeResources = competence.getStock(); System.out.println(this.getLocalName() + " freeResources: "+ freeResources);
		Attribute quantity = compCall.getNeed().getAttribute("quantity");
		float resourcesNeeded = Float.parseFloat(quantity.getPreferredValue().toString()); System.out.println(this.getLocalName()+ " resourcesNeeded: "+ resourcesNeeded);
		
		//gets the active contracts
		HashMap<String, ContractWrapper> hm = getActiveContracts();
					
		//gets the active contracts' id
		Set<String> activeContracts = hm.keySet();
		
		//sorts the contracts from the less to the most useful
		List<String> orderedContracts = orderContractsByUtility(activeContracts);
		
		//list of contracts marked for assessment
		List<String> markedContracts = new ArrayList<String>();
		String contractID, contractToBetrayID;
		
		//1st checks the contracts that can be marked for assessment
		for (int i = 0; i < orderedContracts.size(); i++)
		{
			contractID = orderedContracts.get(i);
			
			//gets the respective contract from the active contracts hashmap
			Contract c = hm.get(contractID).getContract();
			
			//checks the contracts that can be marked for assessment
			if (freeResources + allocatedResources(c) > resourcesNeeded)
			{
				//adds the contract to the marked contracts list
				markedContracts.add(contractID);
			}
		}
		
		
		if (! markedContracts.isEmpty())
		{System.out.println("Existem contratos marcados para decidir de trai ou nao");
			for (int i = 0; i < markedContracts.size(); i++)
			{
				//2nd, considers betraying according to his betrayPropensity
				//i.e., if random value is lower that his betrayPropensity
				//will assess the current situation and consider betraying
				if (Math.random() < betrayPropensity)
				{
					//assesses the current situation for the iterated marked contract
					//if the contract is considered as marked for betraying
					//will betray it, since the contracts list was already order by less utility.
					if ( assessCurrentSituation(compCall, competence, markedContracts.get(i)) );
					{
						//chooses this current contract as the contract to betray
						contractToBetrayID = markedContracts.get(i);
						System.out.println(this.getLocalName() + " VAI TRAIR UM CONTRACTO: " +contractToBetrayID);
						contractToBetray = hm.get(contractToBetrayID).getContract();
												
						freeResources += allocatedResources(contractToBetray);
						
						//updates the supplier stock
						competence.setStock(freeResources);
						
						System.out.println(this.getLocalName() + " decidiu entrar na negociacao");
						
						return true; //stops the iteration
						
					}
				}
			}
		}
		
		
		//if no contract is deemed to be betrayed, the supplier may still
		//accept the new contract if he has enough free resources
		//paper pp. 47, section 2.2.2
		if (freeResources > resourcesNeeded)
		{ System.out.println(this.getLocalName() + " decidiu entrar na negociacao por ter freeResources");
			return true;
		}
		
		System.out.println(this.getLocalName() + " NAO decidiu entrar na negociacao");
		return false;
		
	} //decideOnEnteringNegotiation



	protected boolean handleObligationReport(Obligation obl) {
		
		System.out.println("obligation: "+obl);
		System.out.println("bearer: "+obl.getBearer());
		System.out.println("Context: "+obl.getContext());
		
		// check if this agent is the obligation's bearer
		if (!obl.getBearer().equals(this.getLocalName()))
			return false;
		
		
		//if there's a contract to betray, the supplier won't fulfill the obligation
		if (contractToBetray != null)
		{		System.out.println("contractToBetray: "+contractToBetray.getHeader().getId());
			if (contractToBetray.getHeader().getId().equals(obl.getContext())) 
				return false;
			else
			{
				System.out.println("handleObligationReport error: Couldn't find the ID of the contractToBetray");
				return false;
			}
		}
		
		//otherwise it depends on the ability of the supplier

		//violates the contract
		if (Math.random() > this.ability)
			return false;
		
		return fulfillObligation(obl);
		
	} //handleObligationReport

	
	
	

}
