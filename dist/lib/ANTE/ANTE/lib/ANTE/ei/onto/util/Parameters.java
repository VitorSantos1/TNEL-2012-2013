package ei.onto.util;

import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

public class Parameters extends ArrayList {
	private static final long serialVersionUID = -2246841352517497571L;

	public void add(String negotiationParameterName, String negotiationParameterValue) {
		super.add(new Parameter(negotiationParameterName, negotiationParameterValue));
	}

	public String get(String parameterName) {
		Iterator it = iterator();
		while(it.hasNext()) {
			Parameter p = (Parameter) it.next();
			if(p.getName().equals(parameterName)) {
				return (String) p.getValue();
			}
		}
		return null;
	}
	
}
