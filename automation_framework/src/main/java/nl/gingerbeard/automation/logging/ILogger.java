package nl.gingerbeard.automation.logging;

public interface ILogger {

	default void info(final String message) {
		log(LogLevel.INFO, message);
	}

	default void debug(final String message) {
		log(LogLevel.DEBUG, message);
	}

	default void error(final String message) {
		log(LogLevel.ERROR, message);
	}

	default void warning(final String message) {
		log(LogLevel.WARNING, message);
	}

	void log(LogLevel level, String message);
}
