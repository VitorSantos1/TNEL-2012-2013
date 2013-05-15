package ei.agent.enterpriseagent.qnegotiationstrategy;

import jade.util.leap.List;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import ei.agent.enterpriseagent.ontology.Attribute;
import ei.agent.enterpriseagent.ontology.Competence;
import ei.agent.enterpriseagent.ontology.Need;
import ei.agent.enterpriseagent.qnegotiationstrategy.Action.AttributeAction;
import ei.onto.negotiation.qfnegotiation.AttributeValue;
import ei.onto.negotiation.qfnegotiation.AttributeValueEvaluation;
import ei.onto.negotiation.qfnegotiation.Proposal;
import ei.onto.negotiation.qfnegotiation.ProposalEvaluation;
import ei.onto.negotiation.qfnegotiation.AttributeValueEvaluation.AttributeClassification;
import ei.onto.negotiation.qfnegotiation.AttributeValueEvaluation.AttributeDirection;

public class QNegotiationStrategy {

	private static final double GAMMA     = 0.6;   // discount factor
	private static final double ALPHA_INI = 0.9;   // learning rate
	private static final double TEMPERATURE_INI = 12.0; //probabilidade de selecção: controla a quantidade de exploração que é realizada
	private static final double BETA     = 0.05;

	private static final double PENALTY[] = {0.9, 0.6, 0.2};  //for VERYBAD, BAD, SUFFICIENT
	private static final int PENALTY_INDEX_VERYBAD = 0;
	private static final int PENALTY_INDEX_BAD = 1;
	private static final int PENALTY_INDEX_SUFFICIENT = 2;

//	private static final double DELTA_FEW = 0.05;
//	private static final double DELTA_MEDIUM = 0.5;
//	private static final double DELTA_VERY = 1.5;

	private static final double DELTA_FEW = 0.1;
	private static final double DELTA_MEDIUM = 0.2;
	private static final double DELTA_VERY = 1.8;
	
	private double alpha = 0.0;
	private double temperature = TEMPERATURE_INI;

    private int iteration = 0;

	private Hashtable<String, StateActionPair> previousProposalEvaluations = new Hashtable<String, StateActionPair>(); // negotiationId --> StateActionPair
	private Hashtable<State, Hashtable<Action, Double>> qValues = new Hashtable<State, Hashtable<Action, Double>>();
	
	public Proposal createCounterProposal(String negotiationId, ProposalEvaluation proposalEvaluation, Need need, Competence competence) {

		StateActionPair stateActionPair = previousProposalEvaluations.get(negotiationId);
		if(stateActionPair != null) {   // i.e., learning starts only after counter-proposing to the first proposalEvalution
			// update qValue
			updateStateAction(stateActionPair, proposalEvaluation);
		}

		State state = extractState(proposalEvaluation);
		// get action to play
		Action actionToPlay = generateActionToPlay(state, proposalEvaluation);
		
		
		// store state/action pair in hashtable
		previousProposalEvaluations.put(negotiationId, new StateActionPair(state, actionToPlay));
		
		// create new Proposal based on chosen Action
		Proposal proposal = generateProposal(proposalEvaluation, need, competence, actionToPlay); 

		modifyLearningRate(++iteration);
        decayTemperature(iteration);
		
		return proposal;
	}
	
	private void updateStateAction(StateActionPair stateActionPair, ProposalEvaluation proposalEvaluation) {
		State state = stateActionPair.getState();
		Action action = stateActionPair.getAction();
		
		double reward = calculateReward(proposalEvaluation);
		
		Double currentQValue = null;
		Hashtable<Action, Double> actions = qValues.get(state);
		if(actions == null) {
			actions = new Hashtable<Action, Double>();
			
			qValues.put(state, actions);
			currentQValue = 0.0;
		} else {
			currentQValue = actions.get(action);
			if(currentQValue == null) { // there was no qValue for this state/action pair
				currentQValue = 0.0;
			}
		}

		// compute best qValue in arriving state
		double qMax = 0.0;
		State nextState = extractState(proposalEvaluation);
		Hashtable<Action, Double> nextActions = qValues.get(nextState);
		if(nextActions != null) {   // checking if the nextState was ever reached before
			Enumeration<Double> e = nextActions.elements();
			while(e.hasMoreElements()) {
				double qValue = e.nextElement();
				qMax = Math.max(qMax, qValue);
			}
		}

		// update q value
		double qValue = currentQValue + alpha * (reward + GAMMA * qMax - currentQValue);
		
		actions.put(action, qValue);
	}

	private double calculateReward(ProposalEvaluation proposalEvaluation) {
		double penalties = 0.0;
		for(int i=0; i<proposalEvaluation.getAttributeValues().size(); i++){
			AttributeValueEvaluation attributeValueEvaluation = (AttributeValueEvaluation) proposalEvaluation.getAttributeValues().get(i);
			switch(attributeValueEvaluation.getClassif()) {
			case SUFFICIENT:
				penalties += PENALTY[PENALTY_INDEX_SUFFICIENT];
				break;
			case BAD:
				penalties += PENALTY[PENALTY_INDEX_BAD];
				break;
			case VERY_BAD:
				penalties += PENALTY[PENALTY_INDEX_VERYBAD];
				break;
			}
		}
		return proposalEvaluation.getAttributeValues().size()/2 - penalties;
	}
	
	
	private State extractState(ProposalEvaluation proposalEvaluation) {
		State state = new State();
		List attributeValueEvaluation = proposalEvaluation.getAttributeValues();
		for(int i=0; i<attributeValueEvaluation.size(); i++) {
			state.setAttributeClassification(((AttributeValueEvaluation) attributeValueEvaluation.get(i)).getName(),
					 ((AttributeValueEvaluation) attributeValueEvaluation.get(i)).getClassif());
		}
		return state;
	}
	
	private Action generateActionToPlay(State state, ProposalEvaluation proposalEvaluation) {
		// prepare base action that maintains all attributes
		Action baseAction = new Action();
		Enumeration<String> attributeNames = state.getAttributeClassifications().keys();
		while(attributeNames.hasMoreElements()) {
			baseAction.setAttributeAction(attributeNames.nextElement(), AttributeAction.MAINTAIN);
		}

		// generate sensible actions (i.e., only those that make sense according to the feedback)
		Set<Action> actions = new HashSet<Action>();
		
		attributeNames = state.getAttributeClassifications().keys();
		while(attributeNames.hasMoreElements()) {
			String attributeName = attributeNames.nextElement();
			
			AttributeClassification attributeClassification = state.getAttributeClassifications().get(attributeName);
			
			Vector<AttributeAction> attributeActions = new Vector<AttributeAction>();
			AttributeValueEvaluation attributeValueEvaluation = proposalEvaluation.getAttributeValueEvaluation(attributeName);
			switch(attributeClassification) {
				case EXCELLENT: //...?????????????????????
					attributeActions.add(AttributeAction.MAINTAIN);
					break;
				case SUFFICIENT: //...?????????????????????
					if(attributeValueEvaluation.isDiscrete()) {
						attributeActions.add(AttributeAction.MAINTAIN);
					} else {   // continuous attribute
						if(attributeValueEvaluation.getDirection().equals(AttributeDirection.UP)) {
							attributeActions.add(AttributeAction.INC_FEW);
						} else {
							attributeActions.add(AttributeAction.DEC_FEW);
						}
					}
					break;
				case BAD: //...?????????????????????
					if(attributeValueEvaluation.isDiscrete()) {
						attributeActions.add(AttributeAction.CHANGE);
					} else {   // continuous attribute
						if(attributeValueEvaluation.getDirection().equals(AttributeDirection.UP)) {
							attributeActions.add(AttributeAction.INC_MEDIUM);
							//attributeActions.add(AttributeAction.INC_FEW);
						} else {
							attributeActions.add(AttributeAction.DEC_MEDIUM);
							//attributeActions.add(AttributeAction.DEC_FEW);
						}
					}
					break;
				case VERY_BAD:
					if(attributeValueEvaluation.isDiscrete()) {
						attributeActions.add(AttributeAction.CHANGE);
					} else {   // continuous attribute
						if(attributeValueEvaluation.getDirection().equals(AttributeDirection.UP)) {
							attributeActions.add(AttributeAction.INC_VERY);
							attributeActions.add(AttributeAction.INC_MEDIUM);
							//attributeActions.add(AttributeAction.INC_FEW);
						} else {
							attributeActions.add(AttributeAction.DEC_VERY);
							attributeActions.add(AttributeAction.DEC_MEDIUM);
							//attributeActions.add(AttributeAction.DEC_FEW);
						}
					}
					break;
			}

			Vector<Action> actionsForThisAttribute = new Vector<Action>();
//			System.out.println(" @@@ "+attributeClassification + " ----> AttributeActions: " + attributeActions.size() );			
			for(int i=0; i<attributeActions.size(); i++) {
				// create action just changing (if not maintain) this attribute
				Action simpleAction = (Action) baseAction.clone();
				simpleAction.setAttributeAction(attributeName, attributeActions.elementAt(i));
				actionsForThisAttribute.add(simpleAction);
				
				// cross the change in this attribute with the actions that are already in the actions vector
				Iterator<Action> it = actions.iterator();
				while(it.hasNext()) {
					Action action = (Action) it.next().clone();
					action.setAttributeAction(attributeName, attributeActions.get(i));
					actionsForThisAttribute.add(action);
				}
			}
			
			// add actions for this attribute to the actions vector
			actions.addAll(actionsForThisAttribute);
		}
		
		// select action
		Action action = selectAction(state, actions);
		if(action != null) {
			return action;
		} else {
			return null;//baseAction;   // should never occur
		}
	}
	
	private Action selectAction(State state, Set<Action> actions) {
		Hashtable<Action, Double> probs = boltzmannDistribution(state, actions);
		double r = (new Random()).nextDouble();
		double acum = 0.0;
		Iterator<Action> it = actions.iterator();
		while(it.hasNext()) {
			Action action = it.next();
			acum += probs.get(action);
			if (acum >= r) {
				return action;
			}
		}
		return null;
	}
	
	private Hashtable<Action, Double> boltzmannDistribution(State state, Set<Action> actions) {
		double sum = 0.0;
		Hashtable<Action, Double> stateActions = qValues.get(state);
		if(stateActions == null) stateActions = new Hashtable<Action, Double>();
		
		// denominator
		Iterator<Action> it = actions.iterator();
		while(it.hasNext()) {
			Double qValue = stateActions.get(it.next());
			qValue = (qValue == null) ? 0.0 : qValue;
			sum += Math.exp(qValue/temperature);
		}
		
		// probability for each possible action
		Hashtable<Action, Double> probs = new Hashtable<Action, Double>();
		it = actions.iterator();
		while(it.hasNext()) {
			Action action = it.next();
			Double qValue = stateActions.get(action);
			qValue = (qValue == null) ? 0.0 : qValue;
			probs.put(action, Math.exp(qValue/temperature)/sum);
		}
		
		return probs;
	}
	
	private Proposal generateProposal(ProposalEvaluation proposalEvaluation, Need need, Competence competence, Action actionToPlay) {
		Proposal proposal = new Proposal(proposalEvaluation.getIssuer());
		proposal.setNeedType(proposalEvaluation.getNeedType());

		for(int i=0; i<proposalEvaluation.getAttributeValues().size(); i++) {
			AttributeValueEvaluation attributeValueEvaluation = (AttributeValueEvaluation) proposalEvaluation.getAttributeValues().get(i);
			Attribute askedAttribute = need.getAttribute(attributeValueEvaluation.getName());
			Attribute ownAttribute = competence.getAttribute(attributeValueEvaluation.getName());
			AttributeValue valueToPropose = new AttributeValue();
			valueToPropose.setName(attributeValueEvaluation.getName());
			valueToPropose.setType(attributeValueEvaluation.getType());
			
			AttributeAction myAction = actionToPlay.getAttributeAction(attributeValueEvaluation.getName());
			
			if(attributeValueEvaluation.isDiscrete()) {   // discrete attribute
				if(myAction.equals(AttributeAction.CHANGE)) {
					// propose a new random value for this attribute
					List domain = ownAttribute.getDiscreteDomain();

					String proposedValue = (String) domain.get((int)(domain.size()*Math.random()));   // FIXME: may get the same value or a previously proposed one!
					valueToPropose.setValue(proposedValue);
				} else {
					// maintain previous value
					valueToPropose.setValue(attributeValueEvaluation.getValue());
				}
			} else {   // continuous attribute
				float floatValue = Float.parseFloat(attributeValueEvaluation.getValue());   // treat as float
				float difToMaxEdge = Float.parseFloat((String) askedAttribute.getContinuousDomainMax()) - floatValue;
				float difToMinEdge = floatValue - Float.parseFloat((String) askedAttribute.getContinuousDomainMin());
				switch(myAction) {
					case INC_FEW:
						floatValue += DELTA_FEW*difToMaxEdge;
						break;
					case DEC_FEW:
						floatValue -= DELTA_FEW*difToMinEdge;
						break;
					case INC_MEDIUM:
						floatValue += DELTA_MEDIUM*difToMaxEdge;
						break;
					case DEC_MEDIUM:
						floatValue -= DELTA_MEDIUM*difToMinEdge;
						break;
					case INC_VERY:
						floatValue += DELTA_VERY*difToMaxEdge;
						break;
					case DEC_VERY:
						floatValue -= DELTA_VERY*difToMinEdge;
						break;
				}

				floatValue = checkOwnContinuousDomain(ownAttribute, floatValue);
				floatValue = checkAskedContinuousDomain(askedAttribute, floatValue);

				
				if(attributeValueEvaluation.getType().equals("float")) {   // check if this is really of type float, or integer
					valueToPropose.setValue(String.valueOf(floatValue));
				} else {
					valueToPropose.setValue(String.valueOf(Math.round(floatValue)));
				}
			}
			proposal.getAttributeValues().add(valueToPropose);
		}

		return proposal;
	}

	/**
	 * Checks and rectifies a value for an attribute, keeping it inside the range of acceptable values.
	 * 
	 * @param ownAttribute 
	 * @param value
	 * @return
	 */
	private float checkOwnContinuousDomain(Attribute ownAttribute, float value) {
		value = Math.max(value, Float.parseFloat((String) ownAttribute.getContinuousDomainMin()));
		value = Math.min(value, Float.parseFloat((String) ownAttribute.getContinuousDomainMax()));
		return value;
	}
	private float checkAskedContinuousDomain(Attribute askedAttribute, float value) {
		value = Math.max(value, Float.parseFloat((String) askedAttribute.getContinuousDomainMin()));
		value = Math.min(value, Float.parseFloat((String) askedAttribute.getContinuousDomainMax()));
		return value; 
	}
	
	private void decayTemperature(int n) {
		temperature -= Math.pow(0.9,n);
	}

	private void modifyLearningRate(int n) {
		alpha = ALPHA_INI/(1+BETA*n);
	}
	
	public void endNegotiation(String negotiationId) {
		previousProposalEvaluations.remove(negotiationId);
	}
}
