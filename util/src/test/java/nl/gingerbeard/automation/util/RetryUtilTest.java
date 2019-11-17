package nl.gingerbeard.automation.util;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.util.RetryUtil.RetryTask;

public class RetryUtilTest {

	private static class EmptyTask implements RetryTask {
		@Override
		public void execute() {
		}
	}

	private static class SleepyTask implements RetryTask {
		@Override
		public void execute() throws InterruptedException {
			Thread.sleep(1000 * 60);
		}
	}

	private static class CustomException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		CustomException(String message) {
			super(message);
		}
	}
	
	private static class FailThousandTimesTask implements RetryTask {

		int counter = 0 ;
		@Override
		public void execute() throws Exception {
			if (++counter < 1000) {
				throw new CustomException(Integer.toString(counter));
			}
		}
		
	}

	private static class FailAlwaysTask implements RetryTask {
		@Override
		public void execute() {
			throw new CustomException("I fail always");
		}
	}

	private static class OnceFailTask implements RetryTask {

		private boolean firstCall = true;

		@Override
		public void execute() {
			if (firstCall) {
				firstCall = false;
				throw new NullPointerException("TestException for retry");
			}
		}

	}

	@Test
	public void success_executedOnce() throws Exception {
		RetryTask spy = spy(new EmptyTask());

		Optional<Throwable> result = RetryUtil.retry(spy, 5, Duration.ofSeconds(1));

		assertFalse(result.isPresent());
		verify(spy, times(1)).execute();
	}

	@Test
	public void failOnce_executedTwice() throws Exception {
		RetryTask spy = spy(new OnceFailTask());

		Optional<Throwable> result = RetryUtil.retry(spy, 5, Duration.ofMillis(1));
		assertFalse(result.isPresent());

		verify(spy, times(2)).execute();
	}

	@Test
	public void failAlways_executedMaxTimes_exceptionThrown() throws Exception {
		RetryTask spy = spy(new FailAlwaysTask());

		Optional<Throwable> result = RetryUtil.retry(spy, 5, Duration.ofMillis(1));

		verify(spy, times(5)).execute();
		assertTrue(result.isPresent());
		assertEquals("I fail always", result.get().getMessage());
	}

	@Test
	public void fail_delayApplied() throws InterruptedException {
		RetryTask spy = spy(new FailAlwaysTask());
		int iterations = 5;
		long interval_ms = 250;

		long start = System.nanoTime();

		Optional<Throwable> result = RetryUtil.retry(spy, iterations, Duration.ofMillis(interval_ms));

		long duration_ns = System.nanoTime() - start;
		long duration_ms = duration_ns / (long) 1e6;

		assertTrue(result.isPresent());
		assertTrue(duration_ms > (0.8 * (iterations * interval_ms)));
		// 0.8 to compensate for variability in Thread.sleep
	}
	
	@Test
	public void zeroDelay_noException() throws Exception {
		RetryTask spy = spy(new OnceFailTask());

		Optional<Throwable> result = RetryUtil.retry(spy, 5, Duration.ofSeconds(0));

		assertFalse(result.isPresent());
		verify(spy, times(2)).execute();
	}
	
	@Test
	public void negativeDelay_noException() throws Exception {
		RetryTask spy = spy(new OnceFailTask());

		Optional<Throwable> result = RetryUtil.retry(spy, 5, Duration.ofSeconds(-5));

		assertFalse(result.isPresent());
		verify(spy, times(2)).execute();
	}
	
	@Test
	public void interrupt_throwsInterruptedException() {
		RetryTask sleepy = new SleepyTask();

		Thread.currentThread().interrupt();
		assertThrows(InterruptedException.class, () -> RetryUtil.retry(sleepy, 5, Duration.ofSeconds(0)));
	}
	
	@Test
	public void zeroRetry_infiniteAttempts() throws InterruptedException {
		FailThousandTimesTask task = new FailThousandTimesTask();
		
		RetryUtil.retry(task, 0, Duration.ofSeconds(0));
		
		assertEquals(1000, task.counter);
	}
	
	@Test
	public void negativeTries_throwsException() throws InterruptedException {
		RetryTask task = new EmptyTask();
		
		assertThrows(IllegalArgumentException.class, () -> RetryUtil.retry(task, -1, Duration.ofSeconds(0)));
	}
}
