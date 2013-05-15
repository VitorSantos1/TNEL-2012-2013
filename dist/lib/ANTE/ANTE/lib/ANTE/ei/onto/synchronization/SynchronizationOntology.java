package ei.onto.synchronization;

import ei.ElectronicInstitution;

import jade.content.onto.*;

public class SynchronizationOntology extends BeanOntology {
	
	private static final long serialVersionUID = 7017687972916465882L;
	
	// Name of this ontology
	public static final String ONTOLOGY_NAME = ElectronicInstitution.SYNCHRONIZATION_ONTOLOGY;

	// Singleton instance of this ontology
	private static Ontology theInstance = new SynchronizationOntology();
	
	public static Ontology getInstance(){
		return theInstance;
	}
	
	private SynchronizationOntology() {
		super(ONTOLOGY_NAME);
		
		try {
			// add all Concept, Predicate and AgentAction in the current package
			add(this.getClass().getPackage().getName());
			
			// add Parameters and Parameter
			//add(ei.onto.util.Parameters.class);
			add(ei.onto.util.Parameter.class);
			
		} catch(BeanOntologyException boe) {
			boe.printStackTrace();
		}
	}

}
