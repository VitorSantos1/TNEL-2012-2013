
package ei.onto.ontologymapping;

import jade.content.*;
import jade.util.leap.List;
import jade.util.leap.ArrayList;

/**
 * This class represents a group of Items and the mapping of their names to another agent ontology. The attributes for each 
 * item are also matched to their respective translations
 */
public class ItemMapping implements Predicate {
	private static final long serialVersionUID = 7464797241263393714L;

	private String needType;	// the type of the need to be mapped
	private String competenceType;	//the type of the mapping competence
	private String confidence;	// the confidence in this mapping
	private List attributeMappings;	// list of attribute mappings
	
	public ItemMapping(){
		attributeMappings = new ArrayList();
	}
	
	public void setNeedType(String needType){
		this.needType = needType;
	}
		
	public String getNeedType(){
		return needType;
	}
	
	public String getCompetenceType(){
		return competenceType;
	}
	
	public void setCompetenceType(String competenceType){
		this.competenceType = competenceType;
	}
	
	public void setConfidence(String conf){
		confidence = conf;
	}
	
	public String getConfidence(){
		return confidence;
	}

	public void setAttributeMappings(List attributeMappings){
		this.attributeMappings = attributeMappings;
	}
	
	public List getAttributeMappings(){
		return attributeMappings;
	}

	// ---
	
	/**
	 * 
	 * 
	 * @param attr			the attribute to translate
	 * @return				the name of the attribute based on the translation, or null if there is none
	 */
	public AttributeMapping getAttributeMappingFromMappingName(String attr){
		for(int j=0;j<attributeMappings.size();j++){
			if(((AttributeMapping) attributeMappings.get(j)).getMappingAttributeName().equals(attr)){
				return (AttributeMapping) attributeMappings.get(j);
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * 
	 * @param attribute		the name of the attribute on the inquiring agent's ontology
	 * @return				the name of the same attribute on another agent's ontology
	 */
	public AttributeMapping getAttributeMappingFromName(String attribute){
		for(int j=0;j<attributeMappings.size();j++){
			if(((AttributeMapping) attributeMappings.get(j)).getAtributeName().equals(attribute)){
				return (AttributeMapping) attributeMappings.get(j);
			}
		}
		return null;
	}
	
	/**
	 * 
	 * 
	 * @param attr1		the name of the attribute on the agent's ontology
	 * @param attr2		the name of the attribute on the other agent's ontology
	 */
	public void addAttributeMapping(String attr1, String attr2){
		for(int j=0;j<attributeMappings.size();j++) {
			if(((AttributeMapping) attributeMappings.get(j)).getAtributeName().equals(attr1)) {
				((AttributeMapping) attributeMappings.get(j)).setMappingAttributeName(attr2);
				return;
			}
		}
		AttributeMapping attr = new AttributeMapping();
		attr.setAtributeName(attr1);
		attr.setMappingAttributeName(attr2);
		attributeMappings.add(attr);
		return;
	}
	
}
