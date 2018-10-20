package com.philips.sgse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

public class PoolTest {

	public static enum PoolItemType {
		TYPE_A, TYPE_B, TYPE_C
	}

	public static class PoolItem {
		public static AtomicInteger poolID = new AtomicInteger();

		public final int id = poolID.getAndIncrement();

		public List<PoolItemType> supportedTypes = new ArrayList<>();

		public PoolItem(PoolItemType... types) {
			this.supportedTypes = Arrays.asList(types);
		}

		@Override
		public String toString() {
			return "PoolItem:" + id;
		}
	}

	@Rule
	public TestRule globalTimeout = new DisableOnDebug(Timeout.seconds(120));

	private Pool<PoolItem> pool;

	@Before
	public void createPool() {
		pool = new Pool<>();
	}

	@After
	public void stopPoolIfStarted() {
		if (pool.getState() == Pool.ThreadState.STARTED) {
			pool.stop();
		}
	}

	@Test
	public void emptyPool_sizeZero() {
		assertEquals(0, pool.size());
	}

	@Test
	public void addItem() {
		pool.add(new PoolItem());
		assertEquals(1, pool.size());
	}

	@Test
	public void initialstate_stopped() {
		assertEquals(Pool.ThreadState.STOPPED, pool.getState());
	}

	@Test
	public void start_getState_started() throws InterruptedException {
		pool.start();
		assertEquals(Pool.ThreadState.STARTED, pool.getState());
	}
	
	@Test
	public void start_whileStarted_noException() throws InterruptedException {
		pool.start();
		pool.start();
	}
	
	@Test
	public void stop_whileStopped_noException() throws InterruptedException {
		pool.start();
		pool.stop();
		pool.stop();
	}
	
	@Test
	public void stop_whileInitialized_noException() {
		pool.stop();
	}

	@Test
	public void startThenStop_getState_Stopped() throws InterruptedException {
		pool.start();
		pool.stop();
		assertEquals(Pool.ThreadState.STOPPED, pool.getState());
	}

	@Test
	public void restart_getState_Started() throws InterruptedException {
		pool.start();
		pool.stop();
		pool.start();
		assertEquals(Pool.ThreadState.STARTED, pool.getState());
	}

	@Test
	public void poolItemEquals() {
		PoolItem item1 = new PoolItem();
		PoolItem item2 = new PoolItem();
		assertEquals(item1, item1);
		assertEquals(item2, item2);
		assertNotEquals(item1, item2);
	}

	@Test
	public void singleItemAddThenStart_acquire_itemReturned() throws InterruptedException {
		PoolItem item = new PoolItem();
		pool.add(item);
		pool.start();

		PoolItem acquiredItem = pool.acquire();

		assertEquals(item, acquiredItem);
	}

	@Test
	public void startThanAdd_throwsException() throws InterruptedException {
		PoolItem item = new PoolItem();
		pool.start();
		try {
			pool.add(item);
			fail();
		} catch (IllegalStateException e) {
			// expected
		}
	}

	@Test
	public void addSingleThenAcquire_getSize_sizeOne() throws InterruptedException {
		pool.add(new PoolItem());
		pool.start();

		pool.acquire();

		assertEquals(1, pool.size());
	}

	@Test
	public void acquireThenRelease_getSize_sizeOne() throws InterruptedException {
		pool.add(new PoolItem());
		pool.start();

		PoolItem item = pool.acquire();
		pool.release(item);

		assertEquals(1, pool.size());
	}

	@Test
	public void acquire_moreThanSize_blocks() throws InterruptedException {
		List<Throwable> errorsInThread = new CopyOnWriteArrayList<>();
		Thread blockedAcquire = new Thread(() -> {
			try {
				pool.acquire();
			} catch (InterruptedException t) {
				errorsInThread.add(t);
			}
		});

		pool.add(new PoolItem());
		pool.start();
		pool.acquire();
		blockedAcquire.start();
		assertThreadState(Thread.State.WAITING, blockedAcquire);
		assertEquals(0, errorsInThread.size());
	}

	@Test
	public void acquireBlockedUntilRelease() throws InterruptedException {
		List<PoolItem> acquiredItems = new CopyOnWriteArrayList<>();
		List<Throwable> errorsInThread = new CopyOnWriteArrayList<>();
		Thread acquireThread = new Thread(() -> {
			try {
				acquiredItems.add(pool.acquire());
			} catch (InterruptedException t) {
				errorsInThread.add(t);
			}
		});

		PoolItem item = new PoolItem();
		pool.add(item);
		pool.start();
		PoolItem acquiredItem = pool.acquire();
		acquireThread.start();
		assertThreadState(Thread.State.WAITING, acquireThread);
		pool.release(acquiredItem);
		assertThreadState(Thread.State.TERMINATED, acquireThread);
		assertEquals(1, acquiredItems.size());
		assertEquals(item, acquiredItems.get(0));
		assertEquals(0, errorsInThread.size());
	}

	@Test
	public void stopWhenWatingForAcquire_acquireCancelled() throws InterruptedException {
		List<PoolItem> acquiredItems = new CopyOnWriteArrayList<>();
		List<Throwable> errorsInThread = new CopyOnWriteArrayList<>();
		Thread acquireThread = new Thread(() -> {
			try {
				acquiredItems.add(pool.acquire());
			} catch (InterruptedException t) {
				errorsInThread.add(t);
			}
		});

		pool.add(new PoolItem());
		pool.start();
		pool.acquire();
		acquireThread.start();
		assertThreadState(Thread.State.WAITING, acquireThread);
		pool.stop();
		assertThreadState(Thread.State.TERMINATED, acquireThread);
		assertEquals(0, acquiredItems.size());
		assertEquals(1, errorsInThread.size());
		assertEquals(InterruptedException.class, errorsInThread.get(0).getClass());
	}
	
	@Test
	public void startStopStart_stillWorks() throws InterruptedException {
		pool.add(new PoolItem());
		pool.start();
		PoolItem acquired = pool.acquire();
		pool.release(acquired);
		pool.stop();
		pool.start();
		assertEquals(acquired, pool.acquire());
	}

	@Test
	public void acquireWithCondition_correctItemAcquired() throws InterruptedException {
		PoolItem itemA = new PoolItem(PoolItemType.TYPE_A);
		PoolItem itemB = new PoolItem(PoolItemType.TYPE_B);

		pool.add(itemA);
		pool.add(itemB);

		pool.start();

		PoolItem acquired = pool.acquire(item -> item.supportedTypes.contains(PoolItemType.TYPE_B));
		assertEquals(itemB, acquired);
	}

	@Test
	public void acquireWithCondition_blocksUntilConditionMet() throws InterruptedException {
		List<PoolItem> acquiredItems = new CopyOnWriteArrayList<>();
		List<Throwable> errorsInThread = new CopyOnWriteArrayList<>();
		Thread acquireThread = new Thread(() -> {
			try {
				acquiredItems.add(pool.acquire(item -> item.supportedTypes.contains(PoolItemType.TYPE_B)));
			} catch (InterruptedException t) {
				errorsInThread.add(t);
			}
		});

		PoolItem itemA = new PoolItem(PoolItemType.TYPE_A);
		PoolItem itemB = new PoolItem(PoolItemType.TYPE_B);
		pool.add(itemA);
		pool.add(itemB);

		pool.start();

		PoolItem acquiredItem = pool.acquire(item -> item.supportedTypes.contains(PoolItemType.TYPE_B));
		acquireThread.start();
		assertThreadState(State.WAITING, acquireThread);
		assertEquals(0, acquiredItems.size());
		pool.release(acquiredItem);
		assertThreadState(State.TERMINATED, acquireThread);
		assertEquals(1, acquiredItems.size());
		assertEquals(itemB, acquiredItems.get(0));
	}

	@Test
	public void addRemove_getSize_sizeZero() {
		PoolItem item = new PoolItem();
		pool.add(item);
		pool.remove(item);
		assertEquals(0, pool.size());
	}

	@Test
	public void removeNotScheduled() throws InterruptedException {
		List<PoolItem> acquiredItems = new CopyOnWriteArrayList<>();
		List<Throwable> errorsInThread = new CopyOnWriteArrayList<>();
		Thread acquireThread = new Thread(() -> {
			try {
				acquiredItems.add(pool.acquire(item -> item.supportedTypes.contains(PoolItemType.TYPE_A)));
			} catch (InterruptedException t) {
				errorsInThread.add(t);
			}
		});

		PoolItem itemA = new PoolItem(PoolItemType.TYPE_A);
		PoolItem itemB = new PoolItem(PoolItemType.TYPE_B);
		pool.add(itemA);
		pool.add(itemB);

		pool.start();
		PoolItem acquired = pool.acquire(item -> item.supportedTypes.contains(PoolItemType.TYPE_A));
		acquireThread.start();
		assertThreadState(State.WAITING, acquireThread);
		pool.remove(acquired);
		assertThreadState(State.WAITING, acquireThread);
	}

	@Test
	public void releaseThenAcquireWithCondition_returnsCorrectItem() throws InterruptedException {
		PoolItem itemA = new PoolItem(PoolItemType.TYPE_A);
		PoolItem itemB = new PoolItem(PoolItemType.TYPE_B);
		PoolItem itemC = new PoolItem(PoolItemType.TYPE_C);

		pool.add(itemA);
		pool.add(itemB);
		pool.add(itemC);

		pool.start();

		List<PoolItem> acquiredItems = new CopyOnWriteArrayList<>();
		List<Throwable> errorsInThread = new CopyOnWriteArrayList<>();
		Thread acquireA = new Thread(() -> {
			try {
				acquiredItems.add(pool.acquire(item -> item.supportedTypes.contains(PoolItemType.TYPE_A)));
			} catch (InterruptedException t) {
				errorsInThread.add(t);
			}
		});
		Thread acquireB = new Thread(() -> {
			try {
				acquiredItems.add(pool.acquire(item -> item.supportedTypes.contains(PoolItemType.TYPE_B)));
			} catch (InterruptedException t) {
				errorsInThread.add(t);
			}
		});
		Thread acquireC = new Thread(() -> {
			try {
				acquiredItems.add(pool.acquire(item -> item.supportedTypes.contains(PoolItemType.TYPE_C)));
			} catch (InterruptedException t) {
				errorsInThread.add(t);
			}
		});

		PoolItem acquiredA = pool.acquire(item -> item.supportedTypes.contains(PoolItemType.TYPE_A));
		PoolItem acquiredB = pool.acquire(item -> item.supportedTypes.contains(PoolItemType.TYPE_B));
		PoolItem acquiredC = pool.acquire(item -> item.supportedTypes.contains(PoolItemType.TYPE_C));

		acquireA.start();
		acquireB.start();
		acquireC.start();

		assertThreadState(State.WAITING, acquireA);
		assertThreadState(State.WAITING, acquireB);
		assertThreadState(State.WAITING, acquireC);

		pool.release(acquiredB);

		assertThreadState(State.WAITING, acquireA);
		assertThreadState(State.TERMINATED, acquireB);
		assertThreadState(State.WAITING, acquireC);

		pool.release(acquiredA);

		assertThreadState(State.TERMINATED, acquireA);
		assertThreadState(State.TERMINATED, acquireB);
		assertThreadState(State.WAITING, acquireC);

		pool.release(acquiredC);

		assertThreadState(State.TERMINATED, acquireA);
		assertThreadState(State.TERMINATED, acquireB);
		assertThreadState(State.TERMINATED, acquireC);
	}

	@Test
	public void releaseThenAcquireWithCondition_returnsOnlyPossibleResourceFirst() throws InterruptedException {
		PoolItem itemA = new PoolItem(PoolItemType.TYPE_A);
		PoolItem itemAC = new PoolItem(PoolItemType.TYPE_C, PoolItemType.TYPE_A);

		List<PoolItem> acquiredItems = new CopyOnWriteArrayList<>();
		List<Throwable> errorsInThread = new CopyOnWriteArrayList<>();
		Thread acquireA = new Thread(() -> {
			try {
				acquiredItems.add(pool.acquire(item -> item.supportedTypes.contains(PoolItemType.TYPE_A)));
			} catch (InterruptedException t) {
				errorsInThread.add(t);
			}
		});

		Thread acquireC = new Thread(() -> {
			try {
				acquiredItems.add(pool.acquire(item -> item.supportedTypes.contains(PoolItemType.TYPE_C)));
			} catch (InterruptedException t) {
				errorsInThread.add(t);
			}
		});

		assertEquals(0, pool.size());
		
		pool.add(itemA);
		pool.add(itemAC);

		pool.start();

		pool.acquire();
		pool.acquire();

		acquireA.start();
		acquireC.start();

		assertThreadState(State.WAITING, acquireA);
		assertThreadState(State.WAITING, acquireC);

		pool.release(itemA);

		assertThreadState(State.TERMINATED, acquireA);
		assertThreadState(State.WAITING, acquireC);
		assertEquals(1, acquiredItems.size());
		assertEquals(itemA, acquiredItems.get(0));
	}

	@Test
	public void releaseThenAcquireWithCondition_conditionPrioritizedOverNoCondition() throws InterruptedException {
		PoolItem itemA = new PoolItem(PoolItemType.TYPE_A);
		PoolItem itemAC = new PoolItem(PoolItemType.TYPE_A, PoolItemType.TYPE_C);

		List<PoolItem> acquiredItems = new CopyOnWriteArrayList<>();
		List<Throwable> errorsInThread = new CopyOnWriteArrayList<>();
		Thread acquireA = new Thread(() -> {
			try {
				acquiredItems.add(pool.acquire(item -> item.supportedTypes.contains(PoolItemType.TYPE_A)));
			} catch (InterruptedException t) {
				errorsInThread.add(t);
			}
		});

		Thread acquireC = new Thread(() -> {
			try {
				acquiredItems.add(pool.acquire(item -> item.supportedTypes.contains(PoolItemType.TYPE_C)));
			} catch (InterruptedException t) {
				errorsInThread.add(t);
			}
		});

		pool.add(itemA);
		pool.add(itemAC);

		pool.start();

		pool.acquire();
		pool.acquire();

		acquireA.start();
		acquireC.start();

		assertThreadState(State.WAITING, acquireA);
		assertThreadState(State.WAITING, acquireC);

		pool.release(itemA);

		assertThreadState(State.TERMINATED, acquireA);
		assertThreadState(State.WAITING, acquireC);
		assertEquals(1, acquiredItems.size());
		assertEquals(itemA, acquiredItems.get(0));
	}

	//@Test
	public void durationTest_Threaded() throws InterruptedException {
		for (int i = 0; i < 300; i++) {
			System.err.println("Iteration " + i);
			createPool();
			acquireCombinations_itemOrderPrioritized();
		}
	}

	@Test
	public void acquireCombinations_itemOrderPrioritized() throws InterruptedException {
		PoolItem itemA = new PoolItem(PoolItemType.TYPE_A);
		PoolItem itemAC = new PoolItem(PoolItemType.TYPE_A, PoolItemType.TYPE_C);
		PoolItem itemB = new PoolItem(PoolItemType.TYPE_B);
		
		final CountDownLatch latch = new CountDownLatch(3);
		
		List<PoolItem> acquiredItems = new CopyOnWriteArrayList<>();
		List<Throwable> errorsInThread = new CopyOnWriteArrayList<>();
		Thread acquireA1 = new Thread(() -> {
			try {
				latch.countDown();
				acquiredItems.add(pool.acquire(item -> item.supportedTypes.contains(PoolItemType.TYPE_A)));
			} catch (InterruptedException t) {
				errorsInThread.add(t);
			}
		});
		Thread acquireA2 = new Thread(() -> {
			try {
				latch.countDown();
				acquiredItems.add(pool.acquire(item -> item.supportedTypes.contains(PoolItemType.TYPE_C)));
			} catch (InterruptedException t) {
				errorsInThread.add(t);
			}
		});

		Thread acquireC = new Thread(() -> {
			try {
				latch.countDown();
				acquiredItems.add(pool.acquire());
			} catch (InterruptedException t) {
				errorsInThread.add(t);
			}
		});

		pool.add(itemA);
		pool.add(itemAC);
		pool.add(itemB);

		pool.start();

		pool.acquire();
		pool.acquire();
		pool.acquire();

		acquireA1.start();
		acquireA2.start();
		acquireC.start();
		
		// improve chances that all acquires from the three threads have been done
		Thread.yield();
		Thread.sleep(100);
		latch.await();
		
		assertThreadState(State.WAITING, acquireA1);
		assertThreadState(State.WAITING, acquireA2);
		assertThreadState(State.WAITING, acquireC);
		
		pool.release(itemA);

		assertThreadState(State.TERMINATED, acquireA1);
		assertThreadState(State.WAITING, acquireA2);
		assertThreadState(State.WAITING, acquireC);
		assertEquals(1, acquiredItems.size());
		assertEquals(itemA, acquiredItems.get(0));

		pool.release(itemAC);
		
		assertThreadState(State.TERMINATED, acquireA1);
		assertThreadState(State.TERMINATED, acquireA2);
		assertThreadState(State.WAITING, acquireC);
		assertEquals(2, acquiredItems.size());
		assertEquals(itemAC, acquiredItems.get(1));
		
		pool.release(itemB);
		
		assertThreadState(State.TERMINATED, acquireA1);
		assertThreadState(State.TERMINATED, acquireA2);
		assertThreadState(State.TERMINATED, acquireC);
		assertEquals(3, acquiredItems.size());
		assertEquals(itemB, acquiredItems.get(2));		
	}
	
	@Test
	public void duration_releaseTwice_ignored() throws InterruptedException {
		for (int i = 0; i < 100; ++i) {
			System.err.println("===============");
			pool = new Pool<>();
			releaseTwice_ignored();
		}
	}
	
	@Test
	public void releaseTwice_ignored() throws InterruptedException {

		List<PoolItem> acquiredItems = new CopyOnWriteArrayList<>();
		List<Throwable> errorsInThread = new CopyOnWriteArrayList<>();
		Thread acquire = new Thread(() -> {
			try {
				acquiredItems.add(pool.acquire());
			} catch (InterruptedException t) {
				errorsInThread.add(t);
			}
		});
		
		pool.add(new PoolItem());
		pool.start();
		PoolItem item = pool.acquire();
		
		pool.release(item);
		pool.release(item);
		
		assertEquals(1, pool.size());
		pool.acquire();
		acquire.start(); 
		assertThreadState(State.WAITING, acquire);
	}

	
	/**
	 * Waits max 30 seconds until provided thread has reached the desired state. Assertion fails after timeout.
	 * 
	 * @param expectedState
	 *            The state to wait for.
	 * @param thread
	 *            The thread to check the state of.
	 * @throws InterruptedException
	 *             Thrown when calling thread has been interrupted.
	 */
	private static void assertThreadState(Thread.State expectedState, Thread thread) throws InterruptedException {
		long waitCount = 10;
		TimeUnit waitUnits = TimeUnit.SECONDS;

		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < waitUnits.toMillis(waitCount)) {
			if (thread.getState() == expectedState) {
				return;
			}
			Thread.sleep(100); // Don't hog the CPU
		}
		assertEquals("Timed out while waiting for thread state", expectedState, thread.getState());
	}

	
	
	
	@Test
	public void LinkedBlockingQueue_addTake() throws InterruptedException {
		LinkedBlockingQueue<String> q = new LinkedBlockingQueue<>();
		q.add("hi");
		assertEquals("hi", q.take());
	}
	
	@Test
	public void LinkedBlockingQueue_takeAdd() throws InterruptedException {
		final LinkedBlockingQueue<String> q = new LinkedBlockingQueue<>();
		
		final CountDownLatch readySetGo = new CountDownLatch(1);
		final List<Throwable> errors = new ArrayList<>();
		Thread addThread = new Thread(() -> {
			try {
				Thread.sleep(200);
				q.add("hi");	
			} catch (Throwable e) {
				errors.add(e);
			}
			
		});
		readySetGo.countDown();
		addThread.start();
		q.take();
		assertEquals(0, errors.size());
		
	}
}
