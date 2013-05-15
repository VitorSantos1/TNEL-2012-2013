package ei.agent.enterpriseagent.negotiation;

import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.util.leap.List;

import java.util.Vector;

import ei.EIAgent;
import ei.ElectronicInstitution;
import ei.agent.enterpriseagent.EnterpriseAgent;
import ei.agent.gui.MsgViewerGUI;
import ei.onto.negotiation.Negotiate;
import ei.onto.util.Parameters;
import ei.service.negotiation.qfnegotiation.NegotiationOutcome;

/**
 * Implements the negotiate initiator role. Buyer EnterpriseAgents use this role in order to request a negotiation mediator to negotiate on their
 * behalf.
 * 
 * This protocol starts by sending a Negotiate action request, which includes the name of the protocol to be used, a set of protocol-specific
 * parameters and a Needs object. After a successful negotiation process, the negotiation mediator will send back the outcome of the negotiation
 * (a contract or failure to select appropriate suppliers).
 */
public class QFNegotiationInit_Negotiate extends AchieveREInitiator {
	private static final long serialVersionUID = 5915332397020918685L;

	private AID negotiationMediatorAID; 
	private List needs;
	private Parameters negotiationParameters;
	
	private NegotiationOutcome negotiationOutcome;

	public QFNegotiationInit_Negotiate(Agent ag, AID negotiationMediatorAID, List needs, Parameters negotiationParameters) {
		super(ag, new ACLMessage(ACLMessage.REQUEST));
		this.negotiationMediatorAID = negotiationMediatorAID;
		this.needs = needs;
		this.negotiationParameters = negotiationParameters;
	}

	public NegotiationOutcome getNegotiationOutcome() {
		return negotiationOutcome;
	}

	protected Vector<ACLMessage> prepareRequests(ACLMessage req) {
		Vector<ACLMessage> reqs = new Vector<ACLMessage>();
		req.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		req.setOntology(ElectronicInstitution.NEGOTIATION_ONTOLOGY);
		req.setLanguage(((EIAgent) myAgent).getSlCodec().getName());
		req.addReceiver(negotiationMediatorAID);
		Negotiate negotiate = new Negotiate(ElectronicInstitution.QF_NEGOTIATION_PROTOCOL);
		negotiate.setNegotiationParameters(((EnterpriseAgent) myAgent).fillInNegotiationParameters(negotiationParameters));
		negotiate.setNeeds(needs);
//		negotiate.setNeeds(((EnterpriseAgent) myAgent).getNeeds());

		AID actor = myAgent.getAID();
		Action act = new Action(actor, negotiate);
		try { //insert Request Object as the content element of this aclmessage
			myAgent.getContentManager().fillContent(req, act);
		} catch(OntologyException oe) {
			oe.printStackTrace();
		} catch(Codec.CodecException ce) {
			ce.printStackTrace();
		}
		reqs.add(req);
		return reqs;
	}

	protected void handleInform(ACLMessage inform) {
		if(!((EIAgent) myAgent).isInSynchronizedExperiment()) {
			//				//receive the contract generated from the successful negotiation
			//				ContractWrapper cw = new ContractWrapper(((EnterpriseAgent) myAgent).xsd_file);
			//				cw.unmarshal(inform.getContent(), true);
			//				// save negotiated contract in the to-be-signed contracts ArrayList for future signing
			//				if(!toBeSignedContracts.contains(cw)) {   // only one copy of the contract, since this agent will sign it only once!
			//					toBeSignedContracts.add(cw);
			//				}
		} else {
			try {
				negotiationOutcome = (NegotiationOutcome) inform.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}
	}

	protected void handleFailure(ACLMessage failure) {
		((EIAgent) myAgent).log("Negotiation failed!");
		if(((EIAgent) myAgent).getGUI() != null) {
			MsgViewerGUI mv = new MsgViewerGUI(failure, myAgent, ((EIAgent) myAgent).getGUI());
			mv.setVisible(true);
		}
		// forget the disrupted mediator...
		((EIAgent) myAgent).forgetAgent(ElectronicInstitution.SRV_NEGOTIATION_FACILITATOR, negotiationMediatorAID);
	}
	
	/**
	 * Handle refuse.
	 * @param refuse the refuse
	 */
	protected void handleRefuse(ACLMessage refuse) {
		((EIAgent) myAgent).log("Negotiation refused!");
		if(((EIAgent) myAgent).getGUI() != null) {
			MsgViewerGUI mv = new MsgViewerGUI(refuse, myAgent, ((EIAgent) myAgent).getGUI());
			mv.setVisible(true);
		}
	}

	protected void handleAgree(ACLMessage agree) {
		((EIAgent) myAgent).log(agree.getContent());
	}

	protected void handleNotUnderstood(ACLMessage notUnderstood) {
		((EIAgent) myAgent).log("Negotiation not understood!");
		if(((EIAgent) myAgent).getGUI() != null) {
			MsgViewerGUI mv = new MsgViewerGUI(notUnderstood, myAgent, ((EIAgent) myAgent).getGUI());
			mv.setVisible(true);
		}
	}
	
} // end NegotiationInit_Negotiate class

