package it.unical.dimes.elq;

interface EventList {
	void add(Event e);

	void addInOrder(Event e);

	void add(Event e, final boolean group);

	void addInOrder(Event e, final boolean group);

	void addFirst(Event e);

	Event getFirstAtomic();

	Event getFirst();
	
	Event getLast();

	Event removeFirstAtomic();

	Event removeFirst();

	//long getMinTS();

	//long getMaxTS();

	int eventCount();

	int nodeCount();

	void sort(boolean group);

	boolean isEmpty();
}
