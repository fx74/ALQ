package it.unical.dimes.elq;

public class CompositeEvent implements Event {
	private LinkedEventList list;
	private final long timestamp;

	public CompositeEvent(long ts) {
		timestamp = ts;
		list = new LinkedEventList();

	}

	CompositeEvent(CompositeEvent left, CompositeEvent right) {
		assert left.timestamp == right.timestamp;

		timestamp = left.timestamp;

		left.list.enqueueAndEmpties(right.list);
		list = left.list;

	}

	@Override
	public long getTimeStamp() {

		return timestamp;
	}

	@Override
	public CompositeEvent asComposite() {

		return this;
	}
	@Override
	public int compareTo(Event evt) {
		return Long.compare(timestamp, evt.getTimeStamp());
	}

	public void add(Event evt) {
		assert evt.getTimeStamp() == getTimeStamp();
		list.add(evt);

	}

	public void addFirst(Event evt) {
		assert evt.getTimeStamp() == getTimeStamp();
		list.addFirst(evt);

	}

	public Event getFirst() {
		return list.getFirst();
	}

	public Event remove() {

		return list.removeFirstAtomic();
	}

	public int size() {
		assert (list.eventCount() == list.nodeCount());
		return list.eventCount();

	}

	@Override
	public String toString() {
		return "CE@" + timestamp + '[' + list.nodeCount() + ']';
	}
}
