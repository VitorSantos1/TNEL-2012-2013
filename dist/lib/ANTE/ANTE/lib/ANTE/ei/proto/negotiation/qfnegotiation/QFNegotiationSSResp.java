package ei.proto.negotiation.qfnegotiation;

import java.util.Vector;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.SSIteratedContractNetResponder;

public abstract class QFNegotiationSSResp extends SSIteratedContractNetResponder {
	private static final long serialVersionUID = 1L;
	
	private String negotiationId;
	private Vector<ACLMessage> messages = new Vector<ACLMessage>(); 
	
	private boolean isFirstRound=true;
	
	public String getNegotiationId() {
		return negotiationId;
	}
	public Vector<ACLMessage> getMessages() {
		return messages;
	}

	public QFNegotiationSSResp(Agent agent, ACLMessage msg) {
		super(agent, msg);
		negotiationId = msg.getSender().getLocalName() + ":" + msg.getConversationId();
	}

	/**
	 * Handles a CFP message. This method is invoked when the first CFP is received, and is supposed to create the reply to be sent:
	 * either a PROPOSE (with a Proposal as content) or a REFUSE.
	 */
	public abstract ACLMessage handleFirstCfp(ACLMessage cfp);
	
	/**
	 * Handles a CFP message. This method is invoked when a CFP with feedback is received (from the second negotiation round onwards),
	 * and is supposed to create the reply to be sent: either a PROPOSE (with a Proposal as content) or a REFUSE.
	 */
	public abstract ACLMessage handleCfpWithFeedback(ACLMessage newCFP);	
	
	protected ACLMessage handleCfp(ACLMessage msgCfp) {
		//add message to the hashtable for the corresponding negotiation
		messages.add(msgCfp);

		ACLMessage response = null;
		if (isFirstRound) {
			isFirstRound = false;
			response = handleFirstCfp(msgCfp);
		} else {
			response = handleCfpWithFeedback(msgCfp);
		}

		//add message to the hashtable for the corresponding negotiation
		messages.add(response);
		return response;
	}

	protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
		//add message to the hashtable for the corresponding negotiation
		messages.add(accept);		

		ACLMessage inform;
		try {
			// // get contract
			// ContractWrapper cw = new ContractWrapper(((EnterpriseAgent) myAgent).xsd_file);
			// cw.unmarshal(messagesReceived.getContent(), true);
			// // save negotiated contract in the to-be-signed contracts ArrayList for future signing
			// if(!toBeSignedContracts.contains(cw))	// only one copy of the contract, since this agent will sign it only once!
			// toBeSignedContracts.add(cw);

			inform = accept.createReply();
			inform.setPerformative(ACLMessage.INFORM);

			//System.out.println(myAgent.getLocalName()+ ": I have won this negotiation!");

		} catch (Exception ue) {
			ue.printStackTrace();
			inform = accept.createReply();
			inform.setPerformative(ACLMessage.FAILURE);
		}
		return inform;
	}

	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
		//add message to the hashtable for the corresponding negotiation
		messages.add(reject);		
		//System.out.println(myAgent.getLocalName()+ ": I had REJECT_PROPOSAL!"); // REJECT_PROPOSAL: I lost the negotiation for this component...
	}
}

