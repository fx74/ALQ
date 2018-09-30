package it.unical.dimes.elq;

public interface Event extends Comparable< Event> {
	double getTimeStamp();

	CompositeEvent asComposite();
}
