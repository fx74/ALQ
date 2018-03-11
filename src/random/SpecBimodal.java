package random;

public class SpecBimodal  extends AbstractRandomVariable {
	
	@Override
	public double sample() {
		double v = random.nextDouble();
		double res = 9.95238 * v;
		if (v < 0.1)
			res += 9.95238;
		return res;
	}

}
