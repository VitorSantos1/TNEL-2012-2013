package ei.contract;

import ei.contract.xml.Contract;
import ei.onto.negotiation.qfnegotiation.AttributeValue;
import ei.onto.negotiation.qfnegotiation.Proposal;

import jade.core.AID;

import java.util.List;

/**
 * @author hlc
 */
public class AsaigPlusGenerator extends ContractGenerator {
	
	public AsaigPlusGenerator(AID starterAgent, jade.util.leap.List props) {
		super(starterAgent, props);
	}
	
	public ContractWrapper generateContract(String xsd_file) {
		// contract
		Contract contract = objFactory.createContract();
		
		// - header
		Contract.Header header = standardContractHeader();
		
		// -- type
		header.setType("asaig-plus");
		
		// -- contractual-info
		List<Contract.Header.ContractualInfo> cinfos = header.getContractualInfo();
		Contract.Header.ContractualInfo cinfo;
		Contract.Header.ContractualInfo.Slot slot;
		List<Contract.Header.ContractualInfo.Slot> slots;
		// for each component (should be only 1)
		for(int i=0; i<props.size(); i++) {
			Proposal prop = (Proposal) props.get(i);
			
			// add asaig-plus-data
			cinfo = objFactory.createContractHeaderContractualInfo();
			cinfo.setName("asaig-plus-data");
			slots = cinfo.getSlot();
			
			// - seller
			slot = objFactory.createContractHeaderContractualInfoSlot();
			slot.setName("seller");
			slot.setValue(prop.getIssuer().getLocalName());
			slots.add(slot);
			// - buyer
			slot = objFactory.createContractHeaderContractualInfoSlot();
			slot.setName("buyer");
			slot.setValue(starterAgent.getLocalName());
			slots.add(slot);
			// - product
			slot = objFactory.createContractHeaderContractualInfoSlot();
			slot.setName("product");
			slot.setValue(prop.getNeedType());
			slots.add(slot);
			
			AttributeValue attributeValueInProposal;
			// - quantity
			attributeValueInProposal = prop.getAttributeValue("quantity");
			if(attributeValueInProposal != null) {
				slot = objFactory.createContractHeaderContractualInfoSlot();
				slot.setName("quantity");
				slot.setValue(attributeValueInProposal.getValue());
				slots.add(slot);
			}
			// - unit-price
			attributeValueInProposal = prop.getAttributeValue("price");
			if(attributeValueInProposal != null) {
				slot = objFactory.createContractHeaderContractualInfoSlot();
				slot.setName("unit-price");
				slot.setValue(attributeValueInProposal.getValue());
				slots.add(slot);
			}
			// - delivery time
			attributeValueInProposal = prop.getAttributeValue("delivery");
			if(attributeValueInProposal != null) {
				slot = objFactory.createContractHeaderContractualInfoSlot();
				slot.setName("delivery-rel-deadline");
//				slot.setValue(attributeValueInProposal.getValue());
				int deliveryRelDeadline = Integer.parseInt(attributeValueInProposal.getValue());
				slot.setValue(String.valueOf(deliveryRelDeadline));
				slots.add(slot);
			}
			
			// - delivery dviol sanction
			slot = objFactory.createContractHeaderContractualInfoSlot();
			slot.setName("delivery-dviol-sanction");
			slot.setValue(String.valueOf(prop.getFine()));
			slots.add(slot);
			
			cinfos.add(cinfo);
		}
		contract.setHeader(header);
		
		return new ContractWrapper(contract, xsd_file);
	}
	
}
