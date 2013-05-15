package ei.service.ctr.context;


import jade.util.leap.List;

public abstract class AbstractContextGenerator {
	
	protected List contractualInfos;   // this is a list of NewContract.Frame

	public AbstractContextGenerator(List contractualInfos) {
		this.contractualInfos = contractualInfos;
	}
	
	/**
	 * Generates a Context.
	 * 
	 * @return	The Context
	 */
	public abstract Context generateContext();
	
}
