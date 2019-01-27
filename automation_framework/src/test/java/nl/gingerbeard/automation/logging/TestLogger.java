package nl.gingerbeard.automation.logging;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestLogger implements ILogger {

	List<String> log = new ArrayList<>();

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
		return "[" + level.name() + "] " + message;
	}

	public void printAll() {
		log.stream().forEach(s -> System.out.println(s));
	}

	public void assertContains(final LogLevel level, final String message) {
		assertContains(format(level, message));
	}

	private void assertContains(final String expectation) {
		assertTrue(testContains(expectation), "Logmessage not present: " + expectation);
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
}
