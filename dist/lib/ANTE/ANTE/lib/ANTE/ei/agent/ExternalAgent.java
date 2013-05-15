package ei.agent;

import ei.EIAgent;

import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * This class is a proxy for building external agents.
 * <p>
 * Subclasses should override the setup() method to create agent behaviour(s), and invoke <code>super.setup()</code>, since this
 * method automatically handles registration with the DF, provided that a "role" property was given that corresponds to a valid role name,
 * as defined at the <code>ElectronicInstitution</code> class.
 * <p>
 * 
 * @author hlc
 */
public abstract class ExternalAgent extends EIAgent {
	private static final long serialVersionUID = 3987204624266551122L;
	
	/**
	 * This setup method invokes <code>super.setup()</code> and registers the agent with the DF, according to the role name
	 * specified as an argument. The role name must be valid.
	 * <br>
	 * When extending this class, override this method to create agent behaviour(s) and invoke <code>super.setup()</code>.
	 */
	protected void setup() {
		super.setup();
		
		// register this external agent's role with the DF
		String role = getConfigurationArguments().getProperty("role");
		if(role != null && ei.ElectronicInstitution.isValidRole(role)) {   // checking that the role is valid
			// create a service description
			ServiceDescription sd = new ServiceDescription();
			sd.setType(role);
			sd.setName(""+getLocalName());
			// register the agent
			registerAgentAtDF(sd);
		}
	}
	
}
