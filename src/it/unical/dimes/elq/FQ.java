package it.unical.dimes.elq;

public interface FQ {
	public Event dequeue();

	public void enqueue(Event evt);

	public int size();
}
