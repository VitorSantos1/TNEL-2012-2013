package ei.onto.ontologymapping;

import jade.content.Concept;
import jade.util.leap.List;

/**
 * This class represents the item details who are needed by the ontology service
 * required by the negotiation in order to match possible needs in different ontologies. 
 * This list has another five StringList objects inside. One for attributes of type string, one for integers, one for floats,
 * one for booleans, and one for relations.
 */
public class DetailsForSelectedItem implements Concept {

	private static final long serialVersionUID = 1L;

	private List details;	//list of StringList objects. Each one has a list of strings inside. (strings,ints,floats,etc. for the ontology)
	private String className;
	
	
	/**
	 * @param n	the new class name
	 */
	public void setClassName(String n){
		className = n;
	}
	
	/**
	 * Gets the name of the class 
	 * 
	 * @return	the name of the class 
	 */
	public String getClassName(){
		return className;
	}
	
	/**
	 * Sets the details list
	 * 
	 * @param l	the new details list
	 */
	public void setDetails(List l){
		details = l;
	}
	
	/**
	 * @return	the details list
	 */
	public List getDetails(){
		return details;
	}
}
