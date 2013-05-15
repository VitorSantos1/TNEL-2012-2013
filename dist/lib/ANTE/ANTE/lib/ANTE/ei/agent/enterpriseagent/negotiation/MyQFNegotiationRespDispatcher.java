package ei.agent.enterpriseagent.negotiation;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.List;

import java.util.Hashtable;

import ei.agent.enterpriseagent.EnterpriseAgent;
import ei.agent.enterpriseagent.ontology.Attribute;
import ei.agent.enterpriseagent.ontology.Competence;
import ei.agent.enterpriseagent.ontology.Need;
import ei.agent.enterpriseagent.qnegotiationstrategy.QNegotiationStrategy;
import ei.onto.negotiation.qfnegotiation.AttributeValue;
import ei.onto.negotiation.qfnegotiation.AttributeValueEvaluation;
import ei.onto.negotiation.qfnegotiation.CompetenceCall;
import ei.onto.negotiation.qfnegotiation.Proposal;
import ei.onto.negotiation.qfnegotiation.ProposalEvaluation;
import ei.onto.ontologymapping.ItemMapping;
import ei.proto.negotiation.qfnegotiation.QFNegotiationRespDispatcher;
import ei.proto.negotiation.qfnegotiation.QFNegotiationSSResp;

/**
 */
public class MyQFNegotiationRespDispatcher extends QFNegotiationRespDispatcher {
	private static final long serialVersionUID = 1L;
	
	private Hashtable<String,QNegotiationStrategy> qNegotiationStrategies =
		new Hashtable<String, QNegotiationStrategy>();   // competence_type --> q_negotiation_strategy
	
	public MyQFNegotiationRespDispatcher(Agent agent) {
		super(agent);
	}
	
	protected Behaviour createResponder(ACLMessage message) {
		return new MyQFNegotiationSSResp(myAgent, message);
	}
	
	
	/**
	 * Implements the QF-negotiation responder role. Seller EnterpriseAgents use this role in order to participate in QF-negotiations.
	 */
	private class MyQFNegotiationSSResp extends QFNegotiationSSResp {
		private static final long serialVersionUID = 1L;
		
		private Need need;
		private Competence competence;
		private ItemMapping itemMapping = null;
		
		public MyQFNegotiationSSResp(Agent agent, ACLMessage msg) {
			super(agent, msg);
		}

		/**
		 * Handles a CFP message. This method is invoked when the first CFP is received.
		 * In this implementation, the need is checked against the competence actually provided by this agent. If it is
		 * there, <code>createStartingProposal</code> is used to generate a proposal.
		 * 
		 * @param cfp The CFP message received
		 */
		public ACLMessage handleFirstCfp(ACLMessage cfp) {
			ACLMessage response = cfp.createReply();
			try {
				ContentElement ce = myAgent.getContentManager().extractContent(cfp);
				CompetenceCall competenceCall = (CompetenceCall) ce;
				AID requesterAgent = competenceCall.getRequester();

				if(((EnterpriseAgent) myAgent).getNumberOfActiveContracts() > ((EnterpriseAgent) myAgent).MAX_NUMBER_OF_ACTIVE_CONTRACTS) {
					response.setPerformative(ACLMessage.REFUSE);
					response.setContent("refused...");
				} else {
					need = competenceCall.getNeed();
					competence = ((EnterpriseAgent) myAgent).findCompetence(need, requesterAgent);
					
					itemMapping = ((EnterpriseAgent) myAgent).getItemMapping(need, requesterAgent);
					if(itemMapping != null) {   // using a mapped item - translate need into own ontology
						need = ((EnterpriseAgent) myAgent).translateToOwnItemOntology(need, requesterAgent, itemMapping);
					}

					if(competence != null) {
						if (((EnterpriseAgent) myAgent).decideOnEnteringNegotiation(competenceCall, competence)) {
							// lets go for it!
							
							// create the starting proposal
							Proposal proposal = createStartingProposal(need, competence);

							if(itemMapping != null) {   // using a mapped item - translate proposal into foreign ontology
								proposal = ((EnterpriseAgent) myAgent).translateToForeignItemOntology(proposal, itemMapping);
							}

							myAgent.getContentManager().fillContent(response, proposal);

							response.setPerformative(ACLMessage.PROPOSE);

							if (((EnterpriseAgent) myAgent).isUseQNegotiationStrategy()) {
								if(!qNegotiationStrategies.containsKey(competence.getType())) {
									qNegotiationStrategies.put(competence.getType(), new QNegotiationStrategy());
								}
							}
						} else {
							// not negotiating
							response.setPerformative(ACLMessage.REFUSE);
						}
					} else {
						response.setPerformative(ACLMessage.REFUSE);
					}
				}
			} catch (OntologyException oe) {
				oe.printStackTrace();
				response.setPerformative(ACLMessage.REFUSE);
			} catch(Codec.CodecException ce) {
				ce.printStackTrace();
				response.setPerformative(ACLMessage.REFUSE);
			}

			return response;
		}

		public ACLMessage handleCfpWithFeedback(ACLMessage newCFP) {
			//receives the content object (a counter proposal)
			ProposalEvaluation feedback = null;
			//extract proposal from the aclmessage
			try {
				ContentElement ce = myAgent.getContentManager().extractContent(newCFP);
				if(ce instanceof ProposalEvaluation){
					feedback = (ProposalEvaluation) ce;
				}
			} catch(Codec.CodecException cex) {
				cex.printStackTrace();
			} catch(OntologyException oe) {
				oe.printStackTrace();
			}

			if(itemMapping != null) {   // using a mapped item - translate proposal into own ontology
				feedback = ((EnterpriseAgent) myAgent).translateToOwnItemOntology(feedback, itemMapping);
			}
			// prepares response
			ACLMessage response = newCFP.createReply();

			Proposal newProposal;
			if(!((EnterpriseAgent) myAgent).isUseQNegotiationStrategy()) {
				newProposal = createCounterProposal(feedback, need, competence);
			} else {
				newProposal = qNegotiationStrategies.get(competence.getType()).createCounterProposal(getNegotiationId(), feedback, need, competence);
			}
			
			if(newProposal != null) {
				if(itemMapping != null) {   // using a mapped item - translate proposal back to the foreign ontology
					newProposal = ((EnterpriseAgent) myAgent).translateToForeignItemOntology(newProposal, itemMapping);
				}

				response.setPerformative(ACLMessage.PROPOSE);
				try {
					myAgent.getContentManager().fillContent(response, newProposal);
				} catch(OntologyException oe) {
					oe.printStackTrace();
				} catch(Codec.CodecException cex) {
					cex.printStackTrace();
				}
			} else {
				response.setPerformative(ACLMessage.REFUSE);
			}

			return response;
		}
		
		public int onEnd() {
			((EnterpriseAgent) myAgent).addNegotiationMessage(getNegotiationId(), getMessages());
			
			if(((EnterpriseAgent) myAgent).isUseQNegotiationStrategy()) {
				
				if(competence != null) {
					
					QNegotiationStrategy qNegotiationStrategy = qNegotiationStrategies.get(competence.getType());
					
					//only applies if the agent decided to enter on the negotiation
					if(qNegotiationStrategy != null) {
						qNegotiationStrategy.endNegotiation(getNegotiationId());
					}
				}
			}

			return 0;
		}

		/**
		 * Creates a starting proposal for a new negotiation of a need.
		 * In this implementation, the mostly preferred value for each competence attribute is selected to compose the proposal.
		 * 
		 * @param competence		The competence being negotiated
		 * @return					A new proposal
		 */
		public Proposal createStartingProposal(Need need, Competence competence){
			Proposal proposal = new Proposal(myAgent.getAID());
			proposal.setNeedType(competence.getType());
			//fill attribute values
			for (int j = 0; j < competence.getNumberOfAttributes(); j++) {
				Attribute ownAttribute = (Attribute) competence.getAttributes().get(j);
				AttributeValue valueToPropose = new AttributeValue();
				valueToPropose.setName(ownAttribute.getName());
				valueToPropose.setType(ownAttribute.getType());

				Attribute askedAttribute = need.getAttribute(ownAttribute.getName());
				if (!ownAttribute.isDiscrete()) {
					float ownPreferredValue = checkAskedContinuousDomain(askedAttribute,Float.parseFloat(ownAttribute.getPreferredValue().toString()));
					if(ownAttribute.getType().equals("float")) {   // check if this is really of type float, or integer
						valueToPropose.setValue(String.valueOf(ownPreferredValue));
					} else {
						valueToPropose.setValue(String.valueOf((int)ownPreferredValue));
					}
				} else {
					String ownPreferredValue = checkAskedDiscreteDomain(askedAttribute, ownAttribute.getPreferredValue().toString());
					valueToPropose.setValue(ownPreferredValue);
				}
				
				proposal.getAttributeValues().add(valueToPropose);
			}	
			return proposal;
		}

		/**
		 * Creates a counterproposal, based on qualitative feedback on the previous proposal.
		 * According to the feedback received (on each AttributeValue of the Proposal), the enterpriseAgent generates a new value for each proposal's attribute.
		 * Calculating a new value for an attribute depends on the classification ("excellent", "sufficient", "bad", "very bad") and direction ("up", "down") feedback elements at an AttributeValue.
		 * In this implementation:
		 * <ul>
		 * <li> For discrete attributes, a new random value is selected within the domain of values for this attribute.
		 * <li> For continuous attributes:
		 * 		<ul>
		 * 		<li> If classification = "very bad", the value is changed in 10% towards the indicated direction.
		 * 		<li> If classification = "bad", the value is changed in 5% towards the indicated direction.
		 * 		<li> If classification = "sufficient", the value is changed in 0.5% towards the indicated direction.
		 * 		<li> If classification = "excellent", the value is not changed.
		 * 		</ul>
		 * </ul>
		 */
		public Proposal createCounterProposal(ProposalEvaluation feedback, Need need, Competence competence){
//			final float DELTA_EXCELLENT = 0.0f;
//			final float DELTA_SUFFICIENT = 0.005f;
//			final float DELTA_BAD = 0.5f;
//			final float DELTA_VERYBAD = 3f;
			
			final float DELTA_EXCELLENT = 0.0f;
			final float DELTA_SUFFICIENT = 0.5f;
			final float DELTA_BAD = 0.7f;
			final float DELTA_VERYBAD = 1.2f;
			
//			final float DELTA_SUFFICIENT = 0.05f;
//			final float DELTA_BAD = 0.5f;
//			final float DELTA_VERYBAD = 1.5f;
			
			Proposal proposal = new Proposal(myAgent.getAID());
			proposal.setNeedType(feedback.getNeedType());

			for(int i=0; i<feedback.getAttributeValueEvaluations().size(); i++) {
				AttributeValueEvaluation attributeValueEvaluation = (AttributeValueEvaluation) feedback.getAttributeValueEvaluations().get(i);
				Attribute askedAttribute = need.getAttribute(attributeValueEvaluation.getName());
				Attribute ownAttribute = competence.getAttribute(attributeValueEvaluation.getName());
				AttributeValue valueToPropose = new AttributeValue();
				valueToPropose.setName(attributeValueEvaluation.getName());
				valueToPropose.setType(attributeValueEvaluation.getType());
				
				if(attributeValueEvaluation.isDiscrete()) {   // discrete attribute
					if(attributeValueEvaluation.getClassif() == AttributeValueEvaluation.AttributeClassification.BAD || attributeValueEvaluation.getClassif() == AttributeValueEvaluation.AttributeClassification.VERY_BAD) {
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
					
					
					switch(attributeValueEvaluation.getClassif()) {
						case EXCELLENT:
							floatValue += DELTA_EXCELLENT*floatValue;
							break;
						case SUFFICIENT:
							if(attributeValueEvaluation.getDirection() == AttributeValueEvaluation.AttributeDirection.UP){
								floatValue += DELTA_SUFFICIENT*difToMaxEdge;
							} else {
								floatValue -= DELTA_SUFFICIENT*difToMinEdge;
							}
							break;
						case BAD:
							if(attributeValueEvaluation.getDirection() == AttributeValueEvaluation.AttributeDirection.UP){
								floatValue += DELTA_BAD*difToMaxEdge;
							} else if(attributeValueEvaluation.getDirection() == AttributeValueEvaluation.AttributeDirection.DOWN){
								floatValue -= DELTA_BAD*difToMinEdge;
							}
							break;
						case VERY_BAD:
							if(attributeValueEvaluation.getDirection() == AttributeValueEvaluation.AttributeDirection.UP){
								floatValue += DELTA_VERYBAD*difToMaxEdge;
							} else if(attributeValueEvaluation.getDirection() == AttributeValueEvaluation.AttributeDirection.DOWN){
								floatValue -= DELTA_VERYBAD*difToMinEdge;
							} 
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
		public float checkOwnContinuousDomain(Attribute ownAttribute, float value) {
			value = Math.max(value, Float.parseFloat((String) ownAttribute.getContinuousDomainMin()));
			value = Math.min(value, Float.parseFloat((String) ownAttribute.getContinuousDomainMax()));
			return value;
		}
		
		private float checkAskedContinuousDomain(Attribute askedAttribute, float value) {
			value = Math.max(value, Float.parseFloat((String) askedAttribute.getContinuousDomainMin()));
			value = Math.min(value, Float.parseFloat((String) askedAttribute.getContinuousDomainMax()));
			return value; 
		}
		
		private String checkAskedDiscreteDomain(Attribute askedAttribute, String value) {
			List askedValues = askedAttribute.getDiscreteDomain();
			for (int i=0; i < askedValues.size(); i++) {
				if (askedValues.get(i).equals(value)) {
					return  value;
				}
			}
			
			return String.valueOf(askedValues.get((int)(askedValues.size()*Math.random())));
		}
	}
	
} // end MyQFNegotiationRespDispatcher class

