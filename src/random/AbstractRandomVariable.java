package random;

import java.util.Random;

public abstract class AbstractRandomVariable implements RandomVariable {
	protected Random random;

	public AbstractRandomVariable() {
		random = new Random();
	}

	public AbstractRandomVariable(long seed) {
		random = new Random(seed);
	}

}