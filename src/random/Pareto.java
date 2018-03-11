package random;

import org.apache.commons.math3.distribution.ParetoDistribution;

public final class Pareto implements RandomVariable {
	
	private ParetoDistribution dist;
	
	public Pareto(double scale, double shape){
		dist=new ParetoDistribution(scale, shape);
	}
	
	@Override
	public double sample() {
		return dist.sample();
	}

}
