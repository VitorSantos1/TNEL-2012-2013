package ei.service.normenv;

import ei.ElectronicInstitution;
import ei.contract.Jess2XMLContractParser;
import ei.contract.XMLContract2JessParser;
import ei.contract.ContractWrapper;
import ei.contract.xml.*;
import ei.onto.normenv.illocution.*;
import ei.onto.normenv.management.ContractType;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import jess.awt.TextAreaWriter;
import jess.Defrule;
import jess.Deftemplate;
import jess.Jesp;
import jess.JessException;
import jess.PrettyPrinter;
import jess.QueryResult;
import jess.Userfunction;
import jess.ValueVector;
import jess.WorkingMemoryMarker;
import jess.xml.XMLVisitor;

/**
 * Class that implements the normative environment behaviour.
 * <br>
 * This behaviour will run the Jess engine.
 * 
 * @author hlc
 */
public class NormEnvBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = -7538862402075843113L;

	/**
	 * The GUI for the normative environment.
	 */
	private NormEnvJessGUI neJessGUI;

	/**
	 * The Jess engine for the normative environment.
	 */
	protected NormEnvEngine jess;

	/**
	 * The default Jess filename to load from.
	 */
	private static final String DEFAULT_JESS_FILE = "ei.bsave";

	/**
	 * The Jess filename to load from.
	 */
	private String jess_file;

	/**
	 * The Jess filename to bsave() to.
	 */
	private String jess_bsave_file;

	/**
	 * Maximum number of passes that every run of Jess can execute before giving control to the agent;
	 * if 0, Jess (and this behaviour) will always run until the engine stops.
	 */
	private static final int MAX_JESS_PASSES = 0;
	
	private String executionMode = "normal";

	/**
	 * Creates a <code>NormEnvBehaviour</code> instance with a default Jess filename.
	 *
	 * @param ne	The Normative Environment that adds the behaviour.
	 * @param neGUI	The Normative Environment's GUI
	 */
	public NormEnvBehaviour(NormativeEnvironment ne, NormEnvJessGUI neGUI) {
		// will try to create the normative environment with a default Jess filename
		this(ne, DEFAULT_JESS_FILE, neGUI);
	}

	/**
	 * Creates a <code>NormEnvBehaviour</code> instance.
	 * The Jess file to load may be a Jess source code file (with a .clp extension) or a Jess serialized state file.
	 *
	 * @param ne		The Normative Environment that adds the behaviour.
	 * @param jessFile	The name of the Jess file to be loaded.
	 * @param neGUI		The Normative Environment's GUI
	 */
	public NormEnvBehaviour(NormativeEnvironment ne, String jessFile, NormEnvJessGUI neGUI) {
		super(ne);
		jess_file = jessFile;
		this.neJessGUI = neGUI;
	}

	/**
	 * Sets up the normative environment (Jess engine).
	 * Note: in the past I had some problems with having this inside onStart(), and moved it to this behaviour's constructor.
	 * I think the problem was related with getting notifications from the DF regarding new agents, which asserts facts into Jess,
	 * and if Jess is not initialized yet I get an exception.
	 * However, the problem seems to have disappeared... :|
	 */
	public void onStart() {
		// create a Jess engine
		jess = new NormEnvEngine();

		// redirect Jess output to the GUI
		TextAreaWriter taw = new TextAreaWriter(neJessGUI.getJessOutputConsole());
		jess.addOutputRouter("t", taw);
		jess.addOutputRouter("WSTDOUT", taw);
		jess.addOutputRouter("WSTDERR", taw);
		// provide the GUI with access to the Rete engine
		neJessGUI.setNormEnvEngine(jess);   // this is very important, otherwise the command line will not work

		// load the Jess file
		try {
			// check file type: source code or serialized state file
			if(jess_file.endsWith("clp")) {
				// Jess source code file
				// set the name for the output Jess file - use the same base name
				jess_bsave_file = jess_file.substring(0, jess_file.length()-3) + "bsave";
				// parse the input file
				// open the Jess file
				FileReader fr = new FileReader(jess_file);
				// create a parser for the file, telling it where to take input from and which engine to send the results to
				Jesp j = new Jesp(fr, jess);
				// parse the input file
				try {
					j.parse(false);
				} catch (JessException je) {
					je.printStackTrace();
				}
				fr.close();
			} else {
				// Jess serialized state file
				// set the name for the output Jess file
				jess_bsave_file = jess_file;
				// bload() the file
				try {
					FileInputStream fis = new FileInputStream(jess_file);
					jess.bload(fis);
					fis.close();
				} catch (ClassNotFoundException cnfe) {
					cnfe.printStackTrace();
				}
			}
		} catch (IOException ioe) {
			System.err.println("Error loading Jess file - engine is empty");
			//ioe.printStackTrace();
		}

		// add Java-implemented Jess user-functions
		jess.addUserfunction(new JessNormEnvReport((NormativeEnvironment) myAgent));
		jess.addUserfunction(new JessScheduleTimeAlert(myAgent));
		
		// set execution mode
		setExecutionMode(this.executionMode);
	}

	/**
	 * Executes the behaviour.
	 */
	public void action() {
		// to count the number of Jess passes
		int executedPasses = -1;

		// run jess
		try {
			// check if a maximum number of steps was defined
			if (MAX_JESS_PASSES > 0) {
				// run a maximum number of steps
				executedPasses = jess.run(MAX_JESS_PASSES);
			} else {
				// or run until the engine stops
				jess.run();
				// Note: runUntilHalt() will not work here, because I only have one thread for the agent;
				//       therefore, the agent would not be able to run other behaviours...
			}
		} catch (JessException je) {
			je.printStackTrace(System.err);
		}

		// if the engine stopped, block this behaviour
		if(executedPasses < MAX_JESS_PASSES)
			block();
		// the behaviour shall be unblocked by a call to restart() from another behaviour
		// or by a message arrival (Jade initiative), which should not affect this behaviour
		// since I am not dealing with arriving messages here
	}
	
	protected void setExecutionMode(String executionMode) {
		this.executionMode = executionMode;
		if(jess != null) {
			try {
				jess.eval("(set-execution-mode " + executionMode + ")");
			} catch(JessException je) {
				je.printStackTrace();   // should never occur
			}
		}
	}

	/**
	 * Gets the predefined contract types.
	 * 
	 * @return	A list with the predefined contract types
	 */
	protected ArrayList<ContractType> getPredefinedContractTypes() {
		ArrayList<Deftemplate> contractTypesDeftemplates = jess.getPredefinedContractTypesDeftemplates();
		ArrayList<ContractType> contractTypes = new ArrayList<ContractType>(contractTypesDeftemplates.size());
		ei.onto.normenv.management.ContractType cT;
		for(int i=0; i<contractTypesDeftemplates.size(); i++) {
			cT = new ei.onto.normenv.management.ContractType();
			cT.setName(contractTypesDeftemplates.get(i).getBaseName());
			cT.setComment(contractTypesDeftemplates.get(i).getDocstring());
			contractTypes.add(cT);
		}
		return contractTypes;
	}

	/**
	 * Gets the agents participating in a given contract.
	 * 
	 * @param contract_id	The id of the contract
	 * @return				The names of the contractual agents
	 */
	protected ArrayList<String> getContractualAgents(String contract_id) {
		return jess.getContractualAgents(contract_id);
	}

	/**
	 * Gets the norms applicable to a contract type.
	 * 
	 * @param contract_type	The contract type
	 * @return				A list with the applicable norms in Jess language format
	 */
	protected ArrayList<String> getApplicableNorms(String contract_type) {
		return 
		getApplicableNormsInJessComments
//		getApplicableNormsInJessLanguage
//		getApplicableNormsInJessML
//		getApplicableNormsInXML
		(contract_type);
	}

	/**
	 * Gets the norms applicable to a contract type.
	 * 
	 * @param contract_type	The contract type
	 * @return				A list with the applicable norms in Jess language format
	 */
	protected ArrayList<String> getApplicableNormsInJessComments(String contract_type) {
		ArrayList<Defrule> applicableNormsDefrules = jess.findApplicableNorms(contract_type);
		ArrayList<String> applicableNorms = new ArrayList<String>(applicableNormsDefrules.size());
		applicableNorms.add(contract_type);
		applicableNorms.add("");
		for(int i=0; i<applicableNormsDefrules.size(); i++) {
			StringTokenizer st = new StringTokenizer(applicableNormsDefrules.get(i).getDocstring(), "\n");
			while(st.hasMoreTokens()) {
				applicableNorms.add(st.nextToken());
			}
		}

		return applicableNorms;
	}
	
	/**
	 * Gets the norms applicable to a contract type.
	 * 
	 * @param contract_type	The contract type
	 * @return				A list with the applicable norms in Jess language format
	 */
	protected ArrayList<String> getApplicableNormsInJessLanguage(String contract_type) {
		ArrayList<Defrule> applicableNormsDefrules = jess.findApplicableNorms(contract_type);
		ArrayList<String> applicableNorms = new ArrayList<String>(applicableNormsDefrules.size());
		for(int i=0; i<applicableNormsDefrules.size(); i++) {
			applicableNorms.add(new PrettyPrinter(applicableNormsDefrules.get(i)).toString());
		}

		return applicableNorms;
	}

	/**
	 * Gets the norms applicable to a contract type.
	 * 
	 * @param contract_type	The contract type
	 * @return				A list with the applicable norms in JessML format
	 */
	protected ArrayList<String> getApplicableNormsInJessML(String contract_type) {
		ArrayList<Defrule> applicableNormsDefrules = jess.findApplicableNorms(contract_type);
		ArrayList<String> applicableNorms = new ArrayList<String>(applicableNormsDefrules.size());
		for(int i=0; i<applicableNormsDefrules.size(); i++) {
			applicableNorms.add(new XMLVisitor(applicableNormsDefrules.get(i)).toString());
		}

		return applicableNorms;
	}

	/**
	 * Gets the norms applicable to a contract type.
	 * 
	 * @param contract_type	The contract type
	 * @return				A list with the applicable norms in XML format
	 */
	protected ArrayList<String> getApplicableNormsInXML(String contract_type) {
		ArrayList<Defrule> applicableNormsDefrules = jess.findApplicableNorms(contract_type);
		ArrayList<String> applicableNorms = new ArrayList<String>(applicableNormsDefrules.size());
		for(int i=0; i<applicableNormsDefrules.size(); i++) {
			applicableNorms.add(Jess2XMLContractParser.toXMLNorm(applicableNormsDefrules.get(i), jess.getGlobalContext()));
		}

		return applicableNorms;
	}

	/**
	 * Handles a new time event.
	 * 
	 * @param context	The context to which the time event concerns
	 * @param when		The time instant that is being created
	 */
	protected boolean newTimeEvent(String context, long when) {
		try {
			jess.eval("(new-time-event " + context + " " + when + ")");
		} catch (JessException re) {
			re.printStackTrace();
			return false;
		}

		// if blocked, wake up!
		if(!isRunnable()) restart();

		// time tick asserted
		return true;
	}

	/**
	 * Handles a new illocution arrival, asserting it into the Jess engine as a brute fact.
	 * 
	 * @param msg The message that has arrived.
	 */
	protected boolean newIllocution(ACLMessage msg) {
		// assert a brute fact (illocution) corresponding to the ACLMessage
		// check illocution content
		Illocution illocution = null;
		try {
			// extract content
			ContentElement ce = myAgent.getContentManager().extractContent(msg);
			if(ce instanceof Illocution)
				illocution = (Illocution) ce;
		} catch(Codec.CodecException ce) {
			ce.printStackTrace();
		} catch(OntologyException oe) {
			oe.printStackTrace();
		}

		String bfact_string = null;
		// third-party illocutions
		if(illocution instanceof CurrencyTransfered) {
			CurrencyTransfered ct = (CurrencyTransfered) illocution;
			bfact_string = "(bfact " +
								"	(agent " + msg.getSender().getLocalName() + ") " +
								"	(context " + ct.getContext() + ") " +
								// currency-transfered ref ?ref from ?fr to ?to amount ?am
								"	(statement currency-transfered " +
								" 		ref " + ct.getRef() +
								" 		from " + ct.getFrom_agent().getLocalName() +
								" 		to " + ct.getTo_agent().getLocalName() +
								" 		amount " + ct.getAmount() + ")" +
								"	(when " + ct.getWhen() + ") )";
		} else if(illocution instanceof Delivered) {
			Delivered d = (Delivered) illocution;
			bfact_string = "(bfact " +
								"	(agent " + msg.getSender().getLocalName() + ") " +
								"	(context " + d.getContext() + ") " +
								// delivered ref ?ref from ?fr to ?to product ?p quantity ?qt
								"	(statement delivered " +
								" 		ref " + d.getRef() +
								" 		from " + d.getFrom_agent().getLocalName() +
								" 		to " + d.getTo_agent().getLocalName() +
								" 		product " + d.getProduct() +
								" 		quantity " + d.getQuantity() + ")" +
								"	(when " + d.getWhen() + ") )";
		} else if(illocution instanceof MsgDelivered) {
			MsgDelivered md = (MsgDelivered) illocution;
			bfact_string = "(bfact " +
								"	(agent " + msg.getSender().getLocalName() + ") " +
								"	(context " + md.getContext() + ") " +
								// msg-delivered ref ?ref from ?fr to ?to msg $?msg
								"	(statement msg-delivered " +
								" 		ref " + md.getRef() +
								" 		from " + md.getFrom_agent().getLocalName() +
								" 		to " + md.getTo_agent().getLocalName() +
								" 		msg " + md.getMsg() + ")" +
								"	(when " + md.getWhen() + ") )";
		}
		// issuer illocutions			TODO
		// addressee illocutions		TODO

		if(bfact_string != null) {
			try {
				jess.assertString(bfact_string);
			} catch(JessException je) {
				//je.printStackTrace();
				return false;
			}
		} else
			return false;

		// if blocked, wake up!
		if(!isRunnable()) restart();

		// illocution asserted
		return true;
	}

	/**
	 * Handles new agents and their services/roles, updating the corresponding Jess facts.
	 * 
	 * @param ags	The agent descriptions; for each agent with service changes, a collection of all currently holding services is received
	 */
	protected boolean newAgents(DFAgentDescription[] ags) {
		for(int i=0; i<ags.length; i++) {
			String agentName = ags[i].getName().getLocalName();
			try {
				// get this agent's current social contracts
				QueryResult qr = jess.runQueryStar("MAIN::get-social-contracts", new ValueVector(1).add(agentName));
				Properties socialContracts = new Properties();   // social-contract role --> social-contract id
				while(qr.next()) {
					socialContracts.setProperty(qr.getSymbol("role"), qr.getSymbol("id"));
				}

				// handle reported JADE services
				Iterator it = ags[i].getAllServices();
				String sdT;
				while(it.hasNext()) {
					sdT = ((ServiceDescription) it.next()).getType();
					// check if this is an institutional role
					if(ElectronicInstitution.isValidRole(sdT)) {
						// check (by trying to remove it) if the role is not yet asserted as a social-contract
						if(socialContracts.remove(sdT) == null) {
							// this is a new role for this agent: assert the new role as a social-contract
							jess.assertString("(social-contract (id social-contract_" + System.currentTimeMillis() + Math.random() +") (who " + agentName + ") (agent " + agentName + ") (role " + sdT + "))");
						}
					}
				}
				// end social contracts (roles) that are no longer registered as services at the DF for this agent
				Iterator<Object> socialContractsIt = socialContracts.values().iterator();
				while(socialContractsIt.hasNext()) {
					jess.eval("(end-social-contract " + (String) socialContractsIt.next() + ")");
				}
			} catch(JessException je) {
				je.printStackTrace();
				return false;
			}					
		}

		// if blocked, wake up!
		if(!isRunnable()) restart();

		// agents added
		return true;
	}

	/**
	 * Handles a new contract to monitor, asserting it into the Jess engine.
	 * <br>
	 * Note that this method is tightly coupled with the XML contract XSD. Any changes to the XSD imply changes in the JAXB generated
	 * classes, which will likely require changes here...
	 * --> TODO: This method needs extensive testing!
	 * 
	 * @param cw	The wrapped contract to monitor.
	 * @return		<code>true</code> if the contract parsed ok, <code>false</code> if something went wrong and the contract was not
	 * 				added to the Jess engine
	 */
	protected boolean newContract(ContractWrapper cw) {
		// This is a generic translation, independent of the contract type!
		// I am assuming (at least for now) that the contact document is well formed, that is, all information needed is there
		// (further validation was made at the notary... FIXME: Maybe there should be a consulting step before the signing process,
		// in order for the NormEnv to validate the contract format, or at least to provide information regarding prerequisites for a
		// given contract type...)

		// check contract id
		if(cw.getId() == null)
			return false;
		// check super-context
		String s = cw.getSuper();
		if(s != null && jess.getContextDef(s) == null)
			return false;
		// check type
		s = cw.getType();
		try {
			if(s != null && jess.findDeftemplate(s) == null)
				return false;
		} catch(JessException je) {
			return false;
		}

		// mark current Jess engine state, in case something goes wrong...
		WorkingMemoryMarker marker = jess.mark();

		Contract contract = cw.getContract();

		// create a parser
		XMLContract2JessParser parser = new XMLContract2JessParser(contract);

		// create the contract into Jess engine
		try {
			// module creation
			jess.eval(parser.moduleCode());

			// context creation
			jess.assertString(parser.contextCode());

			// contractual info
			List<Contract.Header.ContractualInfo> cinfos = contract.getHeader().getContractualInfo();
			for(int i=0; i<cinfos.size(); i++) {
				// check if a template definition is needed
				if(jess.findDeftemplate(cinfos.get(i).getName()) == null) {
					// template is not defined - define it
					jess.eval(parser.contractualInfoTemplateCode(cinfos.get(i)));
				}
				// assert this contractual-info
				jess.assertString(parser.contractualInfoCode(cinfos.get(i)));
			}

			// rules
			Contract.Rules the_rules = contract.getRules();
			if(the_rules != null) {
				List<RuleType> rules = the_rules.getRule();
				for(int i=0; i<rules.size(); i++)
					jess.eval(parser.ruleCode(rules.get(i)));
			}

			// norms
			Contract.Norms the_norms = contract.getNorms();
			if(the_norms != null) {
				List<NormType> norms = the_norms.getNorm();
				for(int i=0; i<norms.size(); i++) {
					ArrayList<ContractualInfoType> cinfosInLHS = new ArrayList<ContractualInfoType>();
					String norm = parser.normCode(norms.get(i), cinfosInLHS);
					// check out needed templates that are not yet defined
					for(int j=0; j<cinfosInLHS.size(); j++) {
						if(jess.findDeftemplate(cinfosInLHS.get(j).getName()) == null) {
							// need deftemplate
							jess.eval(parser.contractualInfoTemplateCode(cinfosInLHS.get(j)));
						}
					}
					// define the norm
					jess.eval(norm);
				}
			}

		} catch(JessException je) {
			// a problem occurred - contract is not valid
			je.printStackTrace();
			// restore previous Jess engine state
			try {
				jess.resetToMark(marker);
			} catch(JessException je_) {
				je_.printStackTrace();
			}
			return false;				
		}

		// if blocked, wake up!
		if(!isRunnable()) restart();

		// contract created
		return true;
	}

	/**
	 * Saves the state of the Rete engine.
	 */
	public void saveEnvState() {
		// prevent Userfunctions from being saved in bsave file
		// obtain Userfunctions
		Userfunction norm_env_report = jess.findUserfunction("norm-env-report");
		Userfunction schedule_time_alert = jess.findUserfunction("schedule-time-alert");
		// remove Userfunctions
		jess.removeUserfunction("norm-env-report");
		jess.removeUserfunction("schedule-time-alert");
		// invoke bsave() at the Rete engine
		try {
			FileOutputStream fos = new FileOutputStream(jess_bsave_file);
			jess.bsave(fos);
			fos.close();
		} catch (IOException ioe) {
			System.err.println("Error saving Jess file");
			ioe.printStackTrace();
		}
		// put back Userfunctions
		jess.addUserfunction(norm_env_report);
		jess.addUserfunction(schedule_time_alert);
	}

}
