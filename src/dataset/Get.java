package dataset;

public class Get extends Action{

	public Get(long ts) {
		super(ts);
	}
	@Override
	public String toString() {
		return "GET "+getTs();
	}
	
}
