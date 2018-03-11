package random;

public class Change extends AbstractRandomVariable {
	private final RandomVariable x;
	private final RandomVariable y;
	private final int N;
	private int counter;

	public Change(RandomVariable x, RandomVariable y, int N) {

		this.x = x;
		this.y = y;
		this.N = N;
	}

	public Change(RandomVariable x, RandomVariable y, int N, long seed) {
		super(seed);
		this.x = x;
		this.y = y;
		this.N = N;
	}

	@Override
	public double sample() {
		counter = (counter + 1) % 2 * N;
		if (counter < N)
			return x.sample();
		else
			return y.sample();

	}

}
