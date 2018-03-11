package random;

public class Camel extends AbstractRandomVariable {
	private final double a;
	private final double b;
	private final int n;
	private final double m;
	private final double w;

	public Camel(double a, double b, int n, double m, double w) {
		super();
		this.a = a;
		this.b = b;
		this.n = n;
		this.m = m;
		this.w = w;
	}

	@Override
	public double sample() {
		double r = random.nextDouble();
		int k = (int)Math.floor(n * r);
		double dw = (b - a) / n;
		if (r < (k + (1 - m) * 0.5) / n)
			return a + dw * (k + ((1 - w) / (1 - m)) * (n * r - k));
		else if (r < (k + (1 + m) * 0.5) / n)
			return a
					+ dw
					* (k + (1 - w) / 2.0 + (w / m)
							* (n * r - k - (1 - m) / 2.0));
		else
			return a
					+ dw
					* (k + (1 + w) / 2.0 + ((1 - w) / (1 - m))
							* (n * r - k - (1 + m) / 2.0));

	}
}
