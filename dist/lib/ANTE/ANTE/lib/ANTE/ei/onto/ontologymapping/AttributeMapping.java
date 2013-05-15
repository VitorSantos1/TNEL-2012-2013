package ei.onto.ontologymapping;

import jade.content.Concept;

/**
 * Represents the attribute names and their respective translation to another ontology
 */
public class AttributeMapping implements Concept{
	private static final long serialVersionUID = 3835873872450364159L;
	
	private String atributeName;
	private String mappingAttributeName;
	
	public AttributeMapping(){
	}

	public void setAtributeName(String n){
		atributeName = n;
	}
	
	public String getAtributeName(){
		return atributeName;
	}
	
	public void setMappingAttributeName(String nt){
		mappingAttributeName = nt;
	}
	
	public String getMappingAttributeName(){
		return mappingAttributeName;
	}
	
}
