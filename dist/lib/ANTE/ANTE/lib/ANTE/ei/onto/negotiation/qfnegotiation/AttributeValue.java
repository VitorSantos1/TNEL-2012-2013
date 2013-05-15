package ei.onto.negotiation.qfnegotiation;

import jade.content.*; 

/**
 * Represents the concept for attribute values contained on components involved in a negotiation
 */
public class AttributeValue implements Concept {
	private static final long serialVersionUID = 164169033777805657L;
	
	private String name;
	private String type;
	private String value;
	
	public AttributeValue() {
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public String getType(){
		return type;
	}
	
	public boolean isDiscrete() {
		return (type.equals("string") || type.equals("boolean"));
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	public String toString() {
		return name + " " + value;
	}
	
}
