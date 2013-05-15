package ei.agent.enterpriseagent.enactment;

import ei.ElectronicInstitution;
import ei.agent.enterpriseagent.EnterpriseAgent;
import ei.onto.normenv.illocution.CurrencyTransfered;
import ei.onto.normenv.illocution.Delivered;
import ei.onto.normenv.illocution.IllocutionOntology;
import ei.onto.normenv.illocution.MsgDelivered;
import ei.onto.normenv.report.*;
import ei.onto.transaction.*;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import jade.content.AgentAction;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.proto.SimpleAchieveREInitiator;

/**
 * This abstract class is the basis for developing agents that can (automatically) respond to events generated in the contract enactment phase.
 * Such events are received from the normative environment as a result of its contract monitoring task.
 * 
 * @author hlc
 */
public abstract class EnactmentEnterpriseAgent extends EnterpriseAgent {
	
	private static final long serialVersionUID = -5772457651326537140L;
	
	protected void setup() {
		super.setup();
		
		// register additional ontologies
		getContentManager().registerOntology(TransactionOntology.getInstance());
		getContentManager().registerOntology(IllocutionOntology.getInstance());
	}
	
	/**
	 * Will automatically respond to reports.
	 * 
	 * @param rep	The report received
	 */
	public final void handleNEReport(Report rep) {
		
		if(rep instanceof ContractStart) {
			handleContractStartReport((ContractStart) rep);
		} else if(rep instanceof ContractEnd) {
			handleContractEndReport((ContractEnd) rep);
		} else if(rep instanceof Obligation) {
			handleObligationReport((Obligation) rep);
		} else if(rep instanceof LivelineViolation) {
			handleLivelineViolationReport((LivelineViolation) rep);
		} else if(rep instanceof DeadlineViolation) {
			handleDeadlineViolationReport((DeadlineViolation) rep);
		} else if(rep instanceof Denounce) {
			handleDenounceReport((Denounce) rep);
		} else if(rep instanceof Violation) {
			handleViolationReport((Violation) rep);
		} else if(rep instanceof Fulfillment) {
			handleFulfillmentReport((Fulfillment) rep);
		}
	}
	
	/**
	 * Handles a contract start report.
	 * 
	 * @param contractStart
	 * @return
	 */
	protected abstract boolean handleContractStartReport(ContractStart contractStart);
	
	/**
	 * Handles a contract end report.
	 * 
	 * @param contractEnd
	 * @return
	 */
	protected abstract boolean handleContractEndReport(ContractEnd contractEnd);

	/**
	 * Handles an obligation report.
	 * 
	 * @param obl	The obligation report
	 * @return		<code>true</code> if this agent is the obligation's bearer and chooses to fulfill the obligation;
	 * 				<code>false</code> otherwise
	 */
	protected abstract boolean handleObligationReport(Obligation obl);
	
	/**
	 * Handles a liveline violation report.
	 * 
	 * @param lviol	The liveline violation to denounce
	 * @return		<code>true</code> if this agent is the obligation's counterparty and chooses to denounce the deadline violation;
	 * 				<code>false</code> otherwise
	 */
	protected abstract boolean handleLivelineViolationReport(LivelineViolation lviol);
	
	/**
	 * Handles a deadline violation report.
	 * 
	 * @param dviol	The deadline violation to denounce
	 * @return		<code>true</code> if this agent is the obligation's counterparty and chooses to denounce the deadline violation;
	 * 				<code>false</code> otherwise
	 */
	protected abstract boolean handleDeadlineViolationReport(DeadlineViolation dviol);
	
	/**
	 * Handles a denounce report.
	 * 
	 * @param denounce
	 * @return
	 */
	protected abstract boolean handleDenounceReport(Denounce denounce);
	
	/**
	 * Handles a violation report.
	 * 
	 * @param violation
	 * @return
	 */
	protected abstract boolean handleViolationReport(Violation violation);

	/**
	 * Handles a fulfillment report.
	 * 
	 * @param fulf	The fulfillment report
	 * @return		<code>true</code> if this agent does something in response to this report;
	 * 				<code>false</code> otherwise
	 */
	protected abstract boolean handleFulfillmentReport(Fulfillment fulf);
	
	/**
	 * Fulfills an obligation if this agent is the obligation's bearer.
	 * 
	 * @param obl	The obligation to fulfill
	 * @return		<code>true</code> if this agent is the obligation's bearer and is able to fulfill the obligation;
	 * 				<code>false</code> otherwise
	 */
	protected final boolean fulfillObligation(Obligation obl) {
		// check if this agent is the obligation's bearer (important if invoked from a subclass)
		if(!obl.getBearer().equals(this.getLocalName())) {
			return false;
		}
		
		String[] obligationFactTokens = obl.getFact().split(" ");
		
		if(isInSynchronizedExperiment()) {
			// fulfill by sending message directly to the normative environment

			ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
			inform.setLanguage(getSlCodec().getName());
			inform.setOntology(ElectronicInstitution.ILLOCUTION_ONTOLOGY);
			inform.addReceiver(fetchAgent(ElectronicInstitution.SRV_NORMATIVE_ENVIRONMENT, false));
			
			// what kind of obligation?
			if(obligationFactTokens[0].equals("payment")) {
				// payment ref ?ref from ?fr to ?to amount ?am
				CurrencyTransfered ct = new CurrencyTransfered();
				ct.setContext(obl.getContext());
				ct.setFrom_agent(getAID());
				ct.setRef(obligationFactTokens[2]);
				ct.setAmount(Double.parseDouble(obligationFactTokens[8]));
				ct.setTo_agent(new AID(obligationFactTokens[6], false));
				ct.setWhen(System.currentTimeMillis());
				try {
					getContentManager().fillContent(inform, ct);
				} catch(Codec.CodecException ce) {
					ce.printStackTrace();
					logErr("Error filling content with illocution");
				} catch(OntologyException oe) {
					oe.printStackTrace();
					logErr("Error filling content with illocution");
				}
			} else if(obligationFactTokens[0].equals("delivery")) {
				// delivery ref ?ref from ?fr to ?to product ?p quantity ?qt
				Delivered d = new Delivered();
				d.setContext(obl.getContext());
				d.setFrom_agent(getAID());
				d.setRef(obligationFactTokens[2]);
				d.setProduct(obligationFactTokens[8]);
				d.setQuantity(Integer.parseInt(obligationFactTokens[10]));
				d.setTo_agent(new AID(obligationFactTokens[6], false));
				d.setWhen(System.currentTimeMillis());
				try {
					getContentManager().fillContent(inform, d);
				} catch(Codec.CodecException ce) {
					ce.printStackTrace();
					logErr("Error filling content with illocution");
				} catch(OntologyException oe) {
					oe.printStackTrace();
					logErr("Error filling content with illocution");
				}
			} else if(obligationFactTokens[0].equals("msg-delivery")) {
				// msg-delivery ref ?ref from ?fr to ?to msg $?msg
				MsgDelivered md = new MsgDelivered();
				md.setContext(obl.getContext());
				md.setFrom_agent(getAID());
				md.setRef(obligationFactTokens[2]);
				StringBuffer sb = new StringBuffer();
				for(int i=8; i<obligationFactTokens.length; i++)
					sb.append(obligationFactTokens[i] + " ");
				md.setMsg(sb.toString());
				md.setTo_agent(new AID(obligationFactTokens[6], false));
				md.setWhen(System.currentTimeMillis());
				try {
					getContentManager().fillContent(inform, md);
				} catch(Codec.CodecException ce) {
					ce.printStackTrace();
					logErr("Error filling content with illocution");
				} catch(OntologyException oe) {
					oe.printStackTrace();
					logErr("Error filling content with illocution");
				}
			}
			
			send(inform);
			
		} else {
			// what kind of obligation?
			if(obligationFactTokens[0].equals("payment")) {
				AID bank = fetchAgent(ElectronicInstitution.ROLE_BANK, false);
				if(bank == null) return false;
				// payment ref ?ref from ?fr to ?to amount ?am
				TransferCurrency tc = new TransferCurrency();
				tc.setContext(obl.getContext());
				tc.setRef(obligationFactTokens[2]);
				tc.setAmount(Double.parseDouble(obligationFactTokens[8]));
				tc.setTo(new AID(obligationFactTokens[6], false));
				addBehaviour(new TransactionInit(this, bank, tc));
			} else if(obligationFactTokens[0].equals("delivery")) {
				AID delivery_tracker = fetchAgent(ElectronicInstitution.ROLE_DELIVERY_TRACKER, false);
				if(delivery_tracker == null) return false;

				// delivery ref ?ref from ?fr to ?to product ?p quantity ?qt
				Deliver d = new Deliver();
				d.setContext(obl.getContext());
				d.setRef(obligationFactTokens[2]);
				d.setProduct(obligationFactTokens[8]);
				d.setQuantity(Integer.parseInt(obligationFactTokens[10]));
				d.setTo(new AID(obligationFactTokens[6], false));
				addBehaviour(new TransactionInit(this, delivery_tracker, d));
			} else if(obligationFactTokens[0].equals("msg-delivery")) {
				AID messenger = fetchAgent(ElectronicInstitution.ROLE_MESSENGER, false);
				if(messenger == null) return false;

				// msg-delivery ref ?ref from ?fr to ?to msg $?msg
				DeliverMsg dm = new DeliverMsg();
				dm.setContext(obl.getContext());
				dm.setRef(obligationFactTokens[2]);
				StringBuffer sb = new StringBuffer();
				for(int i=8; i<obligationFactTokens.length; i++)
					sb.append(obligationFactTokens[i] + " ");
				dm.setMsg(sb.toString());
				dm.setTo(new AID(obligationFactTokens[6], false));
				addBehaviour(new TransactionInit(this, messenger, dm));
			}
		}
		return true;
	}
	
	/**
	 * Denounces a liveline-violation if this agent is the obligation's counterparty.
	 * 
	 * @param ire	The liveline-violation to denounce
	 * @return		<code>true</code> if this agent is the obligation's counterparty and is able to denounce
	 * 				<code>false</code> otherwise
	 */
	protected final boolean denounceLivelineViolation(LivelineViolation lviol) {
		// check if this agent is the obligation's counterparty (important if invoked from a subclass)
		if(!lviol.getObligation().getCounterparty().equals(this.getLocalName())) {
			return false;
		}
		
		// get messenger AID
		AID messenger = fetchAgent(ElectronicInstitution.ROLE_MESSENGER, false);
		if(messenger == null) return false;
		
		// denounce fact
		DeliverMsg dm = new DeliverMsg();
		dm.setContext(lviol.getContext());
		dm.setRef(lviol.getContext());
		dm.setMsg("denounce " + lviol.getObligation().getFact());
		dm.setTo(new AID(lviol.getObligation().getBearer(), false));
		addBehaviour(new TransactionInit(this, messenger, dm));
		
		return true;
	}

	/**
	 * Denounces a deadline-violation if this agent is the obligation's counterparty.
	 * 
	 * @param ire	The deadline-violation to denounce
	 * @return		<code>true</code> if this agent is the obligation's counterparty and is able to denounce
	 * 				<code>false</code> otherwise
	 */
	protected final boolean denounceDeadlineViolation(DeadlineViolation dviol) {
		// check if this agent is the obligation's counterparty (important if invoked from a subclass)
		if(!dviol.getObligation().getCounterparty().equals(this.getLocalName())) {
			return false;
		}

		// get messenger AID
		AID messenger = fetchAgent(ElectronicInstitution.ROLE_MESSENGER, false);
		if(messenger == null) return false;
		
		// denounce fact
		DeliverMsg dm = new DeliverMsg();
		dm.setContext(dviol.getContext());
		dm.setRef(dviol.getContext());
		dm.setMsg("denounce " + dviol.getObligation().getFact());
		dm.setTo(new AID(dviol.getObligation().getBearer(), false));
		addBehaviour(new TransactionInit(this, messenger, dm));
		
		return true;
	}
	
	
	protected class TransactionInit extends SimpleAchieveREInitiator {
		
		private static final long serialVersionUID = 8742310630851008461L;
		
		private AID to;
		private AgentAction content;
		
		TransactionInit(Agent agent, AID to, AgentAction content) {
			super(agent, new ACLMessage(ACLMessage.REQUEST));

			this.to = to;
			this.content = content;
		}
		
		protected ACLMessage prepareRequest(ACLMessage msg) {
			// set the protocol of the request to be sent
			msg.setProtocol(InteractionProtocol.FIPA_REQUEST);
			// set the language of the request to be sent
			msg.setLanguage(getSlCodec().getName());
			// set the ontology of the request to be sent
			msg.setOntology(ElectronicInstitution.TRANSACTION_ONTOLOGY);
			// add the envisaged agent as receiver of the request to be sent
			msg.addReceiver(to);
			// set the content of the request to be sent
			Action a = new Action(to, content);
			try {
				getContentManager().fillContent(msg, a);
			} catch(Codec.CodecException ce) {
				ce.printStackTrace();
				logErr("Error filling content with " + content.getClass().toString());
			} catch(OntologyException oe) {
				oe.printStackTrace();
				logErr("Error filling content with " + content.getClass().toString());
			}
			// return the message to use as request
			return msg;
		}
		
		protected void handleAgree(ACLMessage agree) {
		}
		
		protected void handleRefuse(ACLMessage refuse) {
		}
		
		protected void handleInform(ACLMessage inform) {
		}
		
		protected void handleFailure(ACLMessage failure) {
		}
		
	} // end TransactionInit class
	
}
