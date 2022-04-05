package it.unical.dimes.elq.test;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.PriorityQueue;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;

import dataset.Action;
import dataset.Get;
import dataset.Put;
import it.unical.dimes.elq.ALadderQueue;
import it.unical.dimes.elq.AtomicEvent;
import it.unical.dimes.elq.Event;
import it.unical.dimes.elq.FQ;
import it.unical.dimes.elq.GroupingPriorityQueue;
import random.Camel;
import random.Exponential;
import random.Pareto;
import random.RandomVariable;
import random.SpecBimodal;
import random.SpecChange;

public class TestRefactored {
	static int queueSize;
	static final int numAccesses = 1_000_000;
	static final int TOT_RUN = 50;
	static final int NUM_RUN_SCRAP = 10;
//	static final long GARBAGE_COLLECTOR_SLEEP_TIME = 500;
	static final long GARBAGE_COLLECTOR_SLEEP_TIME = 0;
	static double meanIncr = 0.0;
	static int distribution;
	static final int meanContemp = 100;
	static int totAccesses = 2_000_000;

	private enum Structure {
		ALQ, ALQ_G, ALQ_GS, ALQ_GU, ALQ_GUS, PQ, PQ_G
	};

	public static void main(String[] args) {
		if (args.length != 2)
			throw new IllegalArgumentException();

		distribution = Integer.parseInt(args[0]);
		queueSize = Integer.parseInt(args[1]);
		LinkedList<Action> eventList = generate_random();
		test(eventList, Structure.ALQ);
		test(eventList, Structure.PQ);
		test(eventList, Structure.PQ_G);
		test(eventList, Structure.ALQ_GUS);
		test(eventList, Structure.ALQ_GU);
		test(eventList, Structure.ALQ_GS);
		test(eventList, Structure.ALQ_G);
	}
	
	private static void test(LinkedList<Action> list, Structure queue) {
		/*
		 * sit --> average time expressed in nanoseconds c --> temporary variable to
		 * store the min execution time m --> mean d --> temporary variable helping in
		 * computation m2 --> variance
		 */
		double m2 = 0, d = 0, m = 0, c=0;
		for (int i = 0; i < TOT_RUN; i++) {
			System.gc();
			try {
				Thread.sleep(GARBAGE_COLLECTOR_SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			/*
			 * If the execution is one of the first 'SCRAP' then do not compute average and
			 * variance. Just execute!
			 * 
			 */
			FQ queueInstance = instantiate(queue);
			if (i < NUM_RUN_SCRAP)
				fullTest(list, queueInstance);
			else {
				long c1 = fullTest(list, queueInstance);
				c = ((double)c1)/ totAccesses;
				d = c - m;
				m = m + d / (i - (NUM_RUN_SCRAP - 1));
				m2 = m2 +  (c*c-m2)/(i - (NUM_RUN_SCRAP - 1));
			}
		}
		double value = m;
		int n = TOT_RUN-NUM_RUN_SCRAP;
		System.out.print(" " + value+ " "+ Math.sqrt((m2-value*value)*n/(n-1)));
	}

	private static long fullTest(LinkedList<Action> list, FQ queueInstance) {
		ListIterator<Action> li = list.listIterator();
		Action x = null;
		boolean si = false;
		Event e = null;
		for (int j = 0; j < queueSize; j++) {
			x = li.next();
			queueInstance.enqueue(new AtomicEvent(x.getTs()));
		}
		x = li.next();
		if (!(x instanceof Get))
			throw new RuntimeException();
		e = queueInstance.dequeue();
		System.gc();
		try {
			Thread.sleep(GARBAGE_COLLECTOR_SLEEP_TIME);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		long time = System.nanoTime();
		while (li.hasNext()) {
			x = li.next();
			if (x instanceof Put) {
				queueInstance.enqueue(new AtomicEvent(x.getTs()));
			} else if (x instanceof Get) {
				e = queueInstance.dequeue();
				if (e.getTimeStamp() != x.getTs()) {
					si = true;
					break;
				}
			}
		}
		if (si) {
			throw new RuntimeException("");
		}
		time = System.nanoTime() - time;
		return time;
	}

	private static FQ instantiate(Structure queue) {
		FQ ret = null;
		switch (queue) {
		case ALQ:
			ret = new ALQAdapter(false, false, false);
			break;
		case ALQ_G:
			ret = new ALQAdapter(true, false, false);
			break;
		case ALQ_GS:
			ret = new ALQAdapter(true, false, true);
			break;
		case ALQ_GU:
			ret = new ALQAdapter(true, true, false);
			break;
		case ALQ_GUS:
			ret = new ALQAdapter(true, true, true);
			break;
		case PQ:
			ret = new PQAdapter();
			break;
		case PQ_G:
			ret = new GroupingPriorityQueue();
			break;
		}
		return ret;
	}

	private static LinkedList<Action> generate_random() {
		LinkedList<Action> list = new LinkedList<>();
		PriorityQueue<Long> putList = new PriorityQueue<>();
		final double factor = 1.0;
		RandomVariable r = null;
		switch (distribution) {
		case 1:
			r = new Exponential(1.0);
			break;
		case 2:
			r = new Exponential(1.0 / 3000.0);
			break;
		case 3:
			r = new Pareto(1, 1);
			break;
		case 4:
			r = new Pareto(1, 1.5);
			break;
		case 5:
			r = new Pareto(1, 700);
			break;
		case 6:
			r = new SpecChange();
			break;
		case 7:
			r = new Camel(0, 1000, 2, 0.001, 0.999);
			break;
		case 8:
			r = new SpecBimodal();
			break;
		}
		UniformIntegerDistribution ud = new UniformIntegerDistribution(0, 2 * meanContemp - 1);
		int bound;
		double ts = 0;
		int j = 0;
		int incrCount = 0;
		while (j < queueSize) {
			bound = ud.sample();
			double sd = factor * r.sample();
			double d = sd - meanIncr;
			meanIncr = meanIncr + d / (incrCount + 1);
			incrCount++;
			for (int i = 0; i < bound && j < queueSize; i++, j++) {
				long lRepr = Double.doubleToLongBits(ts + sd);
				list.add(new Put(lRepr));
				putList.add(lRepr);
			}
			ts += meanIncr;
		}
		// TEST Classic Hold (non è un vero classic hold )
		for (int jj = 0; jj < numAccesses; jj++) {
			ts = Double.longBitsToDouble(putList.remove());
			list.add(new Get(Double.doubleToLongBits(ts)));
			double s = (factor * r.sample());
			bound = ud.sample();
			int dup = 0;
			for (; dup < bound && jj < numAccesses; dup++, jj++) {
				long lRepr = Double.doubleToLongBits(ts + s);
				list.add(new Put(lRepr));
				putList.add(lRepr);
			}
			for (int k = 0; k < dup - 1; k++) {
				ts = Double.longBitsToDouble(putList.remove());
				list.add(new Get(Double.doubleToLongBits(ts)));
			}
		}
		return list;
	}
}