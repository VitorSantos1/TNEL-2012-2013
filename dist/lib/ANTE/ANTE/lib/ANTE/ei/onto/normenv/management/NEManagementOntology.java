package ei.onto.normenv.management;

import ei.ElectronicInstitution;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

/**
 * Represents the Normative Environment Management Ontology and it's schemas. Provides support for sending and receiving
 * ACLMessages with actions pertaining ne-management operations.
 * <br>
 * Available actions:
 * <ul>
 * <li>send-contract-types - refers to a request for the normative environment to send the available predefined contract types
 * <li>send-applicable-norms - refers to a request for the normative environment to send the applicable norms concerning a certain
 *     contract type
 * </ul>
 * 
 * @author hlc
 */
public class NEManagementOntology extends BeanOntology {
	
	private static final long serialVersionUID = 7017687972916465882L;
	
	// Name of this ontology
	public static final String ONTOLOGY_NAME = ElectronicInstitution.NE_MANAGEMENT_ONTOLOGY;
	
	// Singleton instance of this ontology
	private static Ontology theInstance = new NEManagementOntology();
	
	// Method to access the singleton ontology object
	public static Ontology getInstance() {
		return theInstance;
	}
	
	// Private constructor
	private NEManagementOntology() {
		// this ontology extends the basic ontology
		super(ONTOLOGY_NAME);
		
		try {
			// add all Concept, Predicate and AgentAction in the current package
			add(this.getClass().getPackage().getName());
			
		} catch(BeanOntologyException boe) {
			boe.printStackTrace();
		}
	}
	
}
