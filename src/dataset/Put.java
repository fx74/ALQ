package dataset;

public class Put extends Action {

	public Put(long ts) {
		super(ts);
	}

	@Override
	public String toString() {
		return "PUT " + getTs();
	}
	
}