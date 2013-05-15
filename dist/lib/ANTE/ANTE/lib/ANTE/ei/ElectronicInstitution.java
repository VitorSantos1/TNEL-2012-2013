package ei;

import jade.core.Agent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This is the top class for launching the Electronic Institution (EI) platform.
 * The Electronic Institution is configured through an XML configuration file specifying Jade parameters and agents to launch.
 * The DTD associated with the configuration file is:
 * <p>
 * <code>
 * &lt;!ELEMENT ei-config               (jade-parameters, institutional-agents)&gt;<br>
 * &lt;!ELEMENT jade-parameters         (parameter*)&gt;<br>
 * &lt;!ELEMENT parameter               (#PCDATA)&gt;<br>
 * &lt;!ELEMENT institutional-agents    (agent+)&gt;<br>
 * &lt;!ELEMENT agent                   (class, argument*)&gt;<br>
 * &lt;!ELEMENT class                   (#PCDATA)&gt;<br>
 * &lt;!ELEMENT argument                (#PCDATA)&gt;<br>
 * &lt;!ATTLIST agent name CDATA #REQUIRED&gt;<br>
 * </code>
 * <p>
 * The XML file can include a set of Jade specific parameters.
 * <br>
 * The XML file specifies, for each agent to launch:
 * <ul>
 * <li>the agent name
 * <li>the class implementing the agent (which must extend <code>jade.core.Agent</code>; typically, it will extend <code>ei.EIAgent</code>, either directly or indirectly)
 * <li>optionally, arguments to pass to the agent
 * </ul>
 * <p>
 * Notes:
 * <ul>
 * <li>the EI limits itself to launching these agents -- it is up to each agent to register with the directory facilitator (DF)
 * <li>there can be any number of agents providing each platform service
 * <li>the configuration file can be used to launch at startup any kind of agents (e.g. RMA, ...)
 * </ul>
 * <p><p>
 * This class includes a definition of the services that can be provided by agents participating in the Electronic Institution
 * (institutional agents). When registering with the DF, institutional agents should use one of these service names.
 * When providing this information in the XML configuration file (using "service=..." tags), use
 * one of the following constants:
 * <ul>
 * <li><code>{@value ei.ElectronicInstitution#SRV_NORMATIVE_ENVIRONMENT}</code> for the normative environment agent
 * <li><code>{@value ei.ElectronicInstitution#SRV_ONTOLOGY_MAPPING}</code> for the ontology mapping service
 * <li><code>{@value ei.ElectronicInstitution#SRV_NEGOTIATION_FACILITATOR}</code> for the negotiation mediation service
 * <li><code>{@value ei.ElectronicInstitution#SRV_NOTARY}</code> for the notary service
 * <li><code>{@value ei.ElectronicInstitution#SRV_CTR}</code> for the computational trust and reputation service
 * </ul>
 * <br>
 * This class also includes a definition of the roles that can be enacted by external agents participating in the
 * Electronic Institution. When registering with the DF, agents can use one of these role names, namely:
 * <ul>
 * <li><code>{@value ei.ElectronicInstitution#ROLE_BANK}</code> for a banking role
 * <li><code>{@value ei.ElectronicInstitution#ROLE_DELIVERY_TRACKER}</code> for a delivery tracker role
 * <li><code>{@value ei.ElectronicInstitution#ROLE_MESSENGER}</code> for a messenger role
 * </ul>
 * <p><p>
 * This class also includes constant definitions for all the ontologies used within the Electronic Institution platform.
 * 
 * @author hlc
 */
public class ElectronicInstitution {
	
	/*
	 * Protocols
	 */
	public static final String QF_NEGOTIATION_PROTOCOL = "qf-negotiation";
	
	/*
	 * Ontologies
	 */
	public static final String EI_MANAGEMENT_ONTOLOGY = "ei-management";
	public static final String CTR_ONTOLOGY = "ctr";
	public static final String NE_MANAGEMENT_ONTOLOGY = "ne-management";
	public static final String NE_REPORT_ONTOLOGY = "ne-report";
	public static final String NE_DELEGATION_ONTOLOGY = "ne-delegation";
	public static final String NEGOTIATION_ONTOLOGY = "negotiation";
	public static final String QF_NEGOTIATION_ONTOLOGY = "qf-negotiation";
	public static final String ONTOLOGY_MAPPING_ONTOLOGY = "ontology-mapping";
	public static final String NOTARY_REGISTRATION_ONTOLOGY = "notary-registration";
	public static final String CONTRACT_SIGNING_ONTOLOGY = "contract-signing";
	public static final String ILLOCUTION_ONTOLOGY = "illocution";   // roles --> NormativeEnvironment (INFORM performative - could be implemented as a subscription service)
	public static final String TRANSACTION_ONTOLOGY = "transaction";   // EnterpriseAgent <--> roles (FIPARequest protocol)
	//public static final String CM_IOWfM_ONTOLOGY = "cm-iowfm";
	public static final String SYNCHRONIZATION_ONTOLOGY = "synchronization";
	
	private static final String[] ontologies = {
		EI_MANAGEMENT_ONTOLOGY,
		CTR_ONTOLOGY,
		NE_MANAGEMENT_ONTOLOGY,
		NE_REPORT_ONTOLOGY,
		NE_DELEGATION_ONTOLOGY,
		NEGOTIATION_ONTOLOGY,
		QF_NEGOTIATION_ONTOLOGY,
		ONTOLOGY_MAPPING_ONTOLOGY,
		NOTARY_REGISTRATION_ONTOLOGY,
		CONTRACT_SIGNING_ONTOLOGY,
		ILLOCUTION_ONTOLOGY,
		TRANSACTION_ONTOLOGY//, CM_IOWfM_ONTOLOGY
	};
	
	/*
	 * Institutional services
	 */
	public static final String SRV_NORMATIVE_ENVIRONMENT = "IS-normative-environment";
	public static final String SRV_ONTOLOGY_MAPPING = "IS-ontology-mapping";
	public static final String SRV_NEGOTIATION_FACILITATOR = "IS-negotiation-facilitator";
	public static final String SRV_NOTARY = "IS-notary";
	public static final String SRV_CTR = "IS-ctr";
	private static final String[] services = {SRV_NORMATIVE_ENVIRONMENT, SRV_ONTOLOGY_MAPPING, SRV_NEGOTIATION_FACILITATOR, SRV_NOTARY, SRV_CTR};
	
	/*
	 * Institutional roles
	 */
	public static final String ROLE_BANK = "IR-bank";
	public static final String ROLE_DELIVERY_TRACKER = "IR-delivery-tracker";
	public static final String ROLE_MESSENGER = "IR-messenger";

	/*
	 * User agents
	 */
	public static final String ROLE_USER_AGENT = "enterprise-agent";
	public static final String ROLE_BUYER_AGENT = "buyer";
	public static final String ROLE_SELLER_AGENT = "seller";

	private static final String[] roles = {
		ROLE_BANK, ROLE_DELIVERY_TRACKER, ROLE_MESSENGER,
		ROLE_USER_AGENT, ROLE_BUYER_AGENT, ROLE_SELLER_AGENT};

	private static final String[] externalRoles = {
		ROLE_BANK, ROLE_DELIVERY_TRACKER, ROLE_MESSENGER};
	
	private static final String[] userRoles = {
		ROLE_USER_AGENT, ROLE_BUYER_AGENT, ROLE_SELLER_AGENT};

	/**
	 * JADE main container.
	 */
	private ContainerController mainContainer;
	
	/**
	 * Initializes the Electronic Institution platform and its GUI.
	 * @param jade_parameters		A list of parameter nodes for configuring Jade.
	 */
	protected ElectronicInstitution(NodeList jade_parameters) {
		
		Runtime rt = Runtime.instance(); // get a JADE runtime
		Profile p = new ProfileImpl();   // create a default profile
		
		// adding all JADE parameters
		for(int i=0; i<jade_parameters.getLength(); i++) {
			Node n = jade_parameters.item(i);
			String str = n.getTextContent();
			String[] parameter = str.split("=");
			p.setParameter(parameter[0],parameter[1]);
		}
		
		// create the Main-container
		mainContainer = rt.createMainContainer(p);
		
		// create the ElectronicInstitutionGUIAgent - to allow the connection between the GUI and JADE
		ElectronicInstitutionGUIAgent guiAgent = new ElectronicInstitutionGUIAgent();
		try {
			mainContainer.acceptNewAgent("_guiAgent", guiAgent).start();
		} catch (StaleProxyException e) {
			System.err.println("Error launching agent _guiAgent");
		}
	}
	
	/**
	 * Returns an array with the names for all the predefined ontologies.
	 * @return	The array with the ontologies names
	 */
	public static String[] getAllOntologies() {
		return ontologies;
	}
	
	/**
	 * Checks if a given service is defined.
	 */
	public static boolean isValidService(String service) {
		for (int i=0; i<services.length; i++)
			if (services[i].equals(service))
				return true;
		return false;
	}
	
	/**
	 * Returns an array with the names for all the predefined institutional services.
	 * @return	The array with the service names
	 */
	public static String[] getAllServiceNames() {
		return services;
	}
	
	/**
	 * Checks if a given role is defined.
	 */
	public static boolean isValidRole(String role) {
		for (int i=0; i<roles.length; i++) {
			if (roles[i].equals(role)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if a given external role is defined.
	 */
	public static boolean isValidExternalRole(String externalRole) {
		for (int i=0; i<externalRoles.length; i++) {
			if (externalRoles[i].equals(externalRole)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns an array with the names for all the predefined external roles.
	 * @return	The array with the role names
	 */
	public static String[] getAllExternalRoleNames() {
		return externalRoles;
	}
	
	/**
	 * Returns an array with the names for all the predefined user roles.
	 * @return	The array with the role names
	 */
	public static String[] getAllUserRoleNames() {
		return userRoles;
	}
	
	/**
	 * Searches the DF for all existing agents who provide services related to the Electronic Institution.
	 * @param ag	The requester, needed for DFService
	 * @return		An array with all the discovered agents' DF descriptions
	 */
	public static DFAgentDescription[] fetchAllInstitutionalAgents(Agent ag) {
		// prepare the template
		DFAgentDescription template = new DFAgentDescription();
		// add all the defined institutional services
		for(int i=0; i<services.length; ++i) {
			ServiceDescription sd = new ServiceDescription();
			sd.setType(services[i]);
			template.addServices(sd);
		}
		// get all agents providing institutional services
		DFAgentDescription[] result = null;
		try {
			result = DFService.search(ag, template);
		} catch(FIPAException fe) {
			fe.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Searches the DF for all existing agents who perform institutionally-defined roles.
	 * 
	 * @param ag	The requester, needed for DFService
	 * @return		An array with all the discovered agents' DF descriptions
	 */
	public static DFAgentDescription[] fetchAllExternalRoleAgents(Agent ag) {
		// prepare the template
		DFAgentDescription template = new DFAgentDescription();
		// add all the defined roles
		ServiceDescription sd;
		for(int i=0; i<externalRoles.length; ++i) {
			sd = new ServiceDescription();
			sd.setType(externalRoles[i]);
			template.addServices(sd);
		}
		// get all agents performing roles
		DFAgentDescription[] result = null;
		try {
			result = DFService.search(ag, template);
		} catch(FIPAException fe) {
			fe.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Launch a set of agents defined in an XML document.
	 * <br>
	 * Note that this method is tightly coupled with the DTD as defined above. Any changes to the
	 * DTD will likely require changes here...
	 * 
	 * @param ags	A list of agent nodes
	 */
	protected void launchAgents(NodeList ags) {
		// iterate through <agent> nodes
		for(int i=0; i<ags.getLength(); i++) {
			Node ag = ags.item(i);
			String agent_name = null, agent_class = null;
			// handle the agent name
			agent_name = ((Element) ag).getAttribute("name");
			if(!agent_name.equals("")) {
				NodeList ag_data;
				// handle the agent class and arguments
				// get the class
				ag_data = ((Element) ag).getElementsByTagName("class");
				if(ag_data.item(0).getFirstChild() != null) {
					agent_class = ag_data.item(0).getFirstChild().getNodeValue();
					// get the optional arguments
					ag_data = ((Element) ag).getElementsByTagName("argument");
					Object[] agent_arguments;
					int arg_num = 0;	//index in agent_arguments
					agent_arguments = new Object[ag_data.getLength()];
					// iterate through the arguments
					for(int a=0; a<ag_data.getLength(); a++) {
						Node arg = ag_data.item(a).getFirstChild();
						if(arg != null)
							agent_arguments[arg_num++] = arg.getNodeValue();
						else
							System.err.println("Warning: Empty argument for agent " + agent_name + ":" + agent_class);
					}

					// launch agent
					System.out.print("Creating agent " + agent_name + ":" + agent_class + " ... ");
					// create agent
					try {
						AgentController ac =
							mainContainer.createNewAgent(agent_name, agent_class, agent_arguments);
						// start the agent
						ac.start();
						System.out.println("done");
					} catch (StaleProxyException e) {
						System.err.println("Error launching agent " + agent_name + ":" + agent_class);
						e.printStackTrace();
					}
				} else System.err.println("No class specified for agent " + agent_name);
			} else System.err.println("No agent name specified");
		}
	}
	
	
	/**
	 * Main method...
	 * An XML configuration filename is expected as an argument.
	 * The XML file must be based on the DTD explained above.
	 * 
	 * @param args	There should be an XML filename configuring the Electronic Institution setup: Jade specific parameters and
	 * 				agents to launch at the platform's startup
	 */
	public static void main(String args[]) {
		
		// agents
		if(args.length == 1) {
			// load configuration file
			System.out.print("Loading configuration file ... ");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// activate document validation
			factory.setValidating(true);
			// create Document object
			NodeList jade_parameters;
			NodeList institutional_agents;
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document ei_config = builder.parse(args[0]);
				// get <parameter> nodes in <jade-parameters>
				jade_parameters = ((Element) ei_config.getElementsByTagName("jade-parameters").item(0)).getElementsByTagName("parameter");
				// get <agent> nodes in <institutional-agents>
				institutional_agents = ((Element) ei_config.getElementsByTagName("institutional-agents").item(0)).getElementsByTagName("agent");
				System.out.println("done");
			} catch(Exception e) {
				//javax.xml.parsers.ParserConfigurationException
				//org.xml.sax.SAXException
				//java.io.IOException ioe
				System.err.println("Error occured while parsing the configuration file");
				e.printStackTrace();
				return;
			}
			// create the ElectronicInstitution instance object with the configuration file Document
			ElectronicInstitution ei = new ElectronicInstitution(jade_parameters);
			// launch agents
			ei.launchAgents(institutional_agents);
		} else {
			System.out.println("There is no configuration file specified!");
		}
	}
}
