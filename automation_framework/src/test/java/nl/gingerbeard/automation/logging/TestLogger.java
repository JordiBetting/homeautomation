package nl.gingerbeard.automation.logging;

import java.util.Optional;

public class TestLogger implements ILogger {

	@Override
	public void log(final Optional<Throwable> t, final LogLevel level, final String message) {
		System.out.println("[" + level.name() + "] " + message);
		if (t.isPresent()) {
			t.get().printStackTrace();
		}
	}

}
