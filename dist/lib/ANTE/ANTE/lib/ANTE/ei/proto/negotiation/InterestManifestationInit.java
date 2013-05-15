package ei.proto.negotiation;

import java.util.Date;
import java.util.Vector;

import ei.EIAgent;
import ei.ElectronicInstitution;
import ei.agent.enterpriseagent.ontology.Need;
import ei.onto.negotiation.InterestManifestationCall;
import ei.service.negotiation.qfnegotiation.InterestedAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Initiator role for a FIPA-QUERY protocol, to query agents about their interest in participating in a negotiation.
 * A QUERY_IF message with an InterestManifestationCall is sent to agents. These will answer by confirming or not their interest in
 * participating in the negotiation.
 * 
 * @author pbn, hlc
 */
public class InterestManifestationInit extends AchieveREInitiator {
	private static final long serialVersionUID = 1L;
	
	private Vector<InterestedAgent> enterpriseAgents=null;
	
	private Need need = null;                                   		
	private String contractType;
	private AID requester;
	private Vector<InterestedAgent> interestedEnterpriseAgents = new Vector<InterestedAgent>();  

	public InterestManifestationInit(Agent agent, Need need, String contractType, AID requester, Vector<InterestedAgent> agents) {
		super(agent,new ACLMessage(ACLMessage.QUERY_IF));
		
		this.need = need;
		this.contractType = contractType;
		this.requester = requester;
		this.enterpriseAgents = agents;
		
	}
	
	public Need getNeed() {
		return need;
	}

	public Vector<InterestedAgent> getInterestedEnterpriseAgents() {
		return interestedEnterpriseAgents;
	}

	protected Vector<ACLMessage> prepareRequests(ACLMessage msg) {
		/**/
		if (msg == null) {
			  System.out.println("Initiation message = " + getDataStore().get(INITIATION_K));
		}
		/**/
		Vector<ACLMessage> messages = new Vector<ACLMessage>(); 
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
		msg.setOntology(ElectronicInstitution.NEGOTIATION_ONTOLOGY);
		msg.setLanguage(((EIAgent) myAgent).getSlCodec().getName());
		Date date = new Date();
		date.setTime(date.getTime()+10000);
		msg.setReplyByDate(date);
		for (InterestedAgent enterpriseAgent: enterpriseAgents) {
			msg.addReceiver(enterpriseAgent.getAgent());
		}
		try {
			InterestManifestationCall interestManifestationCall = new InterestManifestationCall();
			interestManifestationCall.setNeed(need);
			interestManifestationCall.setContractType(contractType);
			interestManifestationCall.setRequester(requester);
			myAgent.getContentManager().fillContent(msg, interestManifestationCall); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		messages.add(msg);
		return messages;
	}
	
	protected void handleInform(ACLMessage inform) {
		interestedEnterpriseAgents.addElement(new InterestedAgent(inform.getSender()));
	}
	
	protected void handleAgree(ACLMessage agree) {
		//log(agree.getSender().getLocalName() + "| agree: " + agree.getContent() + "");
	}
	
	protected void handleRefuse(ACLMessage refuse) {
		//log+": "+refuse.getSender().getLocalName() + ": I'm not interested!");
	}
	
	protected void handleFailure(ACLMessage failure) {
		//log(failure.getSender().getLocalName() + "| failure...");
	}

}
