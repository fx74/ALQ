package it.unical.dimes.elq.test;

import java.util.PriorityQueue;

import it.unical.dimes.elq.Event;
import it.unical.dimes.elq.FQ;

public class PQAdapter implements FQ {

	PriorityQueue<Event> pq = null;
	
	public PQAdapter() {
		pq=new PriorityQueue<Event>();
	}
	
	@Override
	public Event dequeue() {
		return pq.remove();
	}

	@Override
	public void enqueue(Event evt) {
		pq.offer(evt);
	}

	@Override
	public int size() {
		return pq.size();
	}

}
