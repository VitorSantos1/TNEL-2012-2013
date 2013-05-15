package ei.service.ctr.context;

import ei.onto.normenv.report.NewContract;

import jade.util.leap.List;

public class ContractContextGenerator extends AbstractContextGenerator {
	
	public ContractContextGenerator(List contractualInfos) {
		super(contractualInfos);
	}
	
	/**
	 * Generates a Context.
	 * 
	 * @return	The Context
	 */
	public Context generateContext() {
		if(contractualInfos.size() == 0) {
			return null;
		}
		
		NewContract.Frame f = (NewContract.Frame) contractualInfos.get(0);
		
		Context context = new Context();
		context.setName(f.getName());
		context.setDeliveryTime(Long.parseLong(f.getSlotValue("delivery")));
		context.setQuantity(Integer.parseInt(f.getSlotValue("quantity")));
		context.setSeller(f.getSlotValue("seller"));
		
		return context;
	}
	
}
