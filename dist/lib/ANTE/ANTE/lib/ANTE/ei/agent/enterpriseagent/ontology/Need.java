
package ei.agent.enterpriseagent.ontology;

public class Need extends Item {
	private static final long serialVersionUID = 89917307384842696L;
	
	private boolean negotiate = true;   // is this need going to be negotiated?
	private boolean essential = false;   // is this need essential?

	private Integer productionVolume;
	
	public Need() {
		super();
	}

	public Need(String name) {
		super(name);
	}
	
	public void setEssential(boolean essential) {
		this.essential = essential;
	}

	public boolean isEssential() {
		return essential;
	}

	public void setNegotiate(boolean negotiate) {
		this.negotiate = negotiate;
	}

	public boolean isNegotiate() {
		return negotiate;
	}

	public void setProductionVolume(Integer productionVolume) {
		this.productionVolume = productionVolume;
	}
	
	public Integer getProductionVolume() {
		return productionVolume;
	}

//	public String toString() {
//		return type + " (essencial " + essential + ") " + attributes;
//	}
}
