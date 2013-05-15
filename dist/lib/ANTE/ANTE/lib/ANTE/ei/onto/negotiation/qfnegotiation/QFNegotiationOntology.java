package ei.onto.negotiation.qfnegotiation;

import jade.content.onto.BasicOntology;
import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

public class QFNegotiationOntology extends BeanOntology {

	private static final long serialVersionUID = 7017687972916465882L;
	
	// Name of this ontology
	public static final String ONTOLOGY_NAME = ei.ElectronicInstitution.QF_NEGOTIATION_ONTOLOGY;
	
	// Singleton instance of this ontology
	private static Ontology theInstance = new QFNegotiationOntology();
	
	// Method to access the singleton ontology object
	public static Ontology getInstance() {
		return theInstance;
	}
	
	// Private constructor
	private QFNegotiationOntology() {
		super(ONTOLOGY_NAME, BasicOntology.getInstance());
		
		try {
			// add all Concept, Predicate and AgentAction in the current package
			add(this.getClass().getPackage().getName());
			
			add(ei.agent.enterpriseagent.ontology.Need.class);
			add(ei.agent.enterpriseagent.ontology.Attribute.class);
			
		} catch(BeanOntologyException boe) {
			boe.printStackTrace();
		}
	}
}

