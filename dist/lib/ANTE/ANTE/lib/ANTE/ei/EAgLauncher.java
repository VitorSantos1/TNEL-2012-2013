package ei;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class EAgLauncher {
	
	private ContainerController container;
	
	protected EAgLauncher(NodeList jade_parameters) {
		
		Runtime rt = Runtime.instance(); // get a JADE runtime
		Profile p = new ProfileImpl();   // create a default profile
		
		// adding all JADE parameters
		for(int i=0; i<jade_parameters.getLength(); i++) {
			Node n = jade_parameters.item(i);
			String str = n.getTextContent();
			String[] parameter = str.split("=");
			p.setParameter(parameter[0],parameter[1]);
		}
		
		// create the container
		container = rt.createAgentContainer(p);
	}
	
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
							container.createNewAgent(agent_name, agent_class, agent_arguments);
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
			EAgLauncher ei = new EAgLauncher(jade_parameters);
			// launch agents
			ei.launchAgents(institutional_agents);
		} else {
			System.out.println("There is no configuration file specified!");
		}
	}
}
