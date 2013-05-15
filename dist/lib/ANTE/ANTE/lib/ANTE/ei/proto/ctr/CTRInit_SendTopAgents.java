package ei.proto.ctr;

import java.util.Date; 
import java.util.Vector;

import ei.EIAgent;
import ei.ElectronicInstitution;

import ei.agent.enterpriseagent.ontology.Need;
import ei.onto.ctr.ContextualEvidence;
import ei.onto.ctr.SendTopAgents;
import ei.onto.ctr.TopAgents;
import ei.service.ctr.OutcomeGenerator;
import ei.service.ctr.OutcomeGenerator.MappingMethod;
import ei.service.negotiation.qfnegotiation.InterestedAgent;
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
 * @author pbn, hlc
 */
public class CTRInit_SendTopAgents extends AchieveREInitiator {
	private static final long serialVersionUID = 1L;
	
	private int topNumberOfAgents;
	private Vector<AID> interestedAgents;
	private boolean useContextual;
	private int quantity, dTime;
	private Need need;
	private MappingMethod mapMethod;
	private Vector<InterestedAgent> topAgents;	
	private AID requester;
	
	/**
	 * @param agent
	 * @param requester
	 * @param interestedAgents
	 * @param need
	 * @param topNumberOfAgents
	 * @param isSelectedContextual
	 * @param quantity
	 * @param dTime
	 * @param mapMethod
	 */
	public CTRInit_SendTopAgents (Agent agent, AID requester, Vector<InterestedAgent> interestedAgents, Need need, int topNumberOfAgents, boolean isSelectedContextual, int quantity, int dTime, OutcomeGenerator.MappingMethod mapMethod) {
		super(agent,new ACLMessage(ACLMessage.REQUEST));
		Vector<AID> interAgent = new Vector<AID>();
		
		if(interestedAgents != null)
		{
			for(InterestedAgent iA : interestedAgents)
				interAgent.add(iA.getAgent());
		}
		else
			interAgent = null;
		
		this.topNumberOfAgents = topNumberOfAgents;
		this.need = need;
		this.interestedAgents = interAgent;
		this.useContextual = isSelectedContextual;
		this.quantity = quantity;
		this.dTime = dTime;
		this.mapMethod = mapMethod;
		this.requester = requester;
	}
	
	public Need getNeed() {
		return need;
	}

	public Vector<InterestedAgent> getTopAgents() {
		return topAgents;
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
			SendTopAgents sendTopAgents = new SendTopAgents();
			sendTopAgents.setTopNumberOfAgents(topNumberOfAgents); 
			sendTopAgents.setInterestedAgents(interestedAgents);
			sendTopAgents.setRequester(requester);
			sendTopAgents.setMapMet(mapMethod);
			
			ContextualEvidence cE = null; //Se contractAux for nulo, é porque na interface a flag "useContextualReputation" não foi seleccionada e portanto não é preciso para cálculo				
			if(useContextual || topNumberOfAgents == SendTopAgents.NO_HANDICAP) { 
				sendTopAgents.setUseContextual(useContextual);
				cE = new ContextualEvidence();
				for (int i=0;i<need.getNumberOfAttributes();i++) {
					if( cE.getQuantity() == 0 ) {
						cE.setQuantity(quantity);
					}
					if( cE.getNeedName().equals("") ) {
						cE.setNeedName(need.getType());
					}
					if( cE.getDeliveryTime() == 0 ) {
						cE.setDeliveryTime(new Long(dTime));
					}
				}
			} else {
				sendTopAgents.setUseContextual(useContextual);
			}
			sendTopAgents.setContract(cE);
			Action a = new Action(reputation, sendTopAgents);
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
		TopAgents ta = null;
		try {
			ContentElement ce = myAgent.getContentManager().extractContent(inform);
			if(ce instanceof TopAgents) {
				ta = (TopAgents) ce;
				if(ta.getAgents() != null) {
					Vector<AID> ags = ta.getAgents();
					Vector<Double> val = ta.getCtrEvaluations();
					Vector<Double> valContext = ta.getCtrContextualEvaluations();
					
					topAgents = new Vector<InterestedAgent>();
					for(int i = 0; i < ags.size(); i++)
					{
						InterestedAgent iA = new InterestedAgent(ags.get(i));
						iA.setCtrEvaluation(val.get(i));
						iA.setContextualEvaluation(valContext.get(i));
						topAgents.add(iA);
					}
					
				}
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
