package it.unical.dimes.elq.test;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.PriorityQueue;

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

public class Test {
	static int qsize;
	static final int accesses = 1_000_000;
	static final int TOT = 50;
	static final int SCRAP = 10;
	static final long SLEEP = 500;
	static double meanincr = 0.0;
	static int distributions;

	public static void main(String[] args) {
		if (args.length != 2)
			throw new IllegalArgumentException();

		distributions = Integer.parseInt(args[0]);
		qsize = Integer.parseInt(args[1]);

		// EVENT LIST CREATION
		LinkedList<Action> list = generate_random();

		// EXPERIMENTS
		// Ladder Queue
		System.out.print("LQ ");
		testALQ(list, false, false, false);
		
		// Priority Queue
		System.out.print("PQ ");
		testPQ(list);

		// Priority Queue + Grouping
		System.out.print("PQG ");
		testPQG(list);

		// Adaptive Ladder Queue + Grouping + UpGrowing + SmartSpawning
		System.out.print("ALQ+GUS ");
		testALQ(list, true, true, true);

		// Adaptive Ladder Queue + Grouping + UpGrowing
		System.out.print("ALQ+GU ");
		testALQ(list, true, true, false);

		// Adaptive Ladder Queue + Grouping + SmarSpawning
		System.out.print("ALQ+GS ");
		testALQ(list, true, false, true);

		// Adaptive Ladder Queue + Grouping
		System.out.print("ALQ+G ");
		testALQ(list, true, false, false);


	}

	private static void testPQ(LinkedList<Action> list) {
		/*
		 * sit --> average time expressed in nanoseconds 
		 * c --> temporary variable to store the min execution time 
		 * m --> mean 
		 * d --> temporary variable helping in computation 
		 * m2 --> variance
		 */
		long sit = 0, c = 0;
		double m2 = 0, d = 0, m = 0;
		long min = Long.MAX_VALUE;

		for (int i = 0; i < TOT; i++) {
			System.gc();
			try {
				Thread.sleep(SLEEP);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			/*
			 * If the execution is one of the first 'SRAP' then do not compute
			 * average and variance. Just execute!
			 * 
			 */
			if (i < SCRAP)
				c = fullTestPQ(list);
			else {

				c = fullTestPQ(list);
				sit = sit + c;
				d = c - m;
				m = m + d / (i - (SCRAP - 1));
				m2 = m2 + d * (c - m);
			}
			if (c < min)
				min = c;
		}
		sit = sit / (TOT - SCRAP);
		System.out.println(m);
	}

	public static long fullTestPQ(LinkedList<Action> list) {
		ListIterator<Action> li = list.listIterator();
		PriorityQueue<Event> ladder = new PriorityQueue<>();
		Action x = null;
		boolean si = false;
		int i = 0;
		Event e = null;
		for (int j = 0; j < qsize; j++) {
			x = li.next();
			ladder.offer(new AtomicEvent(x.getTs()));
		}

		x = li.next();
		if (!(x instanceof Get))
			throw new RuntimeException();

		e = ladder.remove();
		System.gc();
		try {
			Thread.sleep(SLEEP);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		long time = System.nanoTime();

		while (li.hasNext()) {
			x = li.next();

			if (x instanceof Put) {
				ladder.offer(new AtomicEvent(x.getTs()));
			} else if (x instanceof Get) {
				e = ladder.remove();

			}
			i++;

		}
		time = System.nanoTime() - time;
		if (si) {
			throw new RuntimeException("");
		}

		return time;
	}


	private static void testPQG(LinkedList<Action> list) {
		long min = Long.MAX_VALUE;
		long sit = 0, c = 0;
		double m2 = 0, d = 0, m = 0;
		for (int i = 0; i < TOT; i++) {
			System.gc();
			try {
				Thread.sleep(SLEEP);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			FQ ladder = new GroupingPriorityQueue();
			if (i < SCRAP)
				c = fullTest(list, ladder);
			else {

				c = fullTest(list, ladder);
				sit = sit + c;
				d = c - m;
				m = m + d / (i - (SCRAP - 1));
				m2 = m2 + d * (c - m);
			}

			if (c < min)
				min = c;
		}
		sit = sit / (TOT - SCRAP);
		System.out.println(m);
	}

	public static long fullTest(LinkedList<Action> list, FQ ladder) {
		ListIterator<Action> li = list.listIterator();

		Action x = null;
		boolean si = false;
		int i = 0;
		Event e = null;
		for (int j = 0; j < qsize; j++) {
			x = li.next();
			ladder.enqueue(new AtomicEvent(x.getTs()));
		}
		x = li.next();
		if (!(x instanceof Get))
			throw new RuntimeException();

		e = ladder.dequeue();

		System.gc();
		try {
			Thread.sleep(SLEEP);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		long time = System.nanoTime();
		while (li.hasNext()) {
			x = li.next();

			if (x instanceof Put) {
				ladder.enqueue(new AtomicEvent(x.getTs()));
			} else if (x instanceof Get) {
				e = ladder.dequeue();
				if (e.getTimeStamp() != x.getTs()) {
					si = true;
					break;
				}
			}
			i++;
		}

		if (si) {
			throw new RuntimeException("");
		}

		time = System.nanoTime() - time;
		
		return time;
	}

	private static void testALQ(LinkedList<Action> list, boolean grouping, boolean upgrowing, boolean smartspawn) {
		long min = Long.MAX_VALUE;
		long sit = 0, c = 0;
		double m2 = 0, d = 0, m = 0;
		for (int i = 0; i < TOT; i++) {
			System.gc();
			try {
				Thread.sleep(SLEEP);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			ALadderQueue ladder = new ALadderQueue(grouping, upgrowing, smartspawn);
			if (i < SCRAP)
				c = fullTestALQ(list, ladder);
			else {

				c = fullTestALQ(list, ladder);
				sit = sit + c;
				d = c - m;
				m = m + d / (i - (SCRAP - 1));
				m2 = m2 + d * (c - m);
			}
			if (c < min)
				min = c;
		}
		sit = sit / (TOT - SCRAP);
		System.out.println(m);
	}

	public static long fullTestALQ(LinkedList<Action> list, ALadderQueue ladder) {
		ListIterator<Action> li = list.listIterator();

		Action x = null;
		boolean si = false;
		int i = 0;
		Event e = null;
		for (int j = 0; j < qsize; j++) {
			x = li.next();
			ladder.enqueue(new AtomicEvent(x.getTs()));
		}
		x = li.next();
		if (!(x instanceof Get))
			throw new RuntimeException();

		e = ladder.dequeue();

		System.gc();
		try {
			Thread.sleep(SLEEP);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		long time = System.nanoTime();
		while (li.hasNext()) {
			x = li.next();

			if (x instanceof Put) {
				ladder.enqueue(new AtomicEvent(x.getTs()));
			} else if (x instanceof Get) {
				e = ladder.dequeue();
				if (e.getTimeStamp() != x.getTs()) {
					si = true;
					break;
				}
			}
			
			i++;
			
		}

		if (si) {
			throw new RuntimeException("");
		}

		time = System.nanoTime() - time;
		
		return time;
	}

	private static LinkedList<Action> generate_random() {
		LinkedList<Action> list = new LinkedList<>();
		PriorityQueue<Long> putList = new PriorityQueue<>();

		final double factor = 1.0;
		RandomVariable r = null;
		
		switch(distributions){
		case 1: r=new Exponential(1.0);
				break;
		case 2: 
			r=new Exponential(1.0 / 3000.0);
			break;
		case 3:
			r=new Pareto(1,1);
			break;
		case 4:
			r=new Pareto(1,1.5);
			break;
		case 5:
			r=new Pareto(1,700);
			break;
		case 6:
			r=new SpecChange();
			break;
		case 7:
			r=new Camel(0,1000,2,0.001,0.999);
			break;
		case 8:
			r=new SpecBimodal();
			break;
		}

		long ts = 0;
		for (int i = 0; i < qsize; i++) {
			double sd = factor * r.sample();
			long s = (long) (sd);

			// estimation expected value
			double d = sd - meanincr;
			meanincr = meanincr + d / (i + 1);

			list.add(new Put(ts + s));
			putList.add(ts + s);
			if (i > 0 && i % (qsize / 10) == 0) {
				ts += meanincr;
			}

		}
		for (int j = 0; j < accesses; j++) {
			ts = putList.remove();
			list.add(new Get(ts));
			long s = (long) (factor * r.sample());
			list.add(new Put(ts + s));
			putList.add(ts + s);
		}

		return list;

	}
}
