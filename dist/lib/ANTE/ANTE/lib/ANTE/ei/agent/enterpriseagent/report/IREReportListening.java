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
import ei.onto.normenv.report.IRE;
import ei.onto.normenv.report.Report;

/**
 * Listening role for IRE reports from the normative environment.
 * This behaviour is used only in synchronized experiment mode, in which case no subscriptions are created for contracts.
 * Receives inform messages of the report ontology whose content is an IRE.
 * The MessageTemplate used in this listening behaviour has been designed to capture messages that when not in synchronized experiment mode are handled
 * by NormEnvSubscriptionInit. Only IRE reports are handled, not NewContract reports.
 * 
 * @author hlc
 */
public class IREReportListening extends CyclicBehaviour {

	private static final long serialVersionUID = 2130794273613266404L;

	private MessageTemplate mt =
		MessageTemplate.and(
				MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchOntology(ElectronicInstitution.NE_REPORT_ONTOLOGY)),
						new MessageTemplate(new MatchContentIREReport()));

	public void action() {
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			IRE ire = null;
			try {
				// extract content
				ire = (IRE) myAgent.getContentManager().extractContent(msg);
			} catch(Codec.CodecException cex) {
				cex.printStackTrace();
			} catch(OntologyException oe) {
				oe.printStackTrace();
			}

			// invoke method for further processing of the reported event
			((EnterpriseAgent) myAgent).handleNEReport(ire);

			Vector<Report> reports = ((EnterpriseAgent) myAgent).getContractReports(ire.getContext());
			if(reports == null) {   // should always be true
				((EIAgent) myAgent).logErr("Warning: Received IRE report before the corresponding NewContract report: " + ire.getContext());
				reports = new Vector<Report>();
				((EnterpriseAgent) myAgent).getContractReports().put(ire.getContext(), reports);
			}

			// add the IRE report to the reports list
			reports.add(ire);

			if(((EIAgent) myAgent).getGUI() != null) {
				((EnterpriseAgentGUI) ((EIAgent) myAgent).getGUI()).refreshContractReportsTable(ire.getContext());
			}

		} else {
			block();
		}
	}
	

	/**
	 * A custom match for ire reports.
	 * Used in IREReportListening.
	 * 
	 * @author hlc
	 */
	private class MatchContentIREReport implements MessageTemplate.MatchExpression {

		private static final long serialVersionUID = -249807887161060640L;

		public boolean match(ACLMessage msg) {
			try {
				// extract content
				ContentElement ce = myAgent.getContentManager().extractContent(msg);
				// check if content is an IRE report
				if(ce instanceof IRE) {
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

	} // end MatchContentIREReport class

	
} // end IREReportListening class

