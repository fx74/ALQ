package dataset;

import java.io.Serializable;

public abstract class Action implements Serializable{
	private long ts;
	
	public Action(long ts) {
		this.ts=ts;
	}
	
	public long getTs() {
		return ts;
	}
}