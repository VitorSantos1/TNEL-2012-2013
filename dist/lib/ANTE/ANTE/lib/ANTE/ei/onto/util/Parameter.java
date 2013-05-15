package ei.onto.util;

import jade.content.Concept;

/**
 * A parameter, composed by a name and a value.
 * 
 * @author hlc
 */
public class Parameter implements Concept {
	private static final long serialVersionUID = -1603292288425183344L;
	
	private String name;
	private String value;
	
	public Parameter() {
	}

	public Parameter(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
}
