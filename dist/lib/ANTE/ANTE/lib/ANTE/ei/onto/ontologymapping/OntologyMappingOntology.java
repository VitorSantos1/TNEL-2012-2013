package ei.onto.ontologymapping;

import jade.content.onto.*;
import ei.ElectronicInstitution;
import ei.agent.enterpriseagent.ontology.Attribute;
import ei.agent.enterpriseagent.ontology.Competence;
import ei.agent.enterpriseagent.ontology.Need;


/**
 * Represents the Ontology Mapping Ontology and it's schemas. Provides support for messages between the ontology service and the
 * several agents involved in a negotiation about the details of their components in order to match different ontologies allowing
 * the respective negotiations to continue. 
 */
public class OntologyMappingOntology extends BeanOntology{

	private static final long serialVersionUID = 1L;

	// Name of this ontology
	public static final String ONTOLOGY_NAME = ElectronicInstitution.ONTOLOGY_MAPPING_ONTOLOGY;
	
	// Singleton instance of this ontology
	private static Ontology theInstance = new OntologyMappingOntology();
	
	// Method to access the singleton ontology object
	public static Ontology getInstance() {
		return theInstance;
	}
	
	// Private constructor
	private OntologyMappingOntology() {
		super(ONTOLOGY_NAME, BasicOntology.getInstance());

		try {
			// add all Concept, Predicate and AgentAction in the current package
			add(this.getClass().getPackage().getName());
			
			// add need and attribute concepts
			add(Need.class);
			add(Competence.class);
			add(Attribute.class);
			
		} catch(BeanOntologyException boe) {
			boe.printStackTrace();
		}
	}	
}
