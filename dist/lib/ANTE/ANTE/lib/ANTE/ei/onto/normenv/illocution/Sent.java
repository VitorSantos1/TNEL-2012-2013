package ei.onto.normenv.illocution;

/**
 * @author hlc
 */
public class Sent extends IssuerIllocution {
	
	private static final long serialVersionUID = -1727549757869582797L;
	
	private String ref;
	private String item;
	private int quantity;
	
	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getRef() {
		return ref;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String i) {
		item = i;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int q) {
		quantity = q;
	}
	
}
