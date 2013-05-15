
package ei.agent.enterpriseagent.ontology;

import jade.content.Concept;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * Represents the concept for 'Item' objects. Items have a name, a description and an array of attributes
 */
public class Item implements Concept {
	private static final long serialVersionUID = 89917307384842696L;
	
	private String type;
	private String name;	
	private List attributes;	//the list of attributes
//	private String isicClass;
	
	public Item() {
		attributes = new ArrayList();
	}

	public Item(String name) {
		this();
		this.name = name;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public String getType(){
		return type;
	}

	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setAttributes(List attributes){
		this.attributes = attributes;
	}
	
	public List getAttributes(){
		return attributes;
	}

	//-----
	public void addAttribute(Attribute attribute) {
		attributes.add(attribute);
	}
	
	public int getNumberOfAttributes(){
		return attributes.size();
	}
	
	/**
	 * Get Attribute with a name
	 */
	public Attribute getAttribute(String name) {
		for(int j=0;j<attributes.size();j++) {
			Attribute a = (Attribute) attributes.get(j);
			if(a.getName().equals(name))
              return a;
		}
		return null;
	}

	public String toString() {
		return type + " " + attributes;
	}
}
