package random;

import org.apache.commons.math3.distribution.ExponentialDistribution;

public final class Exponential implements RandomVariable {
	
	ExponentialDistribution dist;
	
	public Exponential(double mean) {
		dist=new ExponentialDistribution(mean);
	}
	
	@Override
	public double sample() {
		return dist.sample();
	}

}

