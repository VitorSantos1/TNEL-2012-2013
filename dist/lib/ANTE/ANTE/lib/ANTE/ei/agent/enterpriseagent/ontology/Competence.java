
package ei.agent.enterpriseagent.ontology;

public class Competence extends Item {
	private static final long serialVersionUID = 89917307384842696L;
	
	private Integer stock;
	
	public Competence() {
		super();
	}

	public Competence(String name) {
		super(name);
	}
	
	public void setStock(Integer stock) {
		this.stock = stock;
	}

	public Integer getStock() {
		return stock;
	}
	
//	public String toString() {
//		return type + " (essencial " + essential + ") " + attributes;
//	}
	
}
