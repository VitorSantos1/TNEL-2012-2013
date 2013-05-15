package ei.agent.enterpriseagent.qnegotiationstrategy;

import java.util.Hashtable;

public class Action implements Cloneable {

	public static enum AttributeAction {INC_FEW, INC_MEDIUM, INC_VERY, DEC_FEW, DEC_MEDIUM, DEC_VERY, MAINTAIN, CHANGE};
	
	private Hashtable<String, AttributeAction> attributeActions = new Hashtable<String, AttributeAction>(); // attribute --> action

	public Action() {
	}
	
	private Action(Hashtable<String, AttributeAction> attributeActions) {
		this.attributeActions = attributeActions;
	}
	
	public void setAttributeActions(Hashtable<String, AttributeAction> attributeActions) {
		this.attributeActions = attributeActions;
	}

	public Hashtable<String, AttributeAction> getAttributeActions() {
		return attributeActions;
	}
	
	public void setAttributeAction(String attribute, AttributeAction attributeAction) {
		attributeActions.put(attribute, attributeAction);
	}

	public AttributeAction getAttributeAction(String attribute) {
		return attributeActions.get(attribute);
	}
	
	public Object clone() {
		Hashtable<String, AttributeAction> attributeActions = new Hashtable<String, Action.AttributeAction>();
		attributeActions.putAll(this.attributeActions);
		return new Action(attributeActions);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributeActions == null) ? 0 : attributeActions.hashCode());
		
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
		Action other = (Action) obj;
		if (attributeActions == null) {
			if (other.attributeActions != null) {
				return false;
			}
		} else if (!attributeActions.equals(other.attributeActions)) {
			return false;
		}
		return true;
	}
}
