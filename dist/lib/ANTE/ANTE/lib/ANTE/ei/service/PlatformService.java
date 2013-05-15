package ei.service;

import ei.EIAgent;

import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * This abstract class represents the services provided by institutional agents. It should be extended by agents providing
 * institutional services.
 * <p>
 * Subclasses should override the setup() method to create agent behaviour(s), and invoke <code>super.setup()</code>, since this
 * method automatically handles registration with the DF, provided that a "service" property was given that corresponds to a valid service name, 
 * as defined at the <code>ElectronicInstitution</code> class.
 * 
 * @author hlc
 */
public abstract class PlatformService extends EIAgent {
	private static final long serialVersionUID = -7638217219688733722L;

	/**
	 * This setup method invokes <code>super.setup()</code> and registers the agent with the DF, according to the service name
	 * specified as an argument. The service name must be valid.
	 * <br>
	 * When extending this class, override this method to create agent behaviour(s) and invoke <code>super.setup()</code>.
	 */
	protected void setup() {
		super.setup();
		
		// register this agentified service with the DF
		String service = getConfigurationArguments().getProperty("service");
		if(service != null && ei.ElectronicInstitution.isValidService(service)) {   // checking that the service is valid
			// create a service description
			ServiceDescription sd = new ServiceDescription();
			sd.setType(service);
			sd.setName(""+getLocalName());
			// register the agent
			registerAgentAtDF(sd);
		}
	}
	
}
