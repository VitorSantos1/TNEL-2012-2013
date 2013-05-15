package ei.onto.negotiation;

import ei.ElectronicInstitution;
import ei.agent.enterpriseagent.ontology.Attribute;
import ei.agent.enterpriseagent.ontology.Need;
import ei.onto.util.Parameter;

import jade.content.onto.Ontology; 
import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.BasicOntology;

/**
 * Represents the Negotiation Mediation Ontology and it's schemas. Provides support for agents to request Needs to 
 * Negotiation Mediators initiating new negotiations in the process
 */
public class NegotiationOntology extends BeanOntology {
	private static final long serialVersionUID = 6205302393830746620L;
	
	// Name of this ontology
	public static final String ONTOLOGY_NAME = ElectronicInstitution.NEGOTIATION_ONTOLOGY;
	
	// Singleton instance of this ontology
	private static Ontology theInstance = new NegotiationOntology();
	
	// Method to access the singleton ontology object
	public static Ontology getInstance() {
		return theInstance;
	}
	
	// Private constructor
	private NegotiationOntology() {
		super(ONTOLOGY_NAME, BasicOntology.getInstance());
		try {
			// add all Concept, Predicate and AgentAction in the current package
			add(this.getClass().getPackage().getName());
			
			// add parameters and parameter concepts
			add(Parameter.class);
			
			// add needs and attribute concepts
			add(Need.class);
			add(Attribute.class);
			
		} catch(BeanOntologyException boe) {
			boe.printStackTrace();
		} 
	}
}
