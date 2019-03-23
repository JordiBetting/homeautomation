package nl.gingerbeard.automation.autocontrol.timer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.autocontrol.timer.AutoControlTimer;

public class AutoControlTimerTest {

	@Test
	public void schedule_isExecuted() throws InterruptedException {
		final AutoControlTimer timer = new AutoControlTimer();

		final AtomicLong endTime = new AtomicLong();
		final long startTime = System.currentTimeMillis();

		final CountDownLatch doneLatch = new CountDownLatch(1);

		timer.executeDelayed(() -> {
			endTime.set(System.currentTimeMillis());
			doneLatch.countDown();
		}, 1000);

		assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
		assertTrue(endTime.get() - startTime >= 1000);
	}

	@Test
	public void scheduleTwice_secondExecuted() throws InterruptedException {
		final AutoControlTimer timer = new AutoControlTimer();

		final AtomicLong endTime = new AtomicLong();
		final AtomicInteger executedTask = new AtomicInteger(0);
		final long startTime = System.currentTimeMillis();

		final CountDownLatch doneLatch = new CountDownLatch(1);

		timer.executeDelayed(() -> {
			endTime.set(System.currentTimeMillis());
			executedTask.set(1);
			doneLatch.countDown();
		}, 500);
		timer.executeDelayed(() -> {
			endTime.set(System.currentTimeMillis());
			executedTask.set(2);
			doneLatch.countDown();
		}, 1000);

		assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
		assertEquals(2, executedTask.get());
		assertTrue(endTime.get() - startTime >= 1000);
	}

	@Test
	public void noDelay_executedInstantly() {
		final AtomicBoolean run = new AtomicBoolean(false);

		final AutoControlTimer timer = new AutoControlTimer();
		timer.executeDelayed(() -> {
			run.set(true);
		}, 0);

		assertTrue(run.get());
	}

	@Test
	public void noDelay_sameThread() {
		final AtomicLong taskThreadId = new AtomicLong(0);
		final AutoControlTimer timer = new AutoControlTimer();
		timer.executeDelayed(() -> {
			taskThreadId.set(Thread.currentThread().getId());
		}, 0);

		assertEquals(Thread.currentThread().getId(), taskThreadId.get());
	}

	@Test
	public void delayed_differentThread() throws InterruptedException {
		final AutoControlTimer timer = new AutoControlTimer();

		final AtomicLong taskThreadId = new AtomicLong(0);
		final CountDownLatch doneLatch = new CountDownLatch(1);

		timer.executeDelayed(() -> {
			taskThreadId.set(Thread.currentThread().getId());
			doneLatch.countDown();
		}, 1);

		assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
		assertNotEquals(Thread.currentThread().getId(), taskThreadId.get());
	}

	@Test
	public void noDelay_cancelsDelayed() throws InterruptedException {
		final AutoControlTimer timer = new AutoControlTimer();

		final AtomicInteger executedTask = new AtomicInteger(0);
		final CountDownLatch doneLatch = new CountDownLatch(1);

		timer.executeDelayed(() -> {
			executedTask.set(1);
			doneLatch.countDown();
		}, 500);

		timer.executeDelayed(() -> {
			executedTask.set(2);
		}, 0);

		assertFalse(doneLatch.await(1, TimeUnit.SECONDS));
		assertEquals(2, executedTask.get());
	}

	@Test
	public void stop_cancelsPendingTask() throws InterruptedException {
		final AutoControlTimer timer = new AutoControlTimer();

		final CountDownLatch doneLatch = new CountDownLatch(1);

		timer.executeDelayed(() -> {
			doneLatch.countDown();
		}, 500);

		timer.stop();
		assertFalse(doneLatch.await(1, TimeUnit.SECONDS));
	}

	@Test
	public void executeAfterStop_throwsException() {
		final AutoControlTimer timer = new AutoControlTimer();
		timer.stop();
		assertThrows(IllegalStateException.class, () -> timer.executeDelayed(() -> {
		}, 0));
	}

	@Test
	public void cancelScheduledTask_taskNotExecuted() throws InterruptedException {
		final AutoControlTimer timer = new AutoControlTimer();

		final CountDownLatch doneLatch = new CountDownLatch(1);

		timer.executeDelayed(() -> {
			doneLatch.countDown();
		}, 500);

		timer.cancelTask();
		assertFalse(doneLatch.await(1, TimeUnit.SECONDS));
	}
}
