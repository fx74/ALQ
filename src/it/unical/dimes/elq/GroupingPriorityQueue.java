package it.unical.dimes.elq;

import java.util.Arrays;

public class GroupingPriorityQueue implements FQ {

	private static final int DEFAULT_INITIAL_CAPACITY = 11;

	private Event[] queue;

	// Number of elements in the priority queue.

	private int nodeCount = 0;
	private int size = 0;

	public GroupingPriorityQueue() {
		this(DEFAULT_INITIAL_CAPACITY);
	}

	public GroupingPriorityQueue(int initCapacity) {
		queue = new Event[initCapacity];
	}

	@Override
	public void enqueue(Event evt) {
		int i = nodeCount;
		if (i >= queue.length)
			grow(i + 1);

		++size;

		if (i == 0) {
			queue[0] = evt;
			++nodeCount;
		} else
			siftUp(i, evt);

	}

	@Override
	public Event dequeue() {

		if (size == 0)
			return null;
		--size;

		int s = nodeCount - 1;

		Event result = queue[0];
		CompositeEvent cResult = result.asComposite();
		
		if (cResult != null && cResult.size() == 0)
			System.out.println("ahiahiaia");

		if (cResult != null) {
			result = cResult.remove();
			if (cResult.size() > 0) {// nodes number doesn't decrease
				return result;
			}
		}

		Event x = queue[s];
		--nodeCount;
		queue[0] = null;
		queue[s] = null;
		
		if (s != 0)
			siftDown(0, x);
		
		if (queue[0] == result)
			System.out.println("What's happened?");
		
		return result;
	}

	private void siftUp(int k, Event key) {

		assert key instanceof AtomicEvent;
		int k1 = k;
		while (k > 0) {
			int parent = (k - 1) >>> 1;
			Event e = queue[parent];
			if (key.compareTo(e) >= 0) {
				if (key.compareTo(e) == 0) {// grouping
					CompositeEvent eComp = e.asComposite();
					if (eComp == null) {// trasform in a composite event<<<<---------------------------
						eComp = new CompositeEvent(e.getTimeStamp());
						eComp.add(e);
						queue[parent] = eComp;
					}
					eComp.add(key);
					return;
				} else
					break;
			}
			k = parent;
		}
		
		while (k1 > k) {
			int parent = (k1 - 1) >>> 1;
			Event e = queue[parent];
			queue[k1] = e;
			k1 = parent;
		}

		++nodeCount;
		queue[k] = key;

	}

	/**
	 * Maximum size of array to allocate. Some VMs reserve some header words in
	 * an array. Attempts to allocate larger arrays may result in
	 * OutOfMemoryError: Requested array size exceeds VM limit
	 */
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

	private void grow(int minCapacity) {
		int oldCapacity = queue.length;
		// Double size if small; else grow by 50%
		int newCapacity = oldCapacity + ((oldCapacity < 64) ? (oldCapacity + 2) : (oldCapacity >> 1));
		// overflow-conscious code
		if (newCapacity - MAX_ARRAY_SIZE > 0)
			newCapacity = hugeCapacity(minCapacity);
		queue = Arrays.copyOf(queue, newCapacity);
	}

	private static int hugeCapacity(int minCapacity) {
		if (minCapacity < 0) // overflow
			throw new OutOfMemoryError();
		return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
	}

	private void siftDown(int k, Event key) {
		int half = nodeCount >>> 1; // loop while a non-leaf
		while (k < half) {
			int child = (k << 1) + 1; // assume left child is least
			Event c = queue[child];
			int right = child + 1;
			if (right < nodeCount && c.compareTo(queue[right]) > 0)
				c = queue[child = right];
			if (key.compareTo(c) <= 0)
				break;
			queue[k] = c;
			k = child;
		}
		queue[k] = key;
	}

	@Override
	public int size() {
		return size;
	}

}
