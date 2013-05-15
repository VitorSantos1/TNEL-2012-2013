package ei.proto.negotiation.qfnegotiation;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import ei.ElectronicInstitution;
import ei.agent.enterpriseagent.ontology.Attribute;
import ei.agent.enterpriseagent.ontology.Need;
import ei.onto.negotiation.qfnegotiation.AttributeValue;
import ei.onto.negotiation.qfnegotiation.AttributeValueEvaluation;
import ei.onto.negotiation.qfnegotiation.CompetenceCall;
import ei.onto.negotiation.qfnegotiation.Proposal;
import ei.onto.negotiation.qfnegotiation.ProposalEvaluation;
import ei.onto.util.Parameters;
import ei.scripts.STDev;
import ei.service.negotiation.qfnegotiation.InterestedAgent;
import ei.service.negotiation.qfnegotiation.NegotiationRoundProposalEvaluations;
import ei.service.negotiation.qfnegotiation.ProposalSet;
import ei.service.negotiation.qfnegotiation.QFNegotiationMediator;
import ei.service.negotiation.qfnegotiation.QFNegotiationParameters;

/**
 * Initiator role for a QF_NEGOTIATION protocol, with the QFNegotiationOntology.
 * This protocol is based on FIPA_ITERATED_CONTRACT_NET:
 * <ul>
 * <li> a CFP with a CompetenceCall is sent for each agent;
 * <li> a Proposal is received from each agent participating in the negotiation;
 * <li> a multi-round negotiation takes place, in which:
 * 		<ol>
 * 		<li> proposals are evaluated, and qualitative feedback is generated for each attribute;
 * 		<li> a new negotiation round is started by sending a new CFP with a ProposalEvaluation;
 * 		<li> new proposals are received from each participating agent.
 * 		</ol>
 * <li> the best proposal received is accepted, and the other proposals are rejected.
 * </ul>
 * 
 * @author pbn, hlc
 */
public class QFNegotiationInit extends ContractNetInitiator {
	private static final long serialVersionUID = 1L;
	
	private final int EVAL_MIN_ACCEPT = 0;      
	
	private int round=0;
	private boolean success;
	
	private ProposalEvaluation winnerProposal; //used in fill of contract 

	private float lastRoundAvgUtility;
	private double standardDeviation;		

	private final int PROPOSAL_WAITING_TIME = 5000;

	private AID requester;
    private Need need;
	private Vector<InterestedAgent> agents;
	private Parameters negotiationParameters;

    private int numberOfRefusedSuppliers;        
    
	private Vector<NegotiationRoundProposalEvaluations> receivedProposalsPerRound = new Vector<NegotiationRoundProposalEvaluations>();		
    
	private final int CFP_WAITING_TIME = 10000; 
	
	
	/**TODO acrescento no ei-config tb?
	 * Used to reduce the trust weight in the fine (sanction) calculus. See
	 * page 45 of Joana Urbano, Henrique Lopes Cardoso, 
	 * Ana Paula Rocha, Eugénio Oliveira, “Trust and Normative Control in Multi-Agent 
	 * Systems”, Advances in Distributed Computing and Artificial Intelligence 
	 * Journal, Vol. I, No. 1, July 2012, pp. 43-52 (2012).
	 */
	private float trustReducFactor = 4.0f;
	
	
	public QFNegotiationInit(Agent agent, AID requester, Need need, Vector<InterestedAgent> agents, Parameters negotiationParameters) {
		super(agent, new ACLMessage(ACLMessage.CFP));
		
		this.round=1;
		this.success = false;

		this.requester = requester;
		this.need = need;
		this.agents = agents;
		
		this.negotiationParameters = negotiationParameters;
	}
	
	public Need getNeed() {
		return need;
	}

	protected Vector prepareCfps(ACLMessage cfp) {
		Vector<ACLMessage> cfps = new Vector<ACLMessage>();
		ACLMessage cfpMessage;
		
		for(InterestedAgent iA : agents) {
			cfpMessage = (ACLMessage) cfp.clone();
			cfpMessage.setProtocol(ElectronicInstitution.QF_NEGOTIATION_PROTOCOL); 
			cfpMessage.setOntology( ElectronicInstitution.QF_NEGOTIATION_ONTOLOGY );
			cfpMessage.setLanguage( ((QFNegotiationMediator) myAgent).getSlCodec().getName() );
			Date date = new Date();
			date.setTime(date.getTime()+CFP_WAITING_TIME);
			cfpMessage.setReplyByDate(date);
			cfpMessage.addReceiver(iA.getAgent());
			try {
				CompetenceCall competenceCall = new CompetenceCall();
				competenceCall.setNeed( need );
				competenceCall.setContractType(getContractType());
				competenceCall.setRequester(requester);

				// contract drafting (fines)
				if(isUseTrustInContractDrafting()) {
					competenceCall.setFine(computeFine(iA));
					//System.out.println("Fine for " + iA.getAgent().getLocalName() + ": " + computeFine(iA));
				}
				
				myAgent.getContentManager().fillContent(cfpMessage, competenceCall); 
			} catch (Exception e) {
				e.printStackTrace();
			}
			cfps.add(cfpMessage);
		}
		return cfps;
	}
	
	protected void handleAllResponses(Vector responses, Vector acceptances) {
		Vector<ACLMessage> newCfps = new Vector<ACLMessage>();
		Vector<ACLMessage> receivedProposals=new Vector<ACLMessage>();	
		
		//filter PROPOSE messages 
		for( int i=0; i<responses.size(); i++ ){
			ACLMessage msg = (ACLMessage) responses.get(i);
			if (msg.getPerformative() == ACLMessage.PROPOSE) {
				receivedProposals.add((ACLMessage)responses.get(i));
			} else {
				//System.out.println(getLocalName() + ": agent "+msg.getSender().getLocalName().toUpperCase()+" out of negotiation! " + msg.getPerformative( msg.getPerformative() ));
			}
		}

		// no proposals available
		if (receivedProposals.size() == 0) {
			System.err.println(""+myAgent.getLocalName() + ": all Enterprise Agents quit!!!\n");
			success = false;
			return;
		}

		//send feedBack: creates and sends a counter proposal to the respective enterprise agent			
		ProposalSet proposalSet = new ProposalSet(receivedProposals);
		generateFeedback(proposalSet, need, getRiskTolerance(), agents); 
		
		
//		if (round == 1 && receivedProposals.size() == 1) { //
//			//um agente apenas no inicio da negociacao....
//			//definir um protocolo especifico....
//			success = proposalAnalysisLastRound( receivedProposals, acceptances );
//			System.out.println("ATTENTION: FIRST ROUND WITH ONLY 1 PROPOSAL!!! ROUND: " +round);
//			//success = proposalAnalysisLastRound(proposalSet, acceptances);
//			return;
//		}
		
		ProposalEvaluation bestProposalEvaluation = (ProposalEvaluation) extractMsgContent(proposalSet.getBestMsgProposal());
		
		//for graph and utility chart
		Vector<ProposalEvaluation> proposalEvaluations = new Vector<ProposalEvaluation>();
		for(ACLMessage msg: proposalSet.getMsgProposals()) {
			ProposalEvaluation propEvaluation = (ProposalEvaluation) extractMsgContent(msg);
			proposalEvaluations.add(propEvaluation);
		}
		receivedProposalsPerRound.add(new NegotiationRoundProposalEvaluations(proposalEvaluations, bestProposalEvaluation, round));
		
		// last round or only 1 proposal
		if (round == getMaxNumberOfRounds()) { //|| receivedProposals.size() == 1
			success = proposalAnalysisLastRound(proposalSet, acceptances);
			//calcula a media das utilidades das propostas da ultima rounda e o seu desvio padrao
			float lastRoundUtility = 0.0f;
			Vector<Double> utilities = new Vector<Double>();
			for(ACLMessage msg: proposalSet.getMsgProposals()) {
				ProposalEvaluation proposalEvaluation = (ProposalEvaluation) extractMsgContent(msg);
				utilities.add((double)proposalEvaluation.getEvaluation());
				lastRoundUtility += proposalEvaluation.getEvaluation();
			}
			
			lastRoundAvgUtility = (lastRoundUtility/proposalSet.getMsgProposals().size());
			standardDeviation = STDev.computeSDDvt(utilities, lastRoundAvgUtility);

			return;
		}

		for(ACLMessage msg: proposalSet.getMsgProposals()) {
			Proposal proposal = extractMsgContent(msg);
			newCfps.add(createNewMsgReply(msg, proposal));
		}

		//increases the number of current round
		round++;			
		//initiates a new iteration    		    	
		this.newIteration(newCfps);
	}

	private ACLMessage createNewMsgReply(ACLMessage msg, Proposal proposal) {
		ACLMessage msgReply = msg.createReply();
		
		msgReply = msg.createReply();
		msgReply.setPerformative(ACLMessage.CFP); 
		msgReply.setLanguage(((QFNegotiationMediator) myAgent).getSlCodec().getName());
		msgReply.setOntology(ElectronicInstitution.QF_NEGOTIATION_ONTOLOGY);

		//insert Request Object as the content element of this aclmessage
		try {
			myAgent.getContentManager().fillContent(msgReply, proposal);
		} catch(OntologyException oe) {
			oe.printStackTrace();
		} catch(Codec.CodecException cex) {
			cex.printStackTrace();
		}
		Date date = new Date();
		date.setTime(date.getTime()+PROPOSAL_WAITING_TIME);  
		msgReply.setReplyByDate(date);
		
        return msgReply; 
	}
	
	private boolean proposalAnalysisLastRound(ProposalSet ps, Vector<ACLMessage> answers) {

		if(ps.getMsgProposals().isEmpty()) {
			System.out.println("There is not a winning proposition in the proposalset object!!!");
			return false;
		}

		ACLMessage bestMsg  = ps.getBestMsgProposal(); 			
		ProposalEvaluation bestProposalEvaluation = (ProposalEvaluation) extractMsgContent(bestMsg);

		//removes winner proposal this set and sends reject to every loser
		for( ACLMessage msg: ps.getMsgProposals() ) {
			if( !msg.getSender().equals(bestMsg.getSender()) ) {
				msg=msg.createReply();	
				msg.setPerformative(ACLMessage.REJECT_PROPOSAL);	    				
				answers.addElement(msg);
			}
		}
		//winner will receive accept only later, when all needs are negotiated successfully
		bestMsg = bestMsg.createReply();
		if ( bestProposalEvaluation.getEvaluation() > EVAL_MIN_ACCEPT ) {
			bestMsg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			answers.addElement(bestMsg);
			this.winnerProposal = bestProposalEvaluation;
			
			// contract drafting (fines)
			if(isUseTrustInContractDrafting()) {
				int i;
				for(i=0; !agents.get(i).getAgent().equals(winnerProposal.getIssuer()); i++);
				if(i<agents.size()) {   // should always be true
					//System.out.println("Fetching fine for " + agents.get(i).getAgent().getLocalName() + ": " + computeFine(agents.get(i)));
					this.winnerProposal.setFine(computeFine(agents.get(i)));
				}
			}
			
			return true;
		} else {
			bestMsg.setPerformative(ACLMessage.REJECT_PROPOSAL);
			answers.addElement(bestMsg);
			return false;
		}
	}

	protected void handleInform(ACLMessage inform) {
//		System.out.println("\n"+myAgent.getLocalName() + inform.getSender().getLocalName()+" accepted proposal!");
	}
	
	protected void handleRefuse(ACLMessage refuse) {
		String r = refuse.getContent();
		if (r != null && r.equals("refused...")) {
			numberOfRefusedSuppliers++;
		}
	}
	
	protected void handleFailure(ACLMessage failure) {
	}
	
	protected void handleNotUnderstood(ACLMessage notUnderstood) {
	}
	
	/**
	 * Extract the content (proposal) of a ACLmessage
	 * @param msg
	 * @return Proposal
	 */
	private Proposal extractMsgContent(ACLMessage msg) {
		Proposal proposal = null;
		try{
			ContentElement contentElement = myAgent.getContentManager().extractContent(msg);
			proposal = (Proposal) contentElement;
		}
		catch(Codec.CodecException cex){
			cex.printStackTrace();
		}
		catch(OntologyException oe){
			oe.printStackTrace();
		}
		return proposal;
	}
	
	/**
     * The agent Negotiation Mediatior (NegMed from now on) receives a set of Proposals from participating Sellers in each negotiation round and evaluates each one 
     * in order to provide a qualitative feedback for each proposed attribute value. This feedback can consist of the following comments: "excellent", sufficient, 
     * "bad" or "very bad".
     * The proposal evaluation can be divided into two parts:
     * <ol>
     * <li> Proposals initial evaluation.
     * 		Initially, the utility value of each proposal is calculated based on EA-starter's preferred values for each attribute. For that, we obtain the sum of the
     * 		differences of each proposed attribute value to the EA-starter's preference -- a deviation. The utility value is obtained using the following formula:
     * 		1/deviation.
     *      Each attribute in each proposal is tagged with the direction ("up" or "down") it should take towards the EA-starter's prefered values.
     *      At the end of this evaluation, the best proposal is determined.
     * <li> Proposal classification.
     * 		The best proposal is used as a target for attribute classification of each proposal ("excellent", "sufficient", "bad" or "very bad").
     * 		A difference delta between the attribute value in the best proposal and in the current proposal is calculated: (currentProposalAttDiff - bestProposalAttDiff) / bestProposalAttDiff.
     * 		This value is used to assign the qualitative feedback introduced above.
     * </ol>
     * 
	 * @param proposalSet	The set of proposals to be evaluated; feedback is inserted into these objects
	 * @param need		The need that is being negotiated
	 */
	protected void generateFeedback(ProposalSet proposalSet, Need need, double riskT, Vector<InterestedAgent> agents){

		Vector<ACLMessage> msgForClassification = new Vector<ACLMessage>();	
		Vector<ACLMessage> msgForNextRound = new Vector<ACLMessage>();		
		
        //the best proposal is initially the first one			
		ACLMessage bestMsg=proposalSet.getMsgProposals().firstElement();
		ProposalEvaluation bestProposalEvaluation = null;
		
		Hashtable<String, Float> attributeDifferences = new Hashtable<String, Float>(); // supplierName_competenceType_attributeName --> difference
		
		//starts the evaluation of every proposal
		for(ACLMessage msg: proposalSet.getMsgProposals()) {
			Proposal proposal = extractMsgContent(msg);
			ProposalEvaluation proposalEvaluation = new ProposalEvaluation(proposal);
			
			float difference, somatorioDiff = 0.0f;
			//faz a avaliação de cada atributo da proposta extraída anteriormente 			
			for(int i=0; i<proposal.getAttributeValues().size(); i++) {
				Attribute clientAttribute = (Attribute) need.getAttributes().get(i);
				Object clientPrefValue = clientAttribute.getPreferredValue().toString();
				String clientAttName = clientAttribute.getName();
				
				AttributeValue attributeValueInProposal = (AttributeValue) proposal.getAttributeValue(clientAttName);
				AttributeValueEvaluation attributeValueEvaluation = new AttributeValueEvaluation(attributeValueInProposal);
				
				if(clientAttribute.isDiscrete()) {
					// if the proposed value for this attribute is the one preferred, difference = 0
					if(attributeValueInProposal.getValue().equals(clientPrefValue)) {
						difference = 0.0f;
					} else {
						difference = 1.0f;
					}
					
				} else {   // continuous attribute

					// compute size of attribute domain and distance between preferred and proposed values
					float preferredValue, proposedValue, distancePrefToProp;
					float domainMin, domainMax, diffDomain;
					if(clientAttribute.getType().equals("integer")) {
						
						preferredValue = Integer.parseInt((String) clientPrefValue);
						
						proposedValue = Integer.parseInt(attributeValueInProposal.getValue());
						domainMin = Integer.parseInt((String) clientAttribute.getContinuousDomainMin());
						domainMax = Integer.parseInt((String) clientAttribute.getContinuousDomainMax());
					} else {
						preferredValue = Float.parseFloat((String) clientPrefValue);
						proposedValue = Float.parseFloat(attributeValueInProposal.getValue());
						domainMin = Float.parseFloat((String) clientAttribute.getContinuousDomainMin());
						domainMax = Float.parseFloat((String) clientAttribute.getContinuousDomainMax());
					}
					diffDomain = Math.abs(domainMax - domainMin);
					distancePrefToProp = preferredValue - proposedValue;
					
//					// check if proposed value is within limits
					if(domainMin <= proposedValue && proposedValue <= domainMax) {
						difference = Math.abs(distancePrefToProp)/diffDomain;   //the closer to 0 the better
					} else {
						difference = 1.0f;
					}
					
					// check if the preferred value is higher or lower than the proposed one
					if(distancePrefToProp < 0){   // preferred is lower
						attributeValueEvaluation.setDirection(AttributeValueEvaluation.AttributeDirection.DOWN);
					} else if (distancePrefToProp > 0) {   // preferred is higher
						attributeValueEvaluation.setDirection(AttributeValueEvaluation.AttributeDirection.UP);
					} else {
						attributeValueEvaluation.setDirection(AttributeValueEvaluation.AttributeDirection.MAINTAIN);
					}
				}
				
				attributeDifferences.put(proposalEvaluation.getIssuer().getLocalName() +"_"+ proposalEvaluation.getNeedType() +"_"+ attributeValueEvaluation.getName(), difference);
				somatorioDiff += difference;
				
				proposalEvaluation.getAttributeValueEvaluations().add(attributeValueEvaluation);
			} //fim loop avaliação de cada atributo

			float desvio = somatorioDiff/proposal.getAttributeValues().size();
			float evaluation = (1 - desvio);
			if(isUseTrustInProposalEvaluation())
			{
				double evaluationPercent = riskT;
				double trustPercent = 1 - evaluationPercent;
				evaluation = (float) ((evaluationPercent * evaluation) +
										(trustPercent * getAgent(agents, msg.getSender().getLocalName()).getCtrEvaluation()));
			}
			
			proposalEvaluation.setEvaluation(evaluation);
			if(bestProposalEvaluation != null) {
				
//				if(isUseTrustInProposalEvaluation())
//				{
//					double evaluationPercent = riskT;
//					double trustPercent = 1 - evaluationPercent;
//					double myEvaluation = (evaluationPercent*evaluation) +
//											(trustPercent*getAgent(agents, msg.getSender().getLocalName()).getCtrEvaluation());
//					double myBestProposalEvaluation = (evaluationPercent*bestProposalEvaluation.getEvaluation()) +
//														(trustPercent*getAgent(agents, bestProposalEvaluation.getIssuer().getLocalName()).getCtrEvaluation());
//					
//					if(myEvaluation >= myBestProposalEvaluation) {
//						bestProposalEvaluation = proposalEvaluation;
//						bestMsg = msg;
//					}
//				}
//				else
//				{
					if(evaluation >= bestProposalEvaluation.getEvaluation()) {
						bestProposalEvaluation = proposalEvaluation;
						bestMsg = msg;
					}
//				}
			} else {
				bestProposalEvaluation = proposalEvaluation;
				bestMsg = msg;
			}

			//atribui cada proposta (Proposal) avaliada na sua mensagem 
			try {
				myAgent.getContentManager().fillContent(msg, proposalEvaluation);
			} catch(OntologyException oe) {
				oe.printStackTrace();
			} catch(Codec.CodecException cex) {
				cex.printStackTrace();
			}
			//salva as propostas avaliadas anteriormente para a avaliação final (i.e. classificação ) 
			msgForClassification.addElement(msg);
		}//end loop

		//atribui a melhor proposta em sua mensagem
		try {
			myAgent.getContentManager().fillContent(bestMsg, bestProposalEvaluation);
		} catch(OntologyException oe) {
			oe.printStackTrace();
		} catch(Codec.CodecException cex) {
			cex.printStackTrace();
		}
		proposalSet.setBestMsgProposal(bestMsg);

		//final evaluation of the proposal
		for(ACLMessage msg: msgForClassification) {
			ProposalEvaluation proposalEvaluation = (ProposalEvaluation) extractMsgContent(msg);
			//compares each one with the best proposal and sets a classification to every attribute based on the proximity to the winning proposal
			for(int i=0; i<proposalEvaluation.getAttributeValueEvaluations().size(); i++) {
				AttributeValueEvaluation attributeValueEvaluation = (AttributeValueEvaluation) proposalEvaluation.getAttributeValueEvaluations().get(i);
				if(attributeValueEvaluation.isDiscrete()) {   // discrete attribute
					Attribute attribute = (Attribute) need.getAttributes().get(i);
					if(attributeValueEvaluation.getValue().equals(attribute.getPreferredValue())) {
						attributeValueEvaluation.setClassif(AttributeValueEvaluation.AttributeClassification.EXCELLENT);
					} else {
						attributeValueEvaluation.setClassif(AttributeValueEvaluation.AttributeClassification.BAD);
					}
				} else {   // continuous attribute
					if(proposalEvaluation.getIssuer().getLocalName().equals(bestProposalEvaluation.getIssuer().getLocalName())) {
						attributeValueEvaluation.setClassif(AttributeValueEvaluation.AttributeClassification.EXCELLENT);
					} else { //as propostas sao classificadas de acordo com a distância à melhor proposta
						float difference = attributeDifferences.get(proposalEvaluation.getIssuer().getLocalName() +"_"+ proposalEvaluation.getNeedType() 
										+"_"+attributeValueEvaluation.getName());
						float delta;
						if(difference == 0){
							delta = 0.0f;
						} else {
							float diffPrefBestProposal = attributeDifferences.get(bestProposalEvaluation.getIssuer().getLocalName() +"_"+ bestProposalEvaluation.getNeedType() 
										+"_"+attributeValueEvaluation.getName());
							delta = (difference - diffPrefBestProposal) / diffPrefBestProposal;
						}
						
						if(delta < 0.005) {
							attributeValueEvaluation.setClassif(AttributeValueEvaluation.AttributeClassification.EXCELLENT);
						} else if(delta < 0.5) {
							attributeValueEvaluation.setClassif(AttributeValueEvaluation.AttributeClassification.SUFFICIENT);
						} else if(delta < 1.5) {
							attributeValueEvaluation.setClassif(AttributeValueEvaluation.AttributeClassification.BAD);
						} else {
							attributeValueEvaluation.setClassif(AttributeValueEvaluation.AttributeClassification.VERY_BAD);
						}
					}
					
				}
			}
			
			//insert Proposal Object as the content element of this aclmessage
			try{
				myAgent.getContentManager().fillContent(msg, proposalEvaluation);
			}
			catch(OntologyException oe){
				oe.printStackTrace();
			}
			catch(Codec.CodecException cex){
				cex.printStackTrace();
			}
			msgForNextRound.addElement(msg);
		}//end final evaluation of the proposal loop
		proposalSet.setMsgProposals(msgForNextRound);
	} // end generateFeedback method
	
	private InterestedAgent getAgent(Vector<InterestedAgent> agents, String name) {
		for(int i =0; i < agents.size(); i++) {
			if(agents.get(i).getAgent().getLocalName().equalsIgnoreCase(name)) {
				return agents.get(i);
			}
		}
		return null;
	}

	public boolean isSuccess() {
		return success;
	}
	public float getLastRoundAvgUtility() {
		return lastRoundAvgUtility;
	}

	private double computeFine(InterestedAgent iA) {

		//production volume
		double pv = need.getProductionVolume();
		
		//transaction volume
		Attribute quantity = getNeed().getAttribute("quantity");
		double tv = Double.parseDouble(quantity.getPreferredValue().toString());
		
		//perceived risk, R
		double r = tv / pv * (1 - iA.getCtrEvaluation() / trustReducFactor);
		
		//risk aversion
		double ra = 1 - getRiskTolerance();
		
		//confidence threshold (Ct) that indicates the minimum confidence the buyer
		//needs for entering the transaction
		double ct = r * ra;
		
		//como vou buscar a confiabilidade do fornecedor?
		double fine = ct - iA.getCtrEvaluation() / trustReducFactor;
			
		//double fine = Math.min(0.3, (1-iA.getCtrEvaluation())*(1-getRiskTolerance()));
		//return Math.min(fine, 0.9*getPenaltyFd());
		
		return fine;
	}
	
	public ProposalEvaluation getWinnerProposal() {
		return winnerProposal;
	}

	public double getStandardDeviation() {
		return standardDeviation;
	}

	public int getNumberOfRefusedSuppliers() {
		return numberOfRefusedSuppliers;
	}

	public Vector<NegotiationRoundProposalEvaluations> getReceivedProposalsPerRound() {
		return receivedProposalsPerRound;
	}

	public boolean isUseTrustInProposalEvaluation() {
		try {
			return Boolean.parseBoolean(negotiationParameters.get(QFNegotiationParameters.USE_TRUST_IN_PROPOSAL_EVALUATION));
		} catch(Exception e) {
			return QFNegotiationParameters.USE_TRUST_IN_PROPOSAL_EVALUATION_DEFAULT;
		}
	}
	
	public boolean isUseTrustInContractDrafting() {
		try {
			return Boolean.parseBoolean(negotiationParameters.get(QFNegotiationParameters.USE_TRUST_IN_CONTRACT_DRAFTING));
		} catch(Exception e) {
			return QFNegotiationParameters.USE_TRUST_IN_CONTRACT_DRAFTING_DEFAULT;
		}
	}
	
	private int getMaxNumberOfRounds() {
		try {
			return Integer.parseInt(negotiationParameters.get(QFNegotiationParameters.NEGOTIATION_ROUNDS));
		} catch(Exception e) {
			return QFNegotiationParameters.NEGOTIATION_ROUNDS_DEFAULT;
		}
	}

	private double getRiskTolerance() {
		try {
			return Double.parseDouble(negotiationParameters.get(QFNegotiationParameters.RISK_TOLERANCE));
		} catch(Exception e) {
			return QFNegotiationParameters.RISK_TOLERANCE_DEFAULT;
		}
	}
	
	private String getContractType() {
		return (negotiationParameters.get(QFNegotiationParameters.CONTRACT_TYPE) != null) ? 
				negotiationParameters.get(QFNegotiationParameters.CONTRACT_TYPE) : QFNegotiationParameters.CONTRACT_TYPE_DEFAULT;
	}

}
