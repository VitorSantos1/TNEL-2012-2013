package ei.agent.enterpriseagent.signature;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.security.Signature;
import java.security.SignedObject;

import ei.EIAgent;
import ei.ElectronicInstitution;
import ei.agent.enterpriseagent.EnterpriseAgent;
import ei.agent.enterpriseagent.gui.EnterpriseAgentGUI;
import ei.contract.ContractWrapper;

/**
 * Implements the contract-signing responder role.
 * 
 * @hide Each participating agent will sign the contract only once, regardless of the fact that he may be participating as supplier of more than one item.
 * @hide Practical reason: a single instance of a responder role cannot handle more than one conversation in parallel. Therefore, if this agent were supposed to sign
 * @hide the same contract twice, the call for the second signature would only be processed when the signing conversation terminates, which will not until all the
 * @hide signatures are received!
 * 
 * TODO For now this role is based on the ContractNet protocol with the following semantics:
 * Original sequence			ContractNet sequence
 * -----------------			--------------------
 * REQUEST						CFP
 * INFORM(signature)			PROPOSE
 * CONFIRM/DISCONFIRM			ACCEPT_PROPOSAL/REJECT_PROPOSAL
 * 								INFORM
 * Should define a new protocol.
 * 
 * @author hlc
 */
public class ContractSigningResp_Based_on_ContractNet extends jade.proto.ContractNetResponder {

	private static final long serialVersionUID = -22733248507520666L;

	ContractWrapper cw = null;

	public ContractSigningResp_Based_on_ContractNet(Agent a) {
		super(a, MessageTemplate.and(	ContractNetResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchOntology(ElectronicInstitution.CONTRACT_SIGNING_ONTOLOGY)));
	}

	protected ACLMessage handleCfp(ACLMessage cfp) {
		ACLMessage reply = cfp.createReply();

		if(!((EIAgent) myAgent).providesService(new AID(cfp.getSender().getLocalName(), false), ElectronicInstitution.SRV_NOTARY)) {
			reply.setPerformative(ACLMessage.REFUSE);
			return reply;
		}

		// get contract
		cw = new ContractWrapper(((EnterpriseAgent) myAgent).getXsd_file());
		cw.unmarshal(cfp.getContent(), true);

		// check if this contract was waiting to be signed (in the to-be-signed contracts ArrayList)
		// FIXME: we are assuming that the contract came before from the NegotiationMediator as a result of the negotiation process
		//			if(toBeSignedContracts.remove(cw)) {   // remove will return true if an object in toBeSignedContracts equals() contract
		// sign the contract
		SignedObject so = null;
		try {
			Signature sig = Signature.getInstance(((EIAgent) myAgent).getPrivateKey().getAlgorithm());
			so = new SignedObject(cw.marshal(true), ((EIAgent) myAgent).getPrivateKey(), sig);
			reply.setContentObject(so);
		} catch (Exception e) {
			e.printStackTrace();
		}
		reply.setPerformative(ACLMessage.PROPOSE);
		//			} else {
		//				reply.setPerformative(ACLMessage.REFUSE);
		//			}
		return reply;
	}

	protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
		String contract_id = cw.getId();   // FIXME? I am not getting the contract id from the message, just using the local copy
		// contract is confirmed
		((EnterpriseAgent) myAgent).getContractIds().add(contract_id);
		((EnterpriseAgent) myAgent).addContract(contract_id, cw);

		if(((EIAgent) myAgent).getGUI() != null) {
			((EnterpriseAgentGUI) ((EIAgent) myAgent).getGUI()).addContractRow(contract_id);
		}

		// empty reply just to finish the contract-net protocol
		ACLMessage result = accept.createReply();
		result.setPerformative(ACLMessage.INFORM);
		return result;
	}

	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
		// contract is not confirmed
	}

} // end ContractSigningResp_Based_on_ContractNet class

