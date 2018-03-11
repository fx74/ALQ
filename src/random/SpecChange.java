package random;

public class SpecChange implements RandomVariable {
	
	RandomVariable rv;

	public SpecChange() {
	rv= new Change(new Exponential(1), new Triangular(90000, 100000, 950000), 2000);
	}
	@Override
	public double sample() {
		return rv.sample();
	}

}