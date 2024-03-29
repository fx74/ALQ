package it.unical.dimes.elq;

import java.util.ArrayDeque;
import java.util.Deque;

public class ALadderQueue {
	private int THRES = 64; // Threshold to start spawning in Bottom tier
	// private int THRES_TOP = 16 * THRES; //Threshold to indicate max number of
	// events in Top tier
	private int THRES_TOP = 48 * THRES; // Threshold to indicate max number of events in Top tier
	private int MAX_RUNGS = 10; // Maximum number of Rungs
	private int MAX_RUNGS_UP = 3;

	private final boolean grouping;
	private final boolean upgrowing;
	private final boolean smartspawn;

	private int size = 0;

	// top data structures
	private LinkedEventList top = new LinkedEventList();
	private long MinTS = -1L;
	private long MaxTS = -1L;
	private double m = 0;
	private double m2 = 0;
	private final double alpha = 1.7;
	private long upper = 0;
	private boolean updated = false;
	private long topStart = -1;
	private int rungused = 0;
	private int rungUpgrowing = 0;

	// rungs
	private Deque<Rung> rungs = new ArrayDeque<>(MAX_RUNGS);

	// bottom
	private EventList bottom;

	// statistics
	private int topInsert;
	private int rungInsert;
	private int bottomInsert;
	private int upgrowingCount = 0; // number of upgrowing performed
	private int smartSpawnCount = 0; // number of smartspawn performed
	/*
	 * Constructor with possibility to set: - grouping - upgrowing - smartspawning -
	 * THRES - THRES_TOP - MAX_RUNGS
	 * 
	 * For the first 3 parameters, when true operation is applied otherwise no
	 */

	public ALadderQueue(final boolean grouping, final boolean upgrowing, final boolean smartspawn, int THRES,
			int THRES_TOP, int MAX_RUNGS) {
		this.grouping = grouping;
		this.upgrowing = upgrowing;
		this.smartspawn = smartspawn;
		this.THRES = THRES;
		this.THRES_TOP = THRES_TOP;
		this.MAX_RUNGS = MAX_RUNGS;
	}

	/*
	 * Constructor with possibility to set: - grouping - upgrowing - smartspawning
	 * 
	 * When true operation is applied otherwise no
	 */
	public ALadderQueue(final boolean grouping, final boolean upgrowing, final boolean smartspawn) {
		this.grouping = grouping;
		this.upgrowing = upgrowing;
		this.smartspawn = smartspawn;
	}

	// default constructor
	public ALadderQueue() {
		this(true, true, true);
	}

	public void enqueue(Event evt) {
		assert (evt.asComposite() == null);
		++size;
		final long ts = evt.getTimeStamp();
	
		/*if (ts == 25343588488683292L) {
			System.out.println();
		}
		if (ts == 25342710274932256L) {
			System.out.println();

		}*/

		if (ts >= topStart) {// insert in top
			topInsert++;
			if (top.eventCount() == 0) {
				MinTS = MaxTS = ts;
			} else {
				if (ts > MaxTS)
					MaxTS = ts;
				if (ts < MinTS)
					MinTS = ts;
			}
			top.add(evt, grouping);
			if (smartspawn) {
				smartSpawnStatsTopEnqueue(ts, 1, top.eventCount());
			}
			if (upgrowing) {
				handleUpgrowing();
			}
			return;
		}

		if (bottom != null && !bottom.isEmpty() && ts < bottom.getFirst().getTimeStamp()) {
			bottom.addFirst(evt);
			bottomInsert++;
			return;
		}

		for (Rung rung : rungs) {
			if (ts >= rung.rCur) {
				// insert in a rung
				rung.add(evt);
				rungInsert++;
				return;
			}
		}

		if (bottom == null)
			bottom = new LinkedEventList();
		else if (bottom.nodeCount() > THRES && rungs.size() > 0
				&& bottom.getFirst().getTimeStamp() != bottom.getLast().getTimeStamp()) {

			Rung r = null;
			r = createNewRungDown(bottom);
			if (r != null) {
				assert r == rungs.getLast();

				while (!bottom.isEmpty()) {
					r.add(bottom.removeFirst());
				}
				r.add(evt);
				rungs.addLast(r);
				if (rungs.size() > rungused)
					rungused = rungs.size();
				rungInsert++;
				bottom = null;
				return;
			} // else not possible to create a new rung
			assert false;
		}

		// insert in bottom
		bottomInsert++;
		bottom.addInOrder(evt, grouping);

	}

	private void smartSpawnStatsTopEnqueue(final long ts_long, final int occurrences, final int newsize) {
		double ts=Double.longBitsToDouble(ts_long);
		double d = ts - m;
		m = m + occurrences * (d / newsize);
		m2 = m2 + occurrences * (ts*ts-m2)/newsize;
//		m2 = m2 + occurrences * d * (ts - m);
		updated = false;
	}

	private void handleUpgrowing() {
		/*
		 * if (top.nodeCount() >= THRES_TOP) transferTopToRung();
		 */
		if (top.nodeCount() >= THRES_TOP && rungUpgrowing < MAX_RUNGS_UP) {
			if (transferTopToRung()) {
				rungUpgrowing++;
				upgrowingCount++;
			}
		}
	}

	private void smartTransferTopToBottom() {
		if (bottom == null)
			bottom = new LinkedEventList();
		long upper = getMaxTop();
		m = 0;
		m2 = 0;
		long max = -1;
		long min = Long.MAX_VALUE;
		long maxMoved = -1;
		LinkedEventList newTop = new LinkedEventList();
		while (!top.isEmpty()) {
			Event evt = top.removeFirst();
			long currTs = evt.getTimeStamp();
			if (currTs <= upper) {
				if (currTs > maxMoved)
					maxMoved = currTs;
				bottom.add(evt, grouping);
			} else {
				if (currTs > max)
					max = currTs;
				if (currTs < min)
					min = currTs;
				newTop.add(evt, grouping);
				int occ = 1;
				CompositeEvent cEvt = evt.asComposite();
				if (cEvt != null)
					occ = cEvt.size();
				smartSpawnStatsTopEnqueue(currTs, occ, newTop.eventCount());
			}
		}
		top = newTop;
		if (top.isEmpty()) {
			MinTS = -1L;
			MaxTS = -1L;
		} else {
			MinTS = min;
			MaxTS = max;
		}
		topStart = maxMoved;
	}

	private boolean transferTopToRung() {
		Rung r = createNewRungUp();
		if (r == null)
			return false;

		long upper = getMaxTop();

		if (smartspawn) {
			m = 0;
			m2 = 0;
			long max = -1;
			long min = Long.MAX_VALUE;
			long maxRung = -1;
			LinkedEventList newTop = new LinkedEventList();
			while (!top.isEmpty()) {
				Event evt = top.removeFirst();
				long currTs = evt.getTimeStamp();
				if (currTs <= upper) {
					if (currTs > maxRung)
						maxRung = currTs;
					r.add(evt);
				} else {
					if (currTs > max)
						max = currTs;
					if (currTs < min)
						min = currTs;
					newTop.add(evt, grouping);
					int occ = 1;
					CompositeEvent cEvt = evt.asComposite();
					if (cEvt != null)
						occ = cEvt.size();
					smartSpawnStatsTopEnqueue(currTs, occ, newTop.eventCount());
				}
			}
			if (!newTop.isEmpty())
				smartSpawnCount++;
			top = newTop;
			if (top.isEmpty()) {
				MinTS = -1L;
				MaxTS = -1L;
			} else {
				MinTS = min;
				MaxTS = max;
			}
			topStart = maxRung;
		} else {
			topStart = MaxTS;
			while (!top.isEmpty()) {
				r.add(top.removeFirst());
			}
			MinTS = -1;
			MaxTS = -1;
		}
		rungs.addFirst(r);
		if (rungs.size() > rungused)
			rungused = rungs.size();
		return true;
	}

	public int size() {
		return size;
	}

	/**
	 * It returns event with the highest priority: element is first searched in
	 * Bottom (sorted), if latter is empty, then the element with the highest
	 * priority is searched in the last rung. Once the element to be returned is
	 * found in the bucket, number of events of the bucket is compared to THRES: -
	 * if < THRES, after sorting the bucket is moved into the bottom returning the
	 * most priority event; - otherwise, a new rung is generated transferring
	 * elements in the considered bucket and so on. If also the Ladder is empty,
	 * elements in Top are moved in a rung (the first one) and process is
	 * iterated.If the structure is empty an error comes out.
	 * 
	 * @return Event with the highest priority
	 * 
	 */
	public Event dequeue() {
		--size;
		if (bottom == null || bottom.isEmpty()) {

			if (rungs.size() == 0) {
				long upper = getMaxTop();

				if ((upper - MinTS) / THRES_TOP == 0) {
					if (upper == MaxTS) {// trasfers entire top
						transferTopToBottom();
					} else {
						smartTransferTopToBottom();
					}
				} else {
					if (!transferTopToRung()) {
						if (upper == MaxTS) {// trasfers entire top
							transferTopToBottom();
						} else {
							smartTransferTopToBottom();
						}
					}
				}
			}

			if (rungs.size() > 0) {
				int k = recurseRung();
				Rung lastRung = rungs.getLast();
				// transfer on bottom
				bottom = lastRung.bucket[k];
				lastRung.bucket[k] = null;
				lastRung.bucketCount--;
				lastRung.minBucket++;
				if (k == lastRung.maxBucket) {
					rungs.removeLast();
					rungused = rungs.size();
					if (rungused < MAX_RUNGS_UP && rungUpgrowing>rungused)
						rungUpgrowing = rungused;
				} else {
					lastRung.rCur = lastRung.rStart + lastRung.minBucket * lastRung.bucketWidth;
				}

				checkRungs();
			}
			bottom.sort(grouping);
		}

		// --size;

		return bottom.removeFirstAtomic();
	}

	private void transferTopToBottom() {
		topStart = MaxTS;
		bottom = top;
		top = new LinkedEventList();
		MinTS = -1;
		MaxTS = -1;

		if (smartspawn) {
			m = 0;
			m2 = 0;
		}

	}

	private void checkRungs() {
		while (rungs.size() > 0) {
			Rung lastRung = rungs.getLast();
			while (lastRung.minBucket <= lastRung.maxBucket && (lastRung.bucket[lastRung.minBucket] == null
					|| lastRung.bucket[lastRung.minBucket].nodeCount() == 0))
				++lastRung.minBucket;

			if (lastRung.minBucket > lastRung.maxBucket) {
				rungs.removeLast();

			} else {
				break;
			}
		}
	}

	private int recurseRung() {
		assert rungs.size() > 0;
		Rung lastRung = rungs.getLast();
		int k = lastRung.minBucket;

		while (k <= lastRung.maxBucket && (lastRung.bucket[k] == null || lastRung.bucket[k].nodeCount() == 0)) {
			++k;
		}

		lastRung.rCur = lastRung.rStart + (lastRung.bucketWidth * (k));

		assert k > lastRung.maxBucket;

		EventList bucket_k = lastRung.bucket[k];
		if (bucket_k.nodeCount() > THRES && lastRung.bucketWidth > 1) {
			Rung r = createNewRungDown(bucket_k);
			if (r == null) {
				lastRung.minBucket = k;
				return k;// It was not possible to create a new rung
			}
			while (!bucket_k.isEmpty()) {
				r.add(bucket_k.removeFirst());
			}
			lastRung.bucketCount--;
			lastRung.minBucket = k + 1;
			lastRung.rCur = lastRung.rStart + lastRung.minBucket * lastRung.bucketWidth;
			// checkRungs(); TODO danni!!!!rimosso
			rungs.addLast(r);
			if (rungs.size() > rungused)
				rungused = rungs.size();
			return recurseRung();
		} else {
			lastRung.minBucket = k;
			return k;
		}
	}

	private Rung createNewRungUp() {
		return createNewRung(top, true);
	}

	private Rung createNewRungDown(EventList elist) {
		return createNewRung(elist, false);
	}

	private Rung createNewRung(EventList elist, final boolean up) {
		assert rungs.size() <= MAX_RUNGS;

		int rsize = rungs.size();

		if (rsize >= MAX_RUNGS)
			return null;

		long bw;
		long w;
		long rStart;

		if (elist == top) {
			assert rsize == 0 || upgrowing;
			long min = MinTS;
			if (rsize > 0) {
				min = topStart;
			} else {
				min = MinTS;
			}
			w = getMaxTop() - min + 1;// TODO attento al +1!!
			long nc = THRES;
			bw = w / nc;
			if (w % THRES != 0)
				bw++;
			if (bw == 0)
				return null;
			assert bw != 0;
			rStart = min;
		} else if (elist == bottom) {
			Rung lastRung = rungs.getLast();
			rStart = bottom.getFirst().getTimeStamp();
			w = lastRung.rCur - rStart ;// TODO +1?
			bw = w / THRES;
			if (w % THRES != 0)
				bw++;
			if (bw == 0)
				return null;// it is not possible to create a new rung with bw=0
		} else {
			Rung lastRung = rungs.getLast();
			w = lastRung.bucketWidth;
			// bw = lastRung.bucketWidth / elist.nodeCount();
			bw = w / THRES; // TODO ripristinare
			if (w % THRES != 0)
				bw++;
			if (bw == 0)
				return null;// it is not possible to create a new rung with bw=0
			rStart = lastRung.rCur;
		}

		Rung r = new Rung(w, bw);
		r.rCur = r.rStart = rStart;

		return r;
	}

	private long getMaxTop() {
		if (smartspawn) {
			if (updated)
				return upper;
			int n = top.eventCount();
			upper = Double.doubleToLongBits(m + alpha * Math.sqrt((m2-m*m)*n/(n-1)));
//			upper = (long) (m + alpha * Math.sqrt(m2) / (top.eventCount() - 1));
			if (upper > MaxTS)
				upper = MaxTS;

			if (upper < MinTS)
				upper = MinTS;
			updated = true;
			return upper;
		}
		return MaxTS;
	}

	private final class Rung {
		long bucketWidth;
		long rStart;
		long rCur;

		int bucketCount = 0;

		EventList[] bucket;

		int minBucket = 0;
		int maxBucket = 0;

		/**
		 * Rung constructor with a specified number of bucket
		 * 
		 * @param n number of buckets to create
		 */
		Rung(final long w, final long bw) {

			int s = (int) (w / bw);
			if (w % bw != 0)
				s++;
			/*if (s != 64) {
				System.out.println("strange size " + s);
				// s=65;

			}*/

			bucket = new EventList[s];
			init(bw);
		}

		void init(long bktWidth) {
			bucketWidth = bktWidth;

			bucketCount = minBucket = maxBucket = 0;
		}

		/**
		 * It adds an event to the rung in the i-th bucket, where i is given by:
		 * i=(TS-rStart)/bucketwidth
		 * 
		 * @param evt Event to add to the rung
		 */
		void add(Event evt) {
			int k = 0;
			if (bucketWidth > 0) {
				k = (int) ((evt.getTimeStamp() - rStart) / bucketWidth);
				if (bucket[k] == null) {
					bucket[k] = new LinkedEventList();
					bucketCount++;
				}
				bucket[k].add(evt, grouping);
				if (bucketCount == 1) {
					minBucket = maxBucket = k;
				} else {
					if (k < minBucket)
						minBucket = k;
					if (k > maxBucket)
						maxBucket = k;
				}
			} else {
				assert false;
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(500);
			sb.append("rStart=" + rStart);
			sb.append(" with rCur=" + rCur);
			sb.append(" with minBucket=" + minBucket);
			sb.append(" with maxBucket=" + maxBucket);
			sb.append(" with bucketWidth=" + bucketWidth);
			sb.append(" bkts " + bucketCount);
			return sb.toString();
		}

	}

	public int getBottomInsert() {
		return bottomInsert;
	}

	public int getTopInsert() {
		return topInsert;
	}

	public int getRungInsert() {
		return rungInsert;
	}

	public int getRungused() {
		return rungused;
	}

	public int getUpgrowingCount() {
		return upgrowingCount;
	}

	public int getSmartSpawnCount() {
		return smartSpawnCount;
	}

}
