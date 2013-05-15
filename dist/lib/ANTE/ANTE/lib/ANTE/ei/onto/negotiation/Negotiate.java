package ei.onto.negotiation;

import ei.onto.util.Parameters;

import jade.content.AgentAction;
import jade.util.leap.List;

/**
 * This class represents the action "negotiate". It is to be used in a REQUEST sent to a negotiation facilitator that will initiate a
 * specific negotiation protocol.
 * 
 * @author hlc
 */
public class Negotiate implements AgentAction {
	private static final long serialVersionUID = 6617334951707344975L;
	
	private String negotiationProtocol = "";
	private List needs;	         // the needs to be negotiated
	private Parameters negotiationParameters;
	
	public Negotiate() {
		this.negotiationParameters = new Parameters();
	}
	
	public Negotiate(String negotiationProtocol) {
		this();
		this.setNegotiationProtocol(negotiationProtocol);
	}
	
	public void setNegotiationProtocol(String negotiationProtocol) {
		this.negotiationProtocol = negotiationProtocol;
	}

	public String getNegotiationProtocol() {
		return negotiationProtocol;
	}

	public void setNegotiationParameters(Parameters negotiationParameters) {
		this.negotiationParameters = negotiationParameters;
	}

	public Parameters getNegotiationParameters() {
		return negotiationParameters;
	}

	public void setNeeds(List needs) {
		this.needs = needs;
	}

	public List getNeeds() {
		return needs;
	}

}
