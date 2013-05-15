package ei.util;

import java.util.LinkedHashMap;

public class LimitedSizeLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = -225078074010222108L;
	
	private int max_entries = -1;
	
	public LimitedSizeLinkedHashMap(){
		super();
	}
	
	public LimitedSizeLinkedHashMap(int max_entries){
		this();
		setMaxEntries(max_entries);
	}

	/**
	 * Set the maximum number of elements in this hash map. A negative value is interpreted as no limit.
	 * 
	 * @param max_entries
	 */
	public void setMaxEntries(int max_entries) {
		this.max_entries = max_entries;
	}
	
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
		return (max_entries >= 0 && this.size() > max_entries);
	}
	
}
