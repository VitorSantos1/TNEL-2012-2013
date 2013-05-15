package ei.contract;

import ei.contract.xml.Contract;
import ei.onto.negotiation.qfnegotiation.AttributeValue;
import ei.onto.negotiation.qfnegotiation.Proposal;

import jade.core.AID;

import java.util.List;

/**
 * A generator for a contract of sale.
 * 
 * @author hlc
 */
public class ContractOfSaleGenerator extends ContractGenerator {
	
	public ContractOfSaleGenerator(AID starterAgent, jade.util.leap.List props) {
		super(starterAgent, props);
	}
	
	public ContractWrapper generateContract(String xsd_file) {
		// contract
		Contract contract = objFactory.createContract();
		
		// - header
		Contract.Header header = standardContractHeader();
		
		// -- type
		header.setType("contract-of-sale");
		
		// -- contractual-info
		List<Contract.Header.ContractualInfo> cinfos = header.getContractualInfo();
		Contract.Header.ContractualInfo cinfo;
		Contract.Header.ContractualInfo.Slot slot;
		List<Contract.Header.ContractualInfo.Slot> slots;
		// for each need
		for(int i=0; i<props.size(); i++) {
			Proposal proposal = (Proposal) props.get(i);
			
			// add contract-of-sale-data
			cinfo = objFactory.createContractHeaderContractualInfo();
			cinfo.setName("contract-of-sale-data");
			slots = cinfo.getSlot();
			
			// - seller
			slot = objFactory.createContractHeaderContractualInfoSlot();
			slot.setName("seller");
			slot.setValue(proposal.getIssuer().getLocalName());
			slots.add(slot);
			// - buyer
			slot = objFactory.createContractHeaderContractualInfoSlot();
			slot.setName("buyer");
			slot.setValue(starterAgent.getLocalName());
			slots.add(slot);
		    // - product
			slot = objFactory.createContractHeaderContractualInfoSlot();
			slot.setName("product");
			slot.setValue(proposal.getNeedType());
			slots.add(slot);
			
			AttributeValue attributeValueInProposal;
		    // - quantity
			attributeValueInProposal = proposal.getAttributeValue("quantity");
			if(attributeValueInProposal != null) {
				slot = objFactory.createContractHeaderContractualInfoSlot();
				slot.setName("quantity");
				slot.setValue(attributeValueInProposal.getValue());
				slots.add(slot);
			}
		    // - unit-price
			attributeValueInProposal = proposal.getAttributeValue("price");
			if(attributeValueInProposal != null) {
				slot = objFactory.createContractHeaderContractualInfoSlot();
				slot.setName("unit-price");
				slot.setValue(attributeValueInProposal.getValue());
				slots.add(slot);
			}
			
			cinfos.add(cinfo);
		}
		contract.setHeader(header);
		
		return new ContractWrapper(contract, xsd_file);
	}
	
}
