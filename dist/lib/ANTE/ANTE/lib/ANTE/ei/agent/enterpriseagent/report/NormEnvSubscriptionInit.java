package ei.agent.enterpriseagent.report;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;

import java.util.Vector;

import ei.EIAgent;
import ei.ElectronicInstitution;
import ei.agent.enterpriseagent.EnterpriseAgent;
import ei.agent.enterpriseagent.gui.EnterpriseAgentGUI;
import ei.onto.normenv.report.ContractEnd;
import ei.onto.normenv.report.ContractStart;
import ei.onto.normenv.report.DeadlineViolation;
import ei.onto.normenv.report.Denounce;
import ei.onto.normenv.report.Fulfillment;
import ei.onto.normenv.report.IRE;
import ei.onto.normenv.report.LivelineViolation;
import ei.onto.normenv.report.Obligation;
import ei.onto.normenv.report.Report;
import ei.onto.normenv.report.Violation;

/**
 * Implements the ne-report subscription initiator role.
 * 
 * @author hlc
 */
public class NormEnvSubscriptionInit extends SubscriptionInitiator {

	private static final long serialVersionUID = 2285997070944103132L;

	private AID normenv;

	private String contract_id;

	/**
	 * Creates a <code>NormEnvSubscriptionInit</code> instance
	 *
	 * @param agent	The agent that adds the behaviour.
	 */
	NormEnvSubscriptionInit(Agent agent, String contract_id) {
		super(agent, new ACLMessage(ACLMessage.SUBSCRIBE));

		normenv = ((EIAgent) myAgent).fetchAgent(ElectronicInstitution.SRV_NORMATIVE_ENVIRONMENT, false);
		this.contract_id = contract_id;
	}

	/**
	 * Prepares the subscription message.
	 */
	protected Vector<ACLMessage> prepareSubscriptions(ACLMessage subscription) {
		//Uma vez que o conversationID gerado, por vezes, ? igual para Supplier e Client, ent?o gera-se aqui um valor ?nico
		//Ao ter dois con
		String cID = myAgent.getLocalName()+"_"+contract_id;
		subscription.setConversationId(cID);
		subscription.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
		subscription.setLanguage(((EnterpriseAgent) myAgent).getLeapCodec().getName());
		subscription.setOntology(ElectronicInstitution.NE_REPORT_ONTOLOGY);
		subscription.addReceiver(normenv);
		IRE ire = new IRE();
		ire.setContext(contract_id);
		try {
			myAgent.getContentManager().fillContent(subscription, ire);
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
	 * In this implementation, the message is added to the agent's gui. For certain report types, the trust records are updated.
	 * The method <code>handleNEReport(Report rep)</code> is invoked at the end.
	 */
	protected void handleInform(ACLMessage inform) {
		// check reported predicate
		Report report = null;
		try {
			// extract content
			ContentElement ce = myAgent.getContentManager().extractContent(inform);
			if(ce instanceof Report)
				report = (Report) ce;
			else 
				return;
		} catch(Codec.CodecException cex){
			cex.printStackTrace();
		} catch(OntologyException oe){
			oe.printStackTrace();
		}

		// no need to get the report's context, since it should be the same as contract_id (the contract subscribed on)
		//String contractId = report.getContext();

		// add message to hashtable
		if(report instanceof ContractStart ||
				report instanceof ContractEnd ||
				report instanceof Obligation ||
				report instanceof LivelineViolation ||
				report instanceof DeadlineViolation ||
				report instanceof Denounce ||
				report instanceof Violation ||
				report instanceof Fulfillment) {
			Vector<Report> reports;
			if((reports = ((EnterpriseAgent) myAgent).getContractReports(contract_id)) != null) { // should always be true
				reports.add(report);
				if(((EIAgent) myAgent).getGUI() != null) {
					((EnterpriseAgentGUI) ((EIAgent) myAgent).getGUI()).refreshContractReportsTable(contract_id);
				}
			}
		}

		if(report instanceof ContractEnd) {
			// remove the subscription init from the neSubscriptions hashtable
			((EnterpriseAgent) myAgent).getNeSubscriptions().remove(contract_id);
			// cancel this subscription
			cancelSubscription();
			
			// decrement active contracts
			((EnterpriseAgent) myAgent).decNumberOfActiveContracts();
		} else if(report instanceof ContractStart) {
			// increment active contracts
			((EnterpriseAgent) myAgent).incNumberOfActiveContracts();
		}

		// invoke method for further processing of the reported event
		((EnterpriseAgent) myAgent).handleNEReport(report);
	}

	public void cancelSubscription() {
		super.cancel(normenv, true);
	}

	protected void handleRefuse(ACLMessage refuse) {
		((EIAgent) myAgent).log("*" + myAgent.getLocalName() + " NormEnv subscription refused on " + contract_id + ", giving up");
		// remove the subscription init from the neSubscriptions hashtable
		((EnterpriseAgent) myAgent).getNeSubscriptions().remove(contract_id);
	}

} // end NormEnvSubscriptionInit class

