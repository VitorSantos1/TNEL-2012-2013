package ei.service.normenv;

import jess.Context;
import jess.Deftemplate;
import jess.Fact;
import jess.Filter;
import jess.JessException;

/**
 * A contract filter to be used when getting elements from Jess.
 * 
 * @author hlc
 */
public class JessFactContractFilter implements Filter {
	
	private String contract_id;
	private Deftemplate fact_type;
	private Context context;
	
	public JessFactContractFilter(String contract_id, Context context) {
		this(contract_id, null, context);
	}
	
	public JessFactContractFilter(String contract_id, Deftemplate fact_type, Context context) {
		this.contract_id = contract_id;
		this.fact_type = fact_type;
		this.context = context;
	}
	
	/**
	 * An object will be accepted (i.e. this method will return <code>true</code>) if it is a <code>jess.Fact</code> and, if a
	 * <code>Deftemplate</code> was provided to the constructor, the object's <code>Deftemplate</code> is the same. Also:
	 * <ul>
	 * <li>if the object is a context definition fact, its id must match the contract id passed to the constructor
	 * <li>if the object is a contextual-info or institutional-reality-element fact, its context must
	 *     match the contract id passed to the constructor
	 * </ul>
	 */
	public boolean accept(Object o) {
		// is it a fact?
		if(o instanceof Fact) {
			// get top deftemplate
			Deftemplate t = NormEnvEngine.findTopDeftemplate(((Fact) o).getDeftemplate());
			// check deftemplate, if given
			if(fact_type == null || t.equals(fact_type)) {
				try {
					// is this the context definition for the contract?
					if(t.getBaseName().equals("context") && ((Fact) o).getSlotValue("id").stringValue(context).equals(contract_id))
						return true;
					// is this contextual-info for the contract?
					if(t.getBaseName().equals("contextual-info") && ((Fact) o).getSlotValue("context").stringValue(context).equals(contract_id))
						return true;
					// is this an ire for the contract?
					if(t.getBaseName().equals("ire") && ((Fact) o).getSlotValue("context").stringValue(context).equals(contract_id))
						return true;
				} catch(JessException je) {
					je.printStackTrace();   // should never occur
					return false;
				}
			}
		}
		return false;
	}
	
}
