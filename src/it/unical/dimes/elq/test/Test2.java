package it.unical.dimes.elq.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.PriorityQueue;

import javax.management.RuntimeErrorException;

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

public class Test2 {
	static int qsize;
	static final int accesses = 1_000_000;
	static final int TOT = 50;
	static final int SCRAP = 10;
	static final long SLEEP = 500;
	static double meanincr = 0.0;
	static int distributions;
	static final int mean_contemp = 100;
	static int totacc = 2_000_000;

	public static void main(String[] args) {
		if (args.length != 2)
			throw new IllegalArgumentException();

		distributions = Integer.parseInt(args[0]);
		qsize = Integer.parseInt(args[1]);

		// EVENT LIST CREATION
		LinkedList<Action> list = generate_random();

		boolean write = true;
		if (write) {
			try {
				serializeToFile(list, Paths.get("/Users/angelo/ALQ/dump.obj"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				list = readFromSerializedToFile(Paths.get("/Users/angelo/ALQ/dump.obj"));
			} catch (Exception ex) {
			}
		}

		// testPQ(list);

		// System.exit(0);
		// EXPERIMENTS
		// Ladder Queue
		// System.out.print("LQ ");
		testALQ(list, false, false, false);

		// Priority Queue
		// System.out.print("PQ ");
		testPQ(list);

		// Priority Queue + Grouping
		// System.out.print("PQG ");
		testPQG(list);

		// Adaptive Ladder Queue + Grouping + UpGrowing + SmartSpawning
		// System.out.print("ALQ+GUS ");
		testALQ(list, true, true, true);

		// Adaptive Ladder Queue + Grouping + UpGrowing
		// System.out.print("ALQ+GU ");
		testALQ(list, true, true, false);

		// Adaptive Ladder Queue + Grouping + SmarSpawning
		// System.out.print("ALQ+GS ");
		testALQ(list, true, false, true);

		// Adaptive Ladder Queue + Grouping
		// System.out.print("ALQ+G ");
		testALQ(list, true, false, false);

	}

	private static void testPQ(LinkedList<Action> list) {
		/*
		 * sit --> average time expressed in nanoseconds c --> temporary variable to
		 * store the min execution time m --> mean d --> temporary variable helping in
		 * computation m2 --> variance
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
			 * If the execution is one of the first 'SRAP' then do not compute average and
			 * variance. Just execute!
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
		double value = m / totacc;
		System.out.print(" " + value);
	}

	public static long fullTestPQ(LinkedList<Action> list) {
		ListIterator<Action> li = list.listIterator();
		PriorityQueue<Event> pq = new PriorityQueue<>();
		Action x = null;
		boolean si = false;
		int i = 0;
		Event e = null;
		for (int j = 0; j < qsize; j++) {
			x = li.next();
			pq.offer(new AtomicEvent(x.getTs()));
		}
		x = li.next();
		if (!(x instanceof Get))
			throw new RuntimeException();
//       //Comment from here
//		long ts = 0;
//
//		long newTs = x.getTs();
//		if (newTs < ts) {
//			throw new RuntimeException("Time goes back from " + ts + "to " + newTs);
//
//		} else
//			ts = newTs;
//		// end comment here
		e = pq.remove();
		System.gc();
		try {
			Thread.sleep(SLEEP);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		long time = System.nanoTime();

		while (li.hasNext()) {
			x = li.next();
//			//Comment from  here
//			newTs = x.getTs();
//			if (newTs < ts) {
//				throw new RuntimeException("Time goes back from " + ts + "to " + newTs);
//			}
//			//to here

			if (x instanceof Put) {
				pq.offer(new AtomicEvent(x.getTs()));
			} else if (x instanceof Get) {
				e = pq.remove();
//				ts=newTs;//comment here
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
		double value = m / totacc;
		System.out.print(" " + value);
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
		double value = m / totacc;
		System.out.print(" " + value);

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

	static void dumpToFile(LinkedList<Action> list, Path p) throws IOException {
		try (PrintWriter pw = new PrintWriter(new FileWriter(p.toFile()))) {
			java.util.List<Long> ll = new ArrayList<>();
			for (Action a : list) {
				if (a instanceof Put)
					ll.add(a.getTs());
			}
			Collections.sort(ll);
			for (Long l : ll)
				pw.println(Double.doubleToLongBits(l));
		}

	}

	static void serializeToFile(LinkedList<Action> list, Path p) throws IOException {
		try (ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(p.toFile()))) {
			oo.writeObject(list);
		}

	}

	static LinkedList<Action> readFromSerializedToFile(Path p) throws IOException, ClassNotFoundException {
		LinkedList<Action> rv = new LinkedList<>();
		long minTs = Long.MAX_VALUE;

		try (ObjectInputStream oi = new ObjectInputStream(new FileInputStream(p.toFile()))) {
			LinkedList<Action> list = (LinkedList<Action>) oi.readObject();
			for (Action a : list) {
				long v = a.getTs();
				if (v < minTs)
					minTs = v;
			}
			for (Action a : list) {
				if (a instanceof Get) {
					rv.add(new Get(a.getTs() - minTs));
				} else {
					rv.add(new Put(a.getTs() - minTs));
				}
			}
		}
		return rv;
	}

	private static LinkedList<Action> generate_random() {
		LinkedList<Action> list = new LinkedList<>();
		PriorityQueue<Long> putList = new PriorityQueue<>();

		final double factor = 1.0;
		RandomVariable r = null;

		switch (distributions) {
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

		UniformIntegerDistribution ud = new UniformIntegerDistribution(0, 2 * mean_contemp - 1);
		int bound;

		double ts = 0;

		int j = 0;
		int incrCount = 0;
		while (j < qsize) {
			bound = ud.sample();
			double sd = factor * r.sample();
			double d = sd - meanincr;
			meanincr = meanincr + d / (incrCount + 1);
			// System.out.println("incr#="+incrCount+" incr="+meanincr);

			incrCount++;
			// System.out.println(ts + sd);
			for (int i = 0; i < bound && j < qsize; i++, j++) {
				long lRepr = Double.doubleToLongBits(ts + sd);
				list.add(new Put(lRepr));
				putList.add(lRepr);
			}
			ts += meanincr;
		}

		// TEST CH (non è un vero classic HOLD )
		for (int jj = 0; jj < accesses; jj++) {

			ts = Double.longBitsToDouble(putList.remove());
			list.add(new Get(Double.doubleToLongBits(ts)));
			double s = (factor * r.sample());

			bound = ud.sample();
			int dup = 0;
			for (; dup < bound && jj < accesses; dup++, jj++) {
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
