package ei.onto.normenv.report;

import ei.ElectronicInstitution;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

/**
 * Represents the Normative Environment Report Ontology.
 * Provides support for sending and receiving inform ACLMessages with predicates concerning reports of the normative environment.
 * 
 * @author hlc
 */
public class NEReportOntology extends BeanOntology {
	
	private static final long serialVersionUID = 7017687972916465882L;
	
	// Name of this ontology
	public static final String ONTOLOGY_NAME = ElectronicInstitution.NE_REPORT_ONTOLOGY;
	
	// Singleton instance of this ontology
	private static Ontology theInstance = new NEReportOntology();
	
	// Method to access the singleton ontology object
	public static Ontology getInstance() {
		return theInstance;
	}
	
	// Private constructor
	private NEReportOntology() {
		super(ONTOLOGY_NAME);
		
		try {
			// add all Concept, Predicate and AgentAction in the current package
			add(this.getClass().getPackage().getName());
			
			// must explicitly add static inner classes (otherwise it will not work when generating a jar file)
			add(ei.onto.normenv.report.NewContract.Frame.class);
			add(ei.onto.normenv.report.NewContract.Frame.Slot.class);
			
		} catch(BeanOntologyException boe) {
			boe.printStackTrace();
		}
	}
	
}
