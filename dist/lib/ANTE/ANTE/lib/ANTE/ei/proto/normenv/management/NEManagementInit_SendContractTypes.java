package ei.proto.normenv.management;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.util.leap.List;

import java.util.Vector;

import ei.EIAgent;
import ei.ElectronicInstitution;
import ei.onto.normenv.management.ContractTypes;
import ei.onto.normenv.management.SendContractTypes;

/**
 * Implementation of the ne-management initiator role, for the send-contract-types action.
 * Sends an ACL Message to the Normative Environment Agent requesting the list of available contract types.
 */
public class NEManagementInit_SendContractTypes extends AchieveREInitiator {
	private static final long serialVersionUID = 1L;

	private AID normenv;
	private List contractTypes;

	public NEManagementInit_SendContractTypes(Agent ag, AID normenv) {
		super(ag,new ACLMessage(ACLMessage.REQUEST));
		this.normenv = normenv;

	}

	protected Vector<ACLMessage> prepareRequests(ACLMessage req) {
		Vector<ACLMessage> reqs = new Vector<ACLMessage>();

		req.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		req.setOntology(ElectronicInstitution.NE_MANAGEMENT_ONTOLOGY);
		req.setLanguage(((EIAgent) myAgent).getSlCodec().getName());
		req.addReceiver(normenv);

		// create a new SendContractTypes action
		SendContractTypes sct = new SendContractTypes();
		AID actor = normenv;
		Action act = new Action(actor,sct);
		try{
			myAgent.getContentManager().fillContent(req,act);
		} catch(OntologyException oe) {
			oe.printStackTrace();
		} catch(Codec.CodecException ce) {
			ce.printStackTrace();
		}

		reqs.add(req);
		return reqs;
	}

	protected void handleInform(ACLMessage inform) {
		try {
			ContentElement ce = myAgent.getContentManager().extractContent(inform);
			if(ce instanceof ContractTypes) {
				contractTypes = ((ContractTypes) ce).getTypes();
				if(contractTypes == null) {
					contractTypes = new jade.util.leap.ArrayList();   // an empty list is received as null
				}
			}
		} catch(Codec.CodecException ce) {
			ce.printStackTrace();
		} catch(OntologyException oe) {
			oe.printStackTrace();
		}
	}

	public List getContractTypes() {
		return contractTypes;
	}

}
