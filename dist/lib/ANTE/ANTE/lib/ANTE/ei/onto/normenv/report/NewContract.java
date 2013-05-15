package ei.onto.normenv.report;

import jade.content.Concept;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * 
 * @author hlc
 */
public class NewContract extends Report {
	private static final long serialVersionUID = -5775213626217243146L;
	
	private String type;
	private String superContext;
	private List agents = null;
	private List contractualInfos = null;
	
	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setSuperContext(String superContext) {
		this.superContext = superContext;
	}

	public String getSuperContext() {
		return superContext;
	}

	public List getAgents() {
		return agents;
	}
	
	public void setAgents(List agents) {
		this.agents = agents;
	}
	
	public void setContractualInfos(List contractualInfos) {
		this.contractualInfos = contractualInfos;
	}
	public List getContractualInfos() {
		return contractualInfos;
	}

	public String toString() {
		return "new-contract (id " + getContext() + ") (type " + type + ") (superContext " + superContext +
									") (agents " + agents + ") (contractualInfos " + contractualInfos + ")";
	}
	
	
	public static class Frame implements Concept {
		
		private static final long serialVersionUID = -7576088286615946712L;
		
		private String name;
		private List slots;
		
		public Frame() {
		}
		
		public Frame(String name) {
			this.name = name;
			slots = new ArrayList();
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public void setSlots(List slots) {
			this.slots = slots;
		}
		
		public List getSlots() {
			return slots;
		}
		
		public void addSlot(Slot slot) {
			slots.add(slot);
		}
		
		public String getSlotValue(String slotName) {
			for(int i=0; i<slots.size(); i++) {
				if(((Slot) slots.get(i)).getName().equals(slotName)) {
					return ((Slot) slots.get(i)).getValue();
				}
			}
			return null;
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer(name);
			for(int i=0; i<slots.size(); i++) {
				sb.append(" ");
				sb.append(slots.get(i));
			}
			return sb.toString();
		}
		
		
		public static class Slot implements Concept {

			private static final long serialVersionUID = -1932582858446636878L;
			
			private String name;
			private String value;
			
			public Slot() {
			}
			
			public Slot(String name, String value) {
				this.name = name;
				this.value = value;
			}
			
			public void setName(String name) {
				this.name = name;
			}
			
			public String getName() {
				return name;
			}
			
			public void setValue(String value) {
				this.value = value;
			}
			
			public String getValue() {
				return value;
			}
			
			public String toString() {
				return "(" + name + " " + value + ")";
			}
			
		} // end class Slot
		
	} // end class Frame
	
}
