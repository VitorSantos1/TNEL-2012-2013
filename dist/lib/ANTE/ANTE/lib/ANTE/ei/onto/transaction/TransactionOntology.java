package ei.onto.transaction;

import ei.ElectronicInstitution;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

public class TransactionOntology extends BeanOntology {
	
	private static final long serialVersionUID = 7017687972916465882L;
	
	// Name of this ontology
	public static final String ONTOLOGY_NAME = ElectronicInstitution.TRANSACTION_ONTOLOGY;
	
	// Singleton instance of this ontology
	private static Ontology theInstance = new TransactionOntology();
	
	// Method to access the singleton ontology object
	public static Ontology getInstance() {
		return theInstance;
	}
	
	// Private constructor
	private TransactionOntology() {
		super(ONTOLOGY_NAME);
		
		try {
			// add all Concept, Predicate and AgentAction in the current package
			add(this.getClass().getPackage().getName());
			
		} catch(BeanOntologyException boe) {
			boe.printStackTrace();
		}
	}
	
}
