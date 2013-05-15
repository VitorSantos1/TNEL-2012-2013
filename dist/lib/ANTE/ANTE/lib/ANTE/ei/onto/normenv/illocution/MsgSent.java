package ei.onto.normenv.illocution;

/**
 * @author hlc
 */
public class MsgSent extends IssuerIllocution {
	
	private static final long serialVersionUID = -7506834241146493715L;
	
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
