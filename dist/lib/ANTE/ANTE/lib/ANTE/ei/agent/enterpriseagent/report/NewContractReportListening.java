package ei.agent.enterpriseagent.report;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Vector;

import ei.EIAgent;
import ei.ElectronicInstitution;
import ei.agent.enterpriseagent.EnterpriseAgent;
import ei.agent.enterpriseagent.gui.EnterpriseAgentGUI;
import ei.onto.normenv.report.NewContract;
import ei.onto.normenv.report.Report;

/**
 * Implements a listening role for new contract reports from the normative environment.
 * Receives inform messages of the report ontology whose content is a new contract report.
 * The MessageTemplate used in this listening behaviour has been carefully designed not to collide with messages obtained in NormEnvSubscriptionInit.
 * Here we only deal with NewContract reports, while NormEnvSubscriptionInit deals with IRE reports.
 * 
 * @author hlc
 */
public class NewContractReportListening extends CyclicBehaviour {

	private static final long serialVersionUID = 2130794273613266404L;

	private MessageTemplate mt =
		MessageTemplate.and(
				MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchOntology(ElectronicInstitution.NE_REPORT_ONTOLOGY)),
						new MessageTemplate(new MatchContentNewContractReport()));

	public void action() {
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			NewContract newContract = null;
			try {
				// extract content
				newContract = (NewContract) myAgent.getContentManager().extractContent(msg);
			} catch(Codec.CodecException cex) {
				cex.printStackTrace();
			} catch(OntologyException oe) {
				oe.printStackTrace();
			}

			// if not in synchronized experiment, subscribe the normative environment for reports regarding this contract
			// (in synchronized experiment mode subscriptions are implicit, that is, agents will get notifications regarding all their contracts)
			if(!((EnterpriseAgent) myAgent).isInSynchronizedExperiment()) {
				// subscribe the normative environment for reports regarding this contract
				NormEnvSubscriptionInit neSI = new NormEnvSubscriptionInit(myAgent, newContract.getContext());
				myAgent.addBehaviour(neSI);
				((EnterpriseAgent) myAgent).getNeSubscriptions().put(newContract.getContext(), neSI);
				((EIAgent) myAgent).log("+" + myAgent.getLocalName() + ": subscribing NormEnv on " + newContract.getContext());
			}

			// check if this contract is already stored (it was stored if there was a signing process - not synchronized experiment mode)
			if(!((EnterpriseAgent) myAgent).getContractIds().contains(newContract.getContext())) {
				((EnterpriseAgent) myAgent).getContractIds().add(newContract.getContext());
				if(((EIAgent) myAgent).getGUI() != null) {
					((EnterpriseAgentGUI) ((EIAgent) myAgent).getGUI()).addContractRow(newContract.getContext());
				}
			}

			Vector<Report> reports = ((EnterpriseAgent) myAgent).getContractReports(newContract.getContext());
			if(reports == null) {
				reports = new Vector<Report>();
				((EnterpriseAgent) myAgent).getContractReports().put(newContract.getContext(), reports);
			}

			// add the NewContract report to the reports list
			reports.insertElementAt(newContract, 0);
		} else {
			block();
		}
	}

	
	/**
	 * A custom match for new contract reports, except for social contracts.
	 * Used in NewContractReportListening.
	 * 
	 * @author hlc
	 */
	private class MatchContentNewContractReport implements MessageTemplate.MatchExpression {

		private static final long serialVersionUID = -249807887161060640L;

		public boolean match(ACLMessage msg) {
			try {
				// extract content
				ContentElement ce = myAgent.getContentManager().extractContent(msg);
				// check if content is a new contract report and not a social-contract
				if(ce instanceof NewContract && !((NewContract) ce).getType().equals("social-contract")) {
					return true;
				}
			} catch(Codec.CodecException cex){
				cex.printStackTrace();
				return false;
			} catch(OntologyException oe){
				oe.printStackTrace();
				return false;
			}

			return false;
		}

	} // end MatchContentNewContractReport class


} // end NewContractReportListening class

