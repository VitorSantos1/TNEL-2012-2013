package ei.service.ctr.context;

import ei.onto.normenv.report.NewContract;

import jade.util.leap.List;

public class AsaigRegContextGenerator extends AbstractContextGenerator {
	
	public AsaigRegContextGenerator(List contractualInfos) {
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
		
		for(int i=0; i<contractualInfos.size(); i++) {
			NewContract.Frame f = (NewContract.Frame) contractualInfos.get(i);
			if(f.getName().equals("asaig-reg-data")) {
				Context context = new Context();
				context.setName(f.getSlotValue("product"));
				context.setDeliveryTime(Long.parseLong(f.getSlotValue("delivery-rel-deadline")));
				context.setQuantity(Integer.parseInt(f.getSlotValue("quantity")));
				context.setSeller(f.getSlotValue("seller"));
				context.setFine(Double.parseDouble(f.getSlotValue("fine")));
				return context;
			}
		}
		
		return null;
	}
	
}
