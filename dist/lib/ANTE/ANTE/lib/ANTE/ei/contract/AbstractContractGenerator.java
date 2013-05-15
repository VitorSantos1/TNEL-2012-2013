package ei.contract;

import jade.core.AID;
import ei.contract.xml.ObjectFactory;

/**
 * This abstract class represents a contract generator. The class is provided with information regarding the outcome of a negotiation process
 * and implementations are supposed to generate the corresponding XML contract, wrapped within <code>ContractWrapper</code>.
 * Extensions of this class represent generators for specific contract types.
 * 
 * @author hlc
 */
public abstract class AbstractContractGenerator {
	
	protected jade.util.leap.List props;	//array with the proposals that won the negotiation
	protected AID starterAgent;	//the AID for the agent who requested the negotiation
	
	// the object factory, used for creating contract contents
	protected ObjectFactory objFactory;
	
	public AbstractContractGenerator(AID starterAgent, jade.util.leap.List props) {
		this.starterAgent = starterAgent;
		this.props = props;
		
		// start object factory
		objFactory = new ObjectFactory();
	}
	
	/**
	 * Generates an XML Contract.
	 * 
	 * @return	The XML Contract, which should be valid according to the contract schema definition.
	 */
	public abstract ContractWrapper generateContract(String xsd_file);
	
}
