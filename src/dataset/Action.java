package dataset;

public abstract class Action {
	private long ts;
	
	public Action(long ts) {
		this.ts=ts;
	}
	
	public long getTs() {
		return ts;
	}
}