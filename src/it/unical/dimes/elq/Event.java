package it.unical.dimes.elq;

public interface Event extends Comparable< Event> {
	long getTimeStamp();

	CompositeEvent asComposite();
}