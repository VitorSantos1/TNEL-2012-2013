package ei.proto.normenv.report;

import java.util.Vector;

import ei.EIAgent;
import ei.ElectronicInstitution;
import ei.onto.normenv.report.Report;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;

/**
 * Inner class to implement the ne-report subscription initiator role.
 * 
 * @author hlc
 */
public class NEReportInit_SendReports extends SubscriptionInitiator {
	
	private static final long serialVersionUID = 2285997070944103132L;
	
	private AID normenv;
	
	/**
	 * Creates a <code>NormEnvSubscriptionInit</code> instance
	 *
	 * @param agent	The agent that adds the behaviour.
	 */
	protected NEReportInit_SendReports(Agent agent) {
		super(agent, new ACLMessage(ACLMessage.SUBSCRIBE));
		normenv = ((EIAgent) myAgent).fetchAgent(ElectronicInstitution.SRV_NORMATIVE_ENVIRONMENT, false);
	}
	
	/**
	 * Prepares the subscription message.
	 */
	protected Vector<ACLMessage> prepareSubscriptions(ACLMessage subscription) {
		subscription.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
		subscription.setLanguage(((EIAgent) myAgent).getLeapCodec().getName());
		subscription.setOntology(ElectronicInstitution.NE_REPORT_ONTOLOGY);
		subscription.addReceiver(normenv);
		Report report = new Report();
		try {
			myAgent.getContentManager().fillContent(subscription, report);
		} catch(Codec.CodecException ce) {
			ce.printStackTrace();
		} catch(OntologyException oe) {
			oe.printStackTrace();
		}
		
		Vector<ACLMessage> v = new Vector<ACLMessage>();
		v.addElement(subscription);
		return v;
	}
	
	/**
	 * Handles an inform message from the Normative Environment.
	 */
	protected void handleInform(ACLMessage inform) {
	}
	
	public void cancelSubscription() {
		super.cancel(normenv, true);
	}
	
}
