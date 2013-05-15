package ei.agent.role;

import ei.ElectronicInstitution;
import ei.agent.ExternalAgent;
import ei.onto.normenv.illocution.*;
import ei.onto.transaction.*;

import jade.content.AgentAction;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;
import jade.proto.SubscriptionInitiator;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An agent performing the IR-bank role.
 * <p>
 * The bank agent implements the following interaction protocol roles:
 * <ul>
 * <li>the DF-subscription initiator role
 * <li>the transaction responder role
 * </ul>
 * 
 * @author hlc
 */
public class BankAgent extends ExternalAgent {
	
	private static final long serialVersionUID = -5466580847796164683L;
	
	// agent accounts: String -> Double
	protected Hashtable<AID,Double> accounts;
	private Vector<BankMovement> movements;
	
	//the xml file path with the account data (to save/load the account data)
	private String accounts_file;
	
	/**
	 * Behaviour for subscription with the DF in order to get information on newly registered enterprise agents;
	 * needed as global to cancel the subscription on takedown().
	 */
	protected DFSubscriptionInit subscDF;
	
	/**
	 * Set up of the bank agent.
	 */
	protected void setup() {
		// the setup method at the ExternalAgent class prepares the arguments
		super.setup();
		
		// register additional ontologies
		getContentManager().registerOntology(TransactionOntology.getInstance());
		getContentManager().registerOntology(IllocutionOntology.getInstance());
		
		// check the account file name argument
		if(getConfigurationArguments().containsKey("accounts_file")) {
			accounts_file = getConfigurationArguments().getProperty("accounts_file");
		} else {
			accounts_file = null;
		}
		
		// create the accounts object
		accounts = new Hashtable<AID,Double>();
		movements = new Vector<BankMovement>();
		if(accounts_file != null) {
			loadAccounts();
		}
		
		// responder role for TRANSACTION_ONTOLOGY
		addBehaviour(new TransactionResp(this));
		
		// subscribe the DF for updates on new enterprise agents
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd2 = new ServiceDescription();
		sd2.setType(ElectronicInstitution.ROLE_USER_AGENT);
		template.addServices(sd2);
		subscDF = new DFSubscriptionInit(this, template);
		addBehaviour(subscDF);
	}
	
	
	/**
	 * Inner class to implement the transaction responder role.
	 * The bank will handle requests concerning the transfer of currency between different accounts.
	 * 
	 * @author hlc
	 */
	protected class TransactionResp extends SimpleAchieveREResponder {
		
		private static final long serialVersionUID = -6495629460656817375L;
		
		// action received in the request message
		AgentAction request_action;
		
		private String context;
		private AID from;
		private String ref;
		private double amount;
		private AID to;
		
		TransactionResp(Agent agent) {
			super(agent, MessageTemplate.and(	SimpleAchieveREResponder.createMessageTemplate(InteractionProtocol.FIPA_REQUEST),
												MessageTemplate.MatchOntology(ElectronicInstitution.TRANSACTION_ONTOLOGY)));
		}
		
		protected ACLMessage prepareResponse(ACLMessage request) {
			ACLMessage reply = request.createReply();
			request_action = null;
			
			// check requested action
			try {
				// extract content
				ContentElement ce = getContentManager().extractContent(request);
				if(ce instanceof Action)
					request_action = (AgentAction) ((Action) ce).getAction();
			} catch(Codec.CodecException ce) {
				ce.printStackTrace();
			} catch(OntologyException oe) {
				oe.printStackTrace();
			}
			
			if(request_action instanceof TransferCurrency) {
				context = ((TransferCurrency) request_action).getContext();
				from = request.getSender();
				ref = ((TransferCurrency) request_action).getRef();
				amount = ((TransferCurrency) request_action).getAmount();
				to = ((TransferCurrency) request_action).getTo();
				// if requested action is a currency transfer, check if both agents have an account
				if(accounts.get(from) == null) {
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent(from.getLocalName() + " does not have an account");
				} else if(accounts.get(to) == null) {
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent(to.getLocalName() + " does not have an account");
				} else
					reply.setPerformative(ACLMessage.AGREE);
			} else
				reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			
			// return the created reply
			return reply;
		}
		
		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
			ACLMessage reply = request.createReply();
			
			if(request_action instanceof TransferCurrency) {
				// make transfer
				double saldo_from = ((Double) accounts.get(from)).doubleValue();
				double saldo_to = ((Double) accounts.get(to)).doubleValue();
				// ckeck account balance -- commented for now, let it go negative
//				if(saldo_from - amount < 0) {
//					reply.setPerformative(ACLMessage.FAILURE);
//					reply.setContent("not enough balance");
//					return reply;
//				}
				
				//add the movement to the vector
				movements.add(new BankMovement(from,to,amount));
				
				accounts.put(from, new Double(saldo_from - amount));
				accounts.put(to, new Double(saldo_to + amount));
				
				//update gui
				if (getGUI() != null) {
					((BankAgentGUI) gui).updateBalanceRow(from, saldo_from-amount);
					((BankAgentGUI) gui).updateBalanceRow(to, saldo_to+amount);
				}
				saveAccounts();
				
				// inform the envisaged agent through a message?
				
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent("done");
				
				// inform the normative environment
				ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
				inform.setLanguage(getSlCodec().getName());
				inform.setOntology(ElectronicInstitution.ILLOCUTION_ONTOLOGY);
				inform.addReceiver(fetchAgent(ElectronicInstitution.SRV_NORMATIVE_ENVIRONMENT, false));
				// inform the envisaged agent?
				//inform.addReceiver(to);
				
				CurrencyTransfered ct = new CurrencyTransfered();
				ct.setContext(context);
				ct.setFrom_agent(from);
				ct.setRef(ref);
				ct.setAmount(amount);
				ct.setTo_agent(to);
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
				
				send(inform);
			} else {
				logErr("BUG: This situation should never occur!");
				reply.setPerformative(ACLMessage.FAILURE);
			}
			
			// return the created reply
			return reply;
		}
		
	} // end TransactionResp class
	
	
	/**
	 * Inner class to implement the DF-subscription initiator role.
	 * The bank subscribes the DF for updates on new enterprise agents.
	 */
	protected class DFSubscriptionInit extends SubscriptionInitiator {
		
		private static final long serialVersionUID = 4835413022124498744L;
		
		/**
		 * Creates a <code>SubscribeDFInit</code> instance
		 *
		 * @param agent The agent that adds the behaviour.
		 */
		DFSubscriptionInit(Agent agent, DFAgentDescription dfad) {
			super(agent, DFService.createSubscriptionMessage(agent, getDefaultDF(), dfad, null));
		}
		
		/**
		 * Handles an inform message from the DF.
		 */
		protected void handleInform(ACLMessage inform) {
			try {
				DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
				// TODO: for now, create new accounts for new enterprise agents - initial balance of 0.0!
				for(int i=0; i<dfds.length; i++) {
					AID agent = dfds[i].getName();
					if(accounts.get(agent) == null){
						accounts.put(agent, new Double(0.0));
						if (getGUI() != null) {
							((BankAgentGUI) gui).updateBalanceRow(agent, new Double(0.0));
						}
					}
				}
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
		
	} // end DFSubscriptionInit class
	
	/**
	 * On takedown save the accounts file
	 */
	protected void takeDown() {
		// the takeDown method at the ExternalAgent class handles deregistration from the DF
		super.takeDown();
		// save accounts file
		if(accounts_file != null){
			log("Saving Accounts File!");
			saveAccounts();
		}
    }
	
	/**
	 * Save the agents accounts information to a file (including movements)
	 */
	public void saveAccounts() {
		if(accounts_file == null) {
			return;
		}
		
		OutputStreamWriter f;
		OutputStream f_out;
		OutputStream fout;
		
		File file = new File(accounts_file);
		
		//create file and write to it
		try{
			file.delete();
			file.createNewFile();
			fout = new FileOutputStream(file);
			f_out = new BufferedOutputStream(fout);
			f = new OutputStreamWriter(f_out);
			
			f.write("<?xml version=\"1.0\"?>\n");
			f.write("<!DOCTYPE agent-accounts>\n");
			f.write("<agent-accounts>\n");

			//go through all agents accounts and insert their respective balances in the file
			for(Enumeration<AID> e = accounts.keys();e.hasMoreElements();){
				f.write("\t<agent>\n");
				AID agent = (AID) e.nextElement();
				Double balance = accounts.get(agent);
				f.write("\t\t<aid>"+agent.getLocalName()+"</aid>\n");
				f.write("\t\t<balance>"+balance+"</balance>\n");
				f.write("\t</agent>\n");
			}
			
			//go through all movements and write them to the file
			for(int i=0;i<movements.size();i++){
				f.write("\t<movement>\n");
				BankMovement bm = movements.get(i);
				f.write("\t\t<from>"+bm.getFrom().getLocalName()+"</from>\n");
				f.write("\t\t<to>"+bm.getTo().getLocalName()+"</to>\n");
				f.write("\t\t<amount>"+bm.getAmount()+"</amount>\n");
				f.write("\t</movement>\n");
			}
			
			f.write("</agent-accounts>\n");
			
			f.flush();
			f.close();
			}
			catch(FileNotFoundException fnf){
				fnf.printStackTrace();
			}
			catch(IOException ioe){
				ioe.printStackTrace();
			}
	}
	
	/**
	 * Gets the movements vector
	 * 
	 * @return	the movements vector
	 */
	public Vector<BankMovement> getMovements(){
		return movements;
	}
	
	/**
	 * Gets the accounts hashtable
	 * 
	 * @return	the accounts hashtable
	 */
	public Hashtable<AID,Double> getAccounts(){
		return accounts;
	}
	
	/**
	 * Load the agents accounts and movements from a file (xml)
	 */
	private void loadAccounts(){
		
		File file = new File(accounts_file);
		
		//check if parameter file exists before trying to load the account data
		if(file.exists()){
			
			accounts = new Hashtable<AID,Double>();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// create Document object
			NodeList agents_accounts;
			NodeList agents_movements;
			
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				
				Document ei_config = builder.parse(file);
				// get account balances for every agent
				agents_accounts = ((Element) ei_config.getElementsByTagName("agent-accounts").item(0)).getElementsByTagName("agent");
				
				//get movements for every agent
				agents_movements = ((Element) ei_config.getElementsByTagName("agent-accounts").item(0)).getElementsByTagName("movement");
				
				// iterate through <agent> nodes
				for(int i=0; i<agents_accounts.getLength(); i++) {
					Node ag = agents_accounts.item(i);
					AID agent = null;
					Double balance = null;
					// handle the current agent aid and balance
					agent = new AID(((Element) ag).getElementsByTagName("aid").item(0).getFirstChild().getNodeValue(),false);
					balance = Double.valueOf(((Element) ag).getElementsByTagName("balance").item(0).getFirstChild().getNodeValue());
					//add the data to the accounts hashtable
					accounts.put(agent, balance);
				}
				
				//iterate through <movement> nodes
				for(int i=0;i<agents_movements.getLength();i++){
					Node ag = agents_movements.item(i);
					AID from = null;
					AID to = null;
					Double amount = null;
					BankMovement bm;
					
					from = new AID(((Element) ag).getElementsByTagName("from").item(0).getFirstChild().getNodeValue(),false);
					to = new AID(((Element) ag).getElementsByTagName("to").item(0).getFirstChild().getNodeValue(),false);
					amount = Double.valueOf(((Element) ag).getElementsByTagName("amount").item(0).getFirstChild().getNodeValue());
					bm = new BankMovement(from,to,amount);
					movements.add(bm);
				}
				
			} catch(Exception e) {
				//javax.xml.parsers.ParserConfigurationException
				//org.xml.sax.SAXException
				//java.io.IOException ioe
				logErr("Error occurred while parsing the agents account file");
				e.printStackTrace();
				return;
			}
		}
	}

	protected boolean createGUI() {
		gui = new BankAgentGUI(this);
		return true;
	}
}
