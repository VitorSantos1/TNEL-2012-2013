
package ei.onto.normenv.management;

import jade.content.Predicate;
import jade.util.leap.List;

/**
 * This class represents the predicate "contract-types".
 * 
 * @author hlc
 */
public class ContractTypes implements Predicate {
	
	private static final long serialVersionUID = 89917307384842696L;
	
	private List types;
	
	public List getTypes(){
		return types;
	}
	
	public void setTypes(List types){
		this.types = types;
	}
	
}
