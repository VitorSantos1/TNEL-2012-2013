package ei.onto.normenv.illocution;

import ei.ElectronicInstitution;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

/**
 * @author hlc
 */
public class IllocutionOntology extends BeanOntology {
	
	private static final long serialVersionUID = -6038691535103297102L;
	
	// Name of this ontology
	public static final String ONTOLOGY_NAME = ElectronicInstitution.ILLOCUTION_ONTOLOGY;
	
	// Singleton instance of this ontology
	private static Ontology theInstance = new IllocutionOntology();
	
	// Method to access the singleton ontology object
	public static Ontology getInstance() {
		return theInstance;
	}
	
	// Private constructor
	private IllocutionOntology() {
		super(ONTOLOGY_NAME);
		
		try {
			// add all Concept, Predicate and AgentAction in the current package
			add(this.getClass().getPackage().getName());
			
		} catch(BeanOntologyException boe) {
			boe.printStackTrace();
		}
	}
	
}
