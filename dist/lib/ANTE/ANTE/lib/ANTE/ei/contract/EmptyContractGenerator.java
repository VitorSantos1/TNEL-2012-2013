package ei.contract;

import ei.contract.xml.Contract;
import ei.onto.negotiation.qfnegotiation.AttributeValue;
import ei.onto.negotiation.qfnegotiation.Proposal;

import jade.core.AID;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * This class is a generic contract generator.
 * It generates an XML Contract based on the ei.contract.xml.Contract class obtained with JAXB.
 * 
 * @author hlc
 */
public class EmptyContractGenerator extends ContractGenerator {
	
	public EmptyContractGenerator(AID starterAgent, jade.util.leap.List props) {
		super(starterAgent, props);
	}

	public ContractWrapper generateContract(String xsd_file) {
		// contract
		Contract contract = objFactory.createContract();
		
		// - header
		Contract.Header header = standardContractHeader();
		
		// -- type
		header.setType("_empty-contract");
		
		contract.setHeader(header);
		
		return new ContractWrapper(contract, xsd_file);
	}

}
