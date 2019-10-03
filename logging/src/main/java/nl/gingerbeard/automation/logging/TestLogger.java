package nl.gingerbeard.automation.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestLogger implements ILogger {

	public static class LogOutputToTestLogger implements ILogOutput {

		public static final TestLogger testLogger = new TestLogger();

		@Override
		public void log(final LogLevel level, final String context, final String message) {
			testLogger.log(Optional.empty(), level, "[" + context + "] " + message);
		}

	}

	List<String> log;
	private final String context;

	public TestLogger() {
		this("root", null);
	}

	private TestLogger(final String context, final TestLogger parent) {
		this.context = context;
		if (parent != null) {
			log = parent.log;
		} else {
			log = new ArrayList<>();
		}
	}

	@Override
	public void log(final Optional<Throwable> t, final LogLevel level, final String message) {
		log.add(format(level, message));
		if (t.isPresent()) {
			final StringWriter sw = new StringWriter();
			t.get().printStackTrace(new PrintWriter(sw));
			log.add(sw.toString());
		}
	}

	private String format(final LogLevel level, final String message) {
		return "[" + level.name() + "] [" + context + "] " + message;
	}

	public void printAll() {
		log.stream().forEach(s -> System.out.println(s));
	}

	String getFullLog() {
		final StringBuilder fullLog = new StringBuilder();
		log.stream().forEach(s -> fullLog.append(s).append(System.lineSeparator()));
		return fullLog.toString();
	}

	public void assertContains(final LogLevel level, final String message) {
		if (message.startsWith("[")) {
			assertContains(message);
		} else {
			assertContains(format(level, message));
		}
	}

	private static class TestLoggerException extends RuntimeException {
		private static final long serialVersionUID = 3019916957991833314L;

		public TestLoggerException(final String message) {
			super(message);
		}

	}

	private void assertContains(final String expectation) {
		if (testContains(expectation) == false) {
			throw new TestLoggerException("Logmessage not present: " + expectation + System.lineSeparator() + "Full log:" + System.lineSeparator() + getFullLog());
		}
	}

	private boolean testContains(final String expectation) {
		if (log.contains(expectation)) {
			return true;
		}
		for (final String logline : log) {
			if (logline.contains(expectation)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ILogger createContext(final String context) {
		return new TestLogger(context, this);
	}

	public void assertEmpty() {
		if (!log.isEmpty()) {
			throw new TestLoggerException("Log was expected to be empty, but contains: " + System.lineSeparator() + getFullLog());
		}
	}
}
