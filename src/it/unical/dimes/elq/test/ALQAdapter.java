package it.unical.dimes.elq.test;

import java.util.PriorityQueue;

import it.unical.dimes.elq.ALadderQueue;
import it.unical.dimes.elq.Event;
import it.unical.dimes.elq.FQ;

public class ALQAdapter implements FQ {

	ALadderQueue alq = null;
	
	public ALQAdapter(final boolean grouping, final boolean upgrowing, final boolean smartspawn) {
		alq=new ALadderQueue(grouping, upgrowing, smartspawn);
	}
	
	@Override
	public Event dequeue() {
		return alq.dequeue();
	}

	@Override
	public void enqueue(Event evt) {
		alq.enqueue(evt);
	}

	@Override
	public int size() {
		return alq.size();
	}

}
