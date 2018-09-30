package it.unical.dimes.elq;

public final class LinkedEventList implements EventList {
	private static final class Node {
		Node next;
		Node prev;
		Event info;
	}

	private Node head;
	private Node tail;

	private int nodeCount;
	private int eventCount;

	public LinkedEventList() {
		nodeCount = 0;
		eventCount = 0;
	}

	@Override
	public void add(Event e) {
		add(e, false);
	}

	@Override
	public void addFirst(final Event evt) {
		nodeCount++;

		CompositeEvent compEvt = evt.asComposite();
		
		if (compEvt == null) {
			eventCount++;
		} else {
			eventCount += compEvt.size();
		}

		if (head == null) {
			head = new Node();
			head.info = evt;
			tail = head;
		} else {
			Node n = head;
			head = new Node();
			head.info = evt;
			head.next = n;
			n.prev = head;
		}
		
	}
	
	@Override
	public void add(Event evt, final boolean group) {

		CompositeEvent compEvent = evt.asComposite();
		if (compEvent == null) {
			eventCount++;
		} else {
			eventCount += compEvent.size();
		}

		if (head == null) {
			Node n = new Node();
			nodeCount++;
			n.info = evt;
			head = n;
			tail = n;
		} else {

			if (group && evt.compareTo(tail.info) == 0) {

				CompositeEvent compTailEvt = tail.info.asComposite();

				if (compTailEvt != null) {
					// tail is composite
					if (compEvent == null) {
						// evt is not composite
						compTailEvt.add(evt);
					} else {
						// evt is composite too
						tail.info = new CompositeEvent(compTailEvt, compEvent);
					}
				} else {
					// tail is not composite
					if (compEvent == null) {
						// evt is not composite
						CompositeEvent cevt = new CompositeEvent(
								evt.getTimeStamp());
						cevt.add(tail.info);
						cevt.add(evt);
						tail.info = cevt;
					} else {
						// evt is composite
						compEvent.addFirst(tail.info);
						tail.info = compEvent;
					}
				}

			} else {
				Node n = new Node();
				nodeCount++;
				n.info = evt;
				tail.next = n;
				n.prev = tail;
				tail = n;
			}

		}
	
	}
	
	@Override
	public void addInOrder(Event e) {
		add(e, false);

	}

	@Override
	public void addInOrder(Event event, final boolean group) {
		CompositeEvent compEvent = event.asComposite();
		if (compEvent == null)
			eventCount++;
		else
			eventCount += compEvent.size();

		Node cur = tail;
		while (cur != null) {
			int cmp = cur.info.compareTo(event);
			if (cmp <= 0) {
				if (group && cmp == 0) {
					CompositeEvent compCurEvt = cur.info.asComposite();

					if (compCurEvt != null) {
						// cur is composite
						if (compEvent == null) {
							// evt is not composite
							compCurEvt.add(event);
						} else {
							// evt is composite too
							cur.info = new CompositeEvent(compCurEvt, compEvent);
						}
					} else {
						// cur is not composite
						if (compEvent == null) {
							// evt is not composite
							CompositeEvent cevt = new CompositeEvent(
									event.getTimeStamp());
							cevt.add(cur.info);
							cevt.add(event);
							cur.info = cevt;
						} else {
							// evt is composite
							compEvent.addFirst(cur.info);
							cur.info = compEvent;
						}
					}

				} else {
					Node n = new Node();
					n.info = event;

					n.next = cur.next;
					if (n.next != null) {
						n.next.prev = n;
					} else {
						tail = n;
					}
					n.prev = cur;
					cur.next = n;
					nodeCount++;
				}
				return;
			} else {
				cur = cur.prev;
			}

		}

		Node n = new Node();
		n.info = event;
		n.next = head;

		head = n;
		if (n.next == null)
			tail = n;
		else
			n.next.prev = n;
		nodeCount++;


	}

	@Override
	public Event removeFirstAtomic() {
		assert eventCount > 0;
		eventCount--;
		Event evt;
		CompositeEvent compEvent = head.info.asComposite();
		if (compEvent == null) {
			// head is atomic
			Node n = head;
			evt = n.info;
			head = head.next;
			if (head != null)
				head.prev = null;
			else
				tail = null;
			nodeCount--;
		} else {
			evt = compEvent.remove();
			if (compEvent.size() == 0) {
				head = head.next;
				if (head != null)
					head.prev = null;
				else
					tail = null;
				nodeCount--;
			}
		}

		return evt;
	}

	@Override
	public Event removeFirst() {
		assert eventCount > 0;
		Event evt = head.info;
		CompositeEvent compEvent = evt.asComposite();

		if (compEvent == null) {
			eventCount--;
		} else {
			eventCount -= compEvent.size();
		}

		head = head.next;
		if (head != null)
			head.prev = null;
		else
			tail = null;
		nodeCount--;
		return evt;
	}

	@Override
	public Event getFirstAtomic() {
		assert eventCount > 0;
		Event evt = head.info;
		CompositeEvent compEvent = evt.asComposite();

		if (compEvent != null)
			evt = compEvent.getFirst();
		assert !(evt instanceof CompositeEvent);
		return evt;

	}

	@Override
	public Event getFirst() {
		return head.info;
	}

	@Override
	public Event getLast() {
		return tail.info;
	}
	
	@Override
	public int eventCount() {
		return eventCount;
	}

	@Override
	public int nodeCount() {
		return nodeCount;
	}

	@Override
	public boolean isEmpty() {
		return nodeCount == 0;
	}
	
	public void enqueueAndEmpties(LinkedEventList l2) { 
		// ATTENTION don't use l2 anymore
		tail.next = l2.head;
		l2.head.prev = tail;
		tail = l2.tail;
		eventCount += l2.eventCount;
		nodeCount += l2.nodeCount;
	}
	
	private Event removeNode() {
		Node n = head;
		head = head.next;
		if (head != null)
			head.prev = null;
		else
			tail = null;
		--nodeCount;
		n.next = null;
		n.prev = null;
		
		return n.info;
	}
	
	public void sort(final boolean group) {
		if (nodeCount <= 1)
			return;
		LinkedEventList l1 = new LinkedEventList();
		LinkedEventList l2 = new LinkedEventList();

		LinkedEventList l3 = new LinkedEventList();
		LinkedEventList l4 = new LinkedEventList();

		LinkedEventList currList = l1;

		Event prev = removeNode();
		currList.add(prev, group);
		while (!isEmpty()) {
			Event curr = removeNode();
			if (curr.getTimeStamp() < prev.getTimeStamp()) {
				if (currList == l1)
					currList = l2;
				else
					currList = l1;
			}
			currList.add(curr, group);
			prev = curr;
		}

		boolean sorted = l1.isEmpty() || l2.isEmpty();

		while (!sorted) {
			currList = l3;
			while (!l1.isEmpty() && !l2.isEmpty()) {
				boolean l1ok = true;
				boolean l2ok = true;
				double t1 = l1.head.info.getTimeStamp();
				double t2 = l2.head.info.getTimeStamp();
				while (l1ok && l2ok) {
					if (t1 < t2) {
						currList.add(l1.removeNode(), group);
						if (l1.isEmpty())
							l1ok = false;
						else {
							double nextT1 = l1.head.info.getTimeStamp();
							l1ok = nextT1 >= t1;
							t1 = nextT1;
						}
					} else {
						currList.add(l2.removeNode(), group);
						if (l2.isEmpty())
							l2ok = false;
						else {
							double nextT2 = l2.head.info.getTimeStamp();
							l2ok = nextT2 >= t2;
							t2 = nextT2;
						}
					}
				}
				while (l1ok) {

					currList.add(l1.removeNode(), group);
					if (l1.isEmpty())
						l1ok = false;
					else {
						double nextT1 = l1.head.info.getTimeStamp();
						l1ok = nextT1 >= t1;
						t1 = nextT1;
					}
				}
				while (l2ok) {
					currList.add(l2.removeNode(), group);
					if (l2.isEmpty())
						l2ok = false;
					else {
						double nextT2 = l2.head.info.getTimeStamp();
						l2ok = nextT2 >= t2;
						t2 = nextT2;
					}
				}
				if (currList == l3)
					currList = l4;
				else
					currList = l3;
			}

			while (!l1.isEmpty()) {
				boolean l1ok = true;
				double t1 = l1.head.info.getTimeStamp();
				while (l1ok) {
					currList.add(l1.removeNode(), group);
					if (l1.isEmpty())
						l1ok = false;
					else {
						double nextT1 = l1.head.info.getTimeStamp();
						l1ok = nextT1 >= t1;
						t1 = nextT1;
					}
				}
				if (currList == l3)
					currList = l4;
				else
					currList = l3;
			}

			while (!l2.isEmpty()) {
				boolean l2ok = true;
				double t2 = l2.head.info.getTimeStamp();
				while (l2ok) {
					currList.add(l2.removeNode(), group);
					if (l2.isEmpty())
						l2ok = false;
					else {
						double nextT2 = l2.head.info.getTimeStamp();
						l2ok = nextT2 >= t2;
						t2 = nextT2;
					}
				}
				if (currList == l3)
					currList = l4;
				else
					currList = l3;
			}

			currList = l3;
			l3 = l1;
			l1 = currList;

			currList = l4;
			l4 = l2;
			l2 = currList;
			sorted = l1.isEmpty() || l2.isEmpty();
		}

		if (l1.isEmpty())
			currList = l2;
		else
			currList = l1;

		head = currList.head;
		tail = currList.tail;
		nodeCount = currList.nodeCount;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		Node cur = head;
		while (cur != null) {
			sb.append(cur.info);
			if (cur.next != null)
				sb.append(',');
			cur = cur.next;
		}
		sb.append(']');
		return sb.toString();
	}
}
