package it.unical.dimes.elq;

public final class AtomicEvent implements Event {

	protected final long timestamp;

	public AtomicEvent(long ts) {
		timestamp = ts;
	}

	@Override
	public long getTimeStamp() {

		return timestamp;
	}

	@Override
	public CompositeEvent asComposite() {

		return null;
	}

	@Override
	public int compareTo(Event evt) {
		return Long.compare(timestamp, evt.getTimeStamp());
	}

	@Override
	public String toString() {
		return "@" + timestamp;

	}
}
