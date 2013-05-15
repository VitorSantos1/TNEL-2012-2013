package ei.onto.management;

import ei.ElectronicInstitution;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

/**
 * Represents the Electronic Institution Management Ontology and it's schemas. Provides support for sending and receiving
 * ACLMessages with actions pertaining ei-management operations.
 * <br>
 * Available actions:
 * <ul>
 * <li>send-public-key - refers to a request for a certain agent to sent its public-key back
 * <li>show-gui - refers to a request for a certain agent to display its graphical interface on screen
 * </ul>
 */
public class EIManagementOntology extends BeanOntology{
	
	private static final long serialVersionUID = 7017687972916465882L;
	
	// Name of this ontology
	public static final String ONTOLOGY_NAME = ElectronicInstitution.EI_MANAGEMENT_ONTOLOGY;
	
	// Singleton instance of this ontology
	private static Ontology theInstance = new EIManagementOntology();
	
	// Method to access the singleton ontology object
	public static Ontology getInstance() {
		return theInstance;
	}
	
	private EIManagementOntology(){
		//this ontology extends the basic ontology
		super(ONTOLOGY_NAME);
		
		try{
			// add all Concept, Predicate and AgentAction in the current package
			add(this.getClass().getPackage().getName());
			
		} catch(BeanOntologyException boe){
			boe.printStackTrace();
		}
			
	}
	
}
