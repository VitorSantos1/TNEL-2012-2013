package ei.agent.enterpriseagent.qnegotiationstrategy;

public class StateActionPair {

	private State state;
	private Action action;
	
	public StateActionPair() {
	}

	public StateActionPair(State state, Action action) {
		this.state = state;
		this.action = action;
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
	public State getState() {
		return state;
	}
	
	public void setAction(Action action) {
		this.action = action;
	}
	
	public Action getAction() {
		return action;
	}
	
}
