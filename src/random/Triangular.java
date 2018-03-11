package random;

public class Triangular extends AbstractRandomVariable {
	
	private final double a;
	private final double b;
	private final double c;
	private final double fc;

	public Triangular(double a, double b, double c) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
		fc = (c - a) / (b - a);
	}
	
	public Triangular(double a, double b, double c, long seed) {
		super(seed);
		this.a = a;
		this.b = b;
		this.c = c;
		fc = (c - a) / (b - a);
	}

	@Override
	public double sample() {
		double u = random.nextDouble();
		if (u < fc)
			return a + Math.sqrt(u * (b - a) * (c - a));
		else
			return b - Math.sqrt((1 - u) * (b - a) * (b - c));
	}
	
}
