package ei.onto.negotiation.qfnegotiation;

import jade.content.Predicate;
import jade.core.AID;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * Represents a proposal. This class is composed of two parts. Part of the proposal is created by the proposing agent, and includes
 * the attribute values. Another part concerns the proposal's evaluation and feedback, which is provided by the negotiation mediator.
 */
public class Proposal implements Predicate {
	private static final long serialVersionUID = -8703771178474588373L;
	
	private AID issuer;     // proposal issuer
	private String needType; // the need over which the proposal is made
	private List attributeValues; // attribute values and classifications
	private double fine = 0.0;
	
	public Proposal() {
		attributeValues = new ArrayList();	  
	}
	
	public Proposal(AID issuer) {
		this();
		this.issuer = issuer;
	}
	
	public String getNeedType() {
		return needType;
	}

	public void setNeedType(String needType) {
		this.needType = needType;
	}

	public void setIssuer(AID issuer){
		this.issuer = issuer;
	}
	
	public AID getIssuer(){
		return issuer;
	}
	
	public List getAttributeValues() {
		return attributeValues;
	}
	
	public void setAttributeValues(List attributeValues) {
		this.attributeValues = attributeValues;
	}

	// ---
	
	public AttributeValue getAttributeValue(String name){
		for(int j=0;j<attributeValues.size();j++) {
			AttributeValue a = (AttributeValue) attributeValues.get(j);
			if(a.getName().equals(name))
              return a;
		}
		return null;
	}

	public void setFine(double fine) {
		this.fine = fine;
	}

	public double getFine() {
		return fine;
	}

	public String toString() {
		return "Proposal (" + needType + " " + attributeValues + ")";
	}
	
}
