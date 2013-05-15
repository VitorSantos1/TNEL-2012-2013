package ei.onto.normenv.illocution;

/**
 * @author hlc
 */
public class Delivered extends ThirdPartyIllocution {
	
	private static final long serialVersionUID = 6407928602480512244L;
	
	private String ref;
	private String product;
	private int quantity;
	
	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getRef() {
		return ref;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

}
