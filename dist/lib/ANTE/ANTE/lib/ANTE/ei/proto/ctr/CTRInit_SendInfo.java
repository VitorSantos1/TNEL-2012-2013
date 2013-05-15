package ei.proto.ctr;

import java.util.Date; 
import java.util.Vector;

import ei.EIAgent;
import ei.ElectronicInstitution;
import ei.onto.ctr.Info;
import ei.onto.ctr.SendInfo;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * The CTRInit_SendTopAgents represents the partner’s preselection protocol.
 * This protocol sends a list of agents that are interested in participating to the CompTrust agent, in order to shorten this list to the most trustworthy agents only.
 * This step will only take place if the EA-starter says so in his Negotiate request (which includes two parameters for this).
 * @author Sérgio Moura	
 */
public class CTRInit_SendInfo extends AchieveREInitiator {
	private static final long serialVersionUID = 1L;

	private AID trustedAgent;
	//FIXME a completar com a informação que precisar
	
	/**
	 * @param agent
	 * @param trustedAgent
	 */
	public CTRInit_SendInfo (Agent agent, AID trustedAgent) {
		super(agent,new ACLMessage(ACLMessage.REQUEST));
		this.trustedAgent = trustedAgent;
	
	}

	protected Vector<ACLMessage> prepareRequests(ACLMessage msg) {
		Vector<ACLMessage> messages = new Vector<ACLMessage>();
		AID reputation = ((EIAgent) myAgent).fetchAgent(ElectronicInstitution.SRV_CTR, false);			
		if(reputation != null) {
			messages = new Vector<ACLMessage>(); 
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			msg.setLanguage(((EIAgent) myAgent).getSlCodec().getName());
			msg.setOntology(ElectronicInstitution.CTR_ONTOLOGY);
			msg.addReceiver(reputation);
			Date date = new Date();
			date.setTime(date.getTime()+10000);
			SendInfo sendInfo = new SendInfo();
			sendInfo.setAgent(this.trustedAgent.getLocalName());
			
		
			Action a = new Action(reputation, sendInfo);
			try {
				myAgent.getContentManager().fillContent(msg, a);
			} catch(Codec.CodecException ce) {
				ce.printStackTrace();
			} catch(OntologyException oe) {
				oe.printStackTrace();
			}
			messages.add(msg);
		}
		return messages;
	}

	protected void handleInform(ACLMessage inform) {
//		Info info = null;
		try {
			ContentElement ce = myAgent.getContentManager().extractContent(inform);
			if(ce instanceof Info) {
//				info = (Info) ce;
			}
		} catch(Codec.CodecException cex) {
			cex.printStackTrace();
		} catch(OntologyException oe) {
			oe.printStackTrace();
		}
	}

	protected void handleRefuse(ACLMessage refuse) {
		((EIAgent) myAgent).log(myAgent.getLocalName()+"algum errou ocorreu: mensagem REFUSE. nao foi possivel retornar a lista de topAgent!!!");
	}
	
	protected void handleFailure(ACLMessage failure) {
		((EIAgent) myAgent).log("algum errou ocorreu: mensagem FAILURE. nao foi possivel retornar a lista de topAgent!!!");		
	}
	
}
