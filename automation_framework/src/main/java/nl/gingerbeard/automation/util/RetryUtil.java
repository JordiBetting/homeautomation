package nl.gingerbeard.automation.util;

import java.time.Duration;
import java.util.Optional;

import com.google.common.base.Preconditions;

public final class RetryUtil {

	private RetryUtil() {
		// avoid instantiation
	}
	
	public static interface RetryTask {
		void execute() throws Exception;
	}
	
	public static Optional<Throwable> retry(RetryTask task, int amountOfTries, Duration interval) throws InterruptedException {
		Preconditions.checkArgument(amountOfTries >= 0, "Cannot try to execute a negative amount of times.");
		
		Optional<Throwable> result = Optional.empty();
		
		boolean infinite = amountOfTries <= 0;
		while (amountOfTries > 0 || infinite) {
			try {
				task.execute();
				result = Optional.empty();
				break;
			} catch (InterruptedException e) {
				throw e;
			} catch (Throwable t) {
				amountOfTries--;
				
				long interval_ms = interval.toMillis();
				if (interval_ms > 0) Thread.sleep(interval_ms);
				
				result = Optional.of(t);
			}
		}
		return result;
	}

}
