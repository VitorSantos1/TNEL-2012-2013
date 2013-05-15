package ei.agent.enterpriseagent.ontology;

import jade.content.Concept;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * Represents the concept of an attribute object. Each attribute has a name and a type. They also have a vector of preferable values.
 */
public class Attribute implements Concept {
	private static final long serialVersionUID = -1603292288425183344L;
	
	private String name;	//the name of the attribute
	private String type;	//the type of the attribute (string, integer, float, ...)
	private List discreteDomain;	// the domain of the attribute, if discrete (a list of possible values)
	private Object continuousDomainMin;		// the minimum domain value, if continuous (integer, float, ...) // NOTE: for now this is being set as a String
	private Object continuousDomainMax;		// the maximum domain value, if continuous (integer, float, ...) // NOTE: for now this is being set as a String
	
	private Object preferredValue;   // preferred value for this attribute (if discrete or continuous)
//  NOT USED FOR NOW, but possibly interesting to have in the future:
//	- continuous attributes: range of preferred values, and preference direction within that range
//	- discrete attributes: ordered list of values
//	private List prefValues;	//the preferable values for the agent who owns the item containing this attribute; must be inside domain.
//	private String direction;	//the direction of the interval (only for integers). "up" if its increasing, "down" otherwise
	
	public Attribute() {
		discreteDomain = new ArrayList();
	}

	public Attribute(String name){
		this();
		this.name = name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
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

	public void setDiscreteDomain(List discreteDomain){
		this.discreteDomain = discreteDomain;
	}
	
	public List getDiscreteDomain(){
		return discreteDomain;
	}

	public void setContinuousDomainMin(Object continuousDomainMin){
		this.continuousDomainMin = continuousDomainMin;
	}

	public Object getContinuousDomainMin(){
		return continuousDomainMin;
	}

	public void setContinuousDomainMax(Object continuousDomainMax){
		this.continuousDomainMax = continuousDomainMax;
	}
	
	public Object getContinuousDomainMax(){
		return continuousDomainMax;
	}

	public void setPreferredValue(Object preferredValue) {
		this.preferredValue = preferredValue;
	}

	public Object getPreferredValue() {
		return preferredValue;
	}
	
	public String toString() {
		return name + " " + preferredValue;
	}
	
}
