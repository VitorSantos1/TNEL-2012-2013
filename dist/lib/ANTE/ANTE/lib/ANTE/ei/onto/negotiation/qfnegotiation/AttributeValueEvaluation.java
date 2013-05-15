package ei.onto.negotiation.qfnegotiation;

/**
 * Represents the concept for attribute values contained on components involved in a negotiation
 */
public class AttributeValueEvaluation extends AttributeValue {
	private static final long serialVersionUID = 164169033777805657L;
	
	public static enum AttributeClassification {EXCELLENT, SUFFICIENT, BAD, VERY_BAD};
	public static enum AttributeDirection {UP, DOWN, MAINTAIN};
	
	// feedback
	private AttributeClassification classif;
	private AttributeDirection direction;   // only meaningful for continuous attributes
	
	public AttributeValueEvaluation() {
		super();
	}
	
	public AttributeValueEvaluation(AttributeValue attributeValue) {
		this();
		this.setName(attributeValue.getName());
		this.setType(attributeValue.getType());
		this.setValue(attributeValue.getValue());
	}
	
	public AttributeClassification getClassif(){
		return classif;
	}
	
	public void setClassif(AttributeClassification classif){
		this.classif = classif;
	}
	
	public void setDirection(AttributeDirection direction){
		this.direction = direction;
	}
	
	public AttributeDirection getDirection(){
		return direction;
	}
	
	public String toString() {
		return getName() + " " + getValue() + " " + classif + " " + direction;
	}
	
}
