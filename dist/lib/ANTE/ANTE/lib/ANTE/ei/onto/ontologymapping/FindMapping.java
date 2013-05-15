package ei.onto.ontologymapping;

import ei.agent.enterpriseagent.ontology.Need;
import jade.content.AgentAction;
import jade.core.AID;
import jade.util.leap.List;

public class FindMapping implements AgentAction {
	private static final long serialVersionUID = 1L;

	private AID targetAgent;
	private Need targetNeed;  // target
	
	private List listOfKnownCompetences;
	
	public FindMapping() {
	}
	
	public FindMapping(Need need, AID targetAgent, List listOfKnownCompetences) {
		this.targetNeed = need;
		
		this.targetAgent = targetAgent;
		this.listOfKnownCompetences = listOfKnownCompetences;
	}
	
	public void setTargetAgent(AID targetAgent) {
		this.targetAgent = targetAgent;
	}
	
	public AID getTargetAgent() {
		return targetAgent;
	}

	public List getListOfKnownCompetences() {
		return listOfKnownCompetences;
	}

	public void setListOfKnownCompetences(List listOfKnownCompetences) {
		this.listOfKnownCompetences = listOfKnownCompetences;
	}

	public void setTargetNeed(Need targetNeed) {
		this.targetNeed = targetNeed;
	}

	public Need getTargetNeed() {
		return targetNeed;
	}
	
}
