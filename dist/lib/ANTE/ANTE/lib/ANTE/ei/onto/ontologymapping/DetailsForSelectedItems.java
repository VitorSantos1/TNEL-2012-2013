package ei.onto.ontologymapping;

import jade.content.Predicate;
import jade.util.leap.List;

/**
 * This is just a list of instances of DetailsForSelectedItem.
 */
public class DetailsForSelectedItems implements Predicate {

	private static final long serialVersionUID = 1L;

	private List detailsForSelectedItems;			//the list of objects with the details from the Enterprise Agents
	
	/**
	 * Gets the details list from the enterprise agents
	 * 
	 * @return	the list of details from the enterprise agents
	 */
	public List getDetailsForSelectedItems(){
		return detailsForSelectedItems;
	}
	
	/**
	 * Sets the list of details from the enterprise agents
	 * 
	 * @param id	the new list of details from the enterprise agents
	 */
	public void setDetailsForSelectedItems(List id){
		detailsForSelectedItems = id;
	}
	
}
