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
public class ContractGenerator extends AbstractContractGenerator {
	
	public ContractGenerator(AID starterAgent, jade.util.leap.List props) {
		super(starterAgent, props);
	}
	
	/**
	 * Provides a standard contract header, in which the only contractual-info are product details.
	 * Made available for subclasses because most work in creating the contract header is common. When
	 * extending, subclasses that invoke this method should then change the contract-type attribute and
	 * add contractual-info as needed.
	 * 
	 * @return	A standard contract header created from the winning proposals.
	 */
	protected Contract.Header standardContractHeader() {
		// - header
		Contract.Header header = objFactory.createContractHeader();
		
		// -- date
		try {
			XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
			header.setWhen(calendar);
		} catch(DatatypeConfigurationException dce) {
		}
		
		// -- who
		Contract.Header.Who who = objFactory.createContractHeaderWho();
		List<String> ags = who.getAgent();
		// --- starter
		ags.add(starterAgent.getLocalName());
		// --- further participants
		Vector<AID> included_ags = new Vector<AID>();
		included_ags.add(starterAgent);
		for(int i=0; i<props.size(); i++) {
			boolean already_included = false;
			for(int j=0;j<included_ags.size();j++) {
				if(included_ags.get(j).getLocalName().equals(((Proposal) props.get(i)).getIssuer().getLocalName())) {
					already_included = true;
				}
			}
			if(!already_included) {
				AID aid = ((Proposal) props.get(i)).getIssuer();
				ags.add(aid.getLocalName());
				included_ags.add(aid);
			}
		}
		header.setWho(who);
		
		// -- type
		header.setType("contract");
		
		// -- contractual-info
		List<Contract.Header.ContractualInfo> cinfos = header.getContractualInfo();
		Contract.Header.ContractualInfo cinfo;
		Contract.Header.ContractualInfo.Slot slot;
		List<Contract.Header.ContractualInfo.Slot> slots;
		// for each need
		for(int i=0; i<props.size(); i++) {
			Proposal proposal = (Proposal) props.get(i);
			
			// add need description
			cinfo = objFactory.createContractHeaderContractualInfo();
			cinfo.setName(proposal.getNeedType());
			slots = cinfo.getSlot();
			// - slots for each need attribute
			for(int j=0; j<proposal.getAttributeValues().size(); j++) {
				slot = objFactory.createContractHeaderContractualInfoSlot();
				AttributeValue attributeValueInProposal = (AttributeValue) proposal.getAttributeValues().get(j);
				slot.setName(attributeValueInProposal.getName());
				slot.setValue(attributeValueInProposal.getValue());
				slots.add(slot);
			}
			cinfos.add(cinfo);
		}
		return header;
	}
	
	/**
	 * A very simple contract, with no additional contractual-info.
	 */
	public ContractWrapper generateContract(String xsd_file) {
		// contract
		Contract contract = objFactory.createContract();
		contract.setHeader(standardContractHeader());
		
		return new ContractWrapper(contract, xsd_file);
	}
	
}
