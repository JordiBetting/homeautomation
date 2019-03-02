package nl.gingerbeard.automation.logging;

import java.util.Optional;

public interface ILogger {

	default void info(final String message) {
		log(Optional.empty(), LogLevel.INFO, message);
	}

	default void debug(final String message) {
		log(Optional.empty(), LogLevel.DEBUG, message);
	}

	default void error(final String message) {
		log(Optional.empty(), LogLevel.ERROR, message);
	}

	default void warning(final String message) {
		log(Optional.empty(), LogLevel.WARNING, message);
	}

	void log(Optional<Throwable> t, LogLevel level, String message);

	default void exception(final Throwable t, final String message) {
		log(Optional.of(t), LogLevel.EXCEPTION, message);
	}

	default void warning(final Throwable t, final String message) {
		log(Optional.of(t), LogLevel.WARNING, message);
	}
}
