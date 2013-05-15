package ei.onto.ontologymapping;

import jade.content.*;
import jade.util.leap.List;
import jade.util.leap.ArrayList;

/**
 * Represents a list of strings. Used by Enterprise Agents to send details about their supplied components to the ontology service.
 * Each StringList object contains a list of attributes of a certain type (strings, integers, booleans, etc)
 */
public class StringList implements Concept{

	private static final long serialVersionUID = 1L;

	private List str = new ArrayList();	//the list of attributes
	
	/**
	 * Sets the list of attributes
	 * 
	 * @param s	the new list of attributes
	 */
	public void setStr(List s){
		str = s;
	}
	
	/**
	 * Gets the attribute list
	 * 
	 * @return	the attribute list
	 */
	public List getStr(){
		return str;
	}
	
}
