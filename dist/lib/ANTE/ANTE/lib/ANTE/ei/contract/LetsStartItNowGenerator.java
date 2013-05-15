package ei.contract;

import ei.contract.xml.Contract;
import ei.onto.negotiation.qfnegotiation.AttributeValue;
import ei.onto.negotiation.qfnegotiation.Proposal;

import jade.core.AID;

import java.util.List;

/**
 * @author hlc
 */
public class LetsStartItNowGenerator extends ContractGenerator {
	
	public LetsStartItNowGenerator(AID starterAgent, jade.util.leap.List props) {
		super(starterAgent, props);
	}
	
	public ContractWrapper generateContract(String xsd_file) {
		// contract
		Contract contract = objFactory.createContract();
		
		// - header
		Contract.Header header = standardContractHeader();
		
		// -- type
		header.setType("lets-start-it-now");
		
		// -- contractual-info
		List<Contract.Header.ContractualInfo> cinfos = header.getContractualInfo();
		Contract.Header.ContractualInfo cinfo;
		Contract.Header.ContractualInfo.Slot slot;
		List<Contract.Header.ContractualInfo.Slot> slots;
		// for each component
		for(int i=0; i<props.size(); i++) {
			Proposal prop = (Proposal) props.get(i);
			
			// add coop-effort
			cinfo = objFactory.createContractHeaderContractualInfo();
			cinfo.setName("coop-effort");
			slots = cinfo.getSlot();
			
			// - agent
			slot = objFactory.createContractHeaderContractualInfoSlot();
			slot.setName("agent");
			slot.setValue(prop.getIssuer().getLocalName());
			slots.add(slot);
			// - product
			slot = objFactory.createContractHeaderContractualInfoSlot();
			slot.setName("product");
			slot.setValue(prop.getNeedType());
			slots.add(slot);
			
			AttributeValue attributeValueInProposal;
			// - unit-price
			attributeValueInProposal = prop.getAttributeValue("price");
			if(attributeValueInProposal != null) {
				slot = objFactory.createContractHeaderContractualInfoSlot();
				slot.setName("unit-price");
				slot.setValue(attributeValueInProposal.getValue());
				slots.add(slot);
			}

			cinfos.add(cinfo);
			
			// add business-process
			cinfo = objFactory.createContractHeaderContractualInfo();
			cinfo.setName("business-process");
			slots = cinfo.getSlot();
			
			// - from
			slot = objFactory.createContractHeaderContractualInfoSlot();
			slot.setName("from");
			slot.setValue(prop.getIssuer().getLocalName());
			slots.add(slot);
			// - product
			slot = objFactory.createContractHeaderContractualInfoSlot();
			slot.setName("product");
			slot.setValue(prop.getNeedType());
			slots.add(slot);
			// - to
			slot = objFactory.createContractHeaderContractualInfoSlot();
			slot.setName("to");
			slot.setValue(starterAgent.getLocalName());
			slots.add(slot);
			
			cinfos.add(cinfo);
		}
		contract.setHeader(header);
		
		return new ContractWrapper(contract, xsd_file);
	}
	
}
