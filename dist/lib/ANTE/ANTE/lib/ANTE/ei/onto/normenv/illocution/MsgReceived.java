package ei.onto.normenv.illocution;

/**
 * @author hlc
 */
public class MsgReceived extends AddresseeIllocution {
	
	private static final long serialVersionUID = 7630113679649230452L;
	
	private String ref;
	private String msg;
	
	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getRef() {
		return ref;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String m) {
		msg = m;
	}
	
}
