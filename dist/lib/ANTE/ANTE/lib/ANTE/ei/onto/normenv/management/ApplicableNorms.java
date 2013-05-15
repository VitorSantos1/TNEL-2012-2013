
package ei.onto.normenv.management;

import jade.content.Predicate;
import jade.util.leap.List;

/**
 * This class represents the predicate "applicable-norms".
 * 
 * @author hlc
 */
public class ApplicableNorms implements Predicate {
	private static final long serialVersionUID = 89917307384842696L;
	
	private List norms;
	
	public List getNorms(){
		return norms;
	}
	
	public void setNorms(List norms){
		this.norms = norms;
	}
	
}
