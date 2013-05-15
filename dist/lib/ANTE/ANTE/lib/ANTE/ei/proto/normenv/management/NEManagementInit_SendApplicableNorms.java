package ei.proto.normenv.management;

import ei.EIAgent;
import ei.ElectronicInstitution;
import ei.onto.normenv.management.ApplicableNorms;
import ei.onto.normenv.management.SendApplicableNorms;

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

/**
 * Implementation of the ne-management initiator role, for the send-applicable-norms action.
 * Sends an ACL Message to the Normative Environment Agent requesting the applicable norms for a given contract type.
 */
public class NEManagementInit_SendApplicableNorms extends AchieveREInitiator {

	private static final long serialVersionUID = 1L;

	private AID normenv;
	private String contractType;
	private List applicableNorms;

	public NEManagementInit_SendApplicableNorms(Agent agent, AID normenv, String contractType) {
		super(agent, new ACLMessage(ACLMessage.REQUEST));
		this.normenv = normenv;
		this.contractType = contractType;
	}

	protected Vector<ACLMessage> prepareRequests(ACLMessage req) {
		Vector<ACLMessage> reqs = new Vector<ACLMessage>();

		req.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		req.setLanguage(((EIAgent) myAgent).getSlCodec().getName());
		req.setOntology(ElectronicInstitution.NE_MANAGEMENT_ONTOLOGY);
		req.addReceiver(normenv);

		// create a new SendApplicableNorms action
		SendApplicableNorms san = new SendApplicableNorms();
		san.setContractType(contractType);
		AID actor = normenv;
		Action act = new Action(actor,san);
		try {
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
			if(ce instanceof ApplicableNorms) {
				applicableNorms = ((ApplicableNorms) ce).getNorms();
				if(applicableNorms == null) {
					applicableNorms = new jade.util.leap.ArrayList();   // an empty list is received as null
				}
			}
		} catch(Codec.CodecException ce) {
			ce.printStackTrace();
		} catch(OntologyException oe) {
			oe.printStackTrace();
		}
	}

	public List getApplicableNorms() {
		return applicableNorms;
	}

}
