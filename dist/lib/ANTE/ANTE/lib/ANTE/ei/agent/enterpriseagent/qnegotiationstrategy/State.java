package ei.agent.enterpriseagent.qnegotiationstrategy;

import java.util.Hashtable;

import ei.onto.negotiation.qfnegotiation.AttributeValueEvaluation.AttributeClassification;

public class State {
	
	private Hashtable<String, AttributeClassification> attributeClassifications =
		new Hashtable<String, AttributeClassification>(); // attribute --> classification

	public void setAttributeClassifications(Hashtable<String, AttributeClassification> attributeClassifications) {
		this.attributeClassifications = attributeClassifications;
	}

	public Hashtable<String, AttributeClassification> getAttributeClassifications() {
		return attributeClassifications;
	}

	public void setAttributeClassification(String attribute, AttributeClassification attributeClassification) {
		attributeClassifications.put(attribute, attributeClassification);
	}

	public AttributeClassification getAttributeClassification(String attribute) {
		return attributeClassifications.get(attribute);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result  
				+ ((attributeClassifications == null) ? 0 : attributeClassifications.hashCode());
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		State other = (State) obj;
		if (attributeClassifications == null) {
			if (other.attributeClassifications != null) {
				return false;
			}
		} else if (!attributeClassifications.equals(other.attributeClassifications)) {
			return false;
		}
		return true;
	}
}
