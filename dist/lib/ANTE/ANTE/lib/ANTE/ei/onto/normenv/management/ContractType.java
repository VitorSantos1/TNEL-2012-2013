
package ei.onto.normenv.management;

import jade.content.Concept;

/**
 * This class represents the concept "contract-type".
 * 
 * @author hlc
 */
public class ContractType implements Concept {
	
	private static final long serialVersionUID = 89917307384842696L;
	
	private String name;
	private String comment;
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getComment(){
		return comment;
	}
	
	public void setComment(String comment){
		this.comment = comment;
	}
	
}
