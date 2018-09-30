package it.unical.dimes.elq;

public final class AtomicEvent implements Event {

	protected final double timestamp;

	public AtomicEvent(double ts) {
		timestamp = ts;
	}

	@Override
	public double getTimeStamp() {

		return timestamp;
	}

	@Override
	public CompositeEvent asComposite() {

		return null;
	}

	@Override
	public int compareTo(Event evt) {
		return Double.compare(timestamp, evt.getTimeStamp());
	}

	@Override
	public String toString() {
		return "@" + timestamp;

	}
}
