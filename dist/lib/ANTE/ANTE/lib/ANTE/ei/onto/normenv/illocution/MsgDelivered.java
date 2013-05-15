package ei.onto.normenv.illocution;

/**
 * @author hlc
 */
public class MsgDelivered extends ThirdPartyIllocution {
	
	private static final long serialVersionUID = 5556888628741778775L;
	
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
