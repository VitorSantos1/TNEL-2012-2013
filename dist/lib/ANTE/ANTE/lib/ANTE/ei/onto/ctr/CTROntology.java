package ei.onto.ctr;

import ei.ElectronicInstitution;

import jade.content.onto.*;

/**
 * Represents the Reputation Ontology and it's schemas.
 * 
 * @author hlc
 */
public class CTROntology extends BeanOntology {
	
	private static final long serialVersionUID = 7017687972916465882L;
	
	// Name of this ontology
	public static final String ONTOLOGY_NAME = ElectronicInstitution.CTR_ONTOLOGY;

	// Singleton instance of this ontology
	private static Ontology theInstance = new CTROntology();
	
	// Method to access the singleton ontology object
	public static Ontology getInstance(){
		return theInstance;
	}
	
	// Private constructor
	private CTROntology(){
		super(ONTOLOGY_NAME);
		
		try {
			// add all Concept, Predicate and AgentAction in the current package
			add(this.getClass().getPackage().getName());
			
		} catch(BeanOntologyException boe) {
			boe.printStackTrace();
		}
	}	
}