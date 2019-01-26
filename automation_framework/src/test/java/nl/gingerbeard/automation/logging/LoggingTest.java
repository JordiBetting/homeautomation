package nl.gingerbeard.automation.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class LoggingTest {

	@Test
	public void activate_createsLoggingInterface() {
		final LoggingComponent component = new LoggingComponent();
		component.createComponent();

		assertNotNull(component.logInput);
	}

	private static class LogRecorder implements ILogOutput {

		private final List<String> logs = new ArrayList<>();

		@Override
		public void log(final LogLevel level, final String message) {
			logs.add(level.name() + " " + message);
		}

	}

	@Test
	public void log_wired() {
		final LogRecorder logOutput = new LogRecorder();
		final LoggingComponent component = new LoggingComponent();
		component.logOutput = Optional.of(logOutput);
		component.createComponent();

		component.logInput.debug("testdebug");
		component.logInput.info("testinfo");
		component.logInput.error("testerror");
		component.logInput.warning("testwarning");

		assertEquals(4, logOutput.logs.size());
		assertEquals("DEBUG testdebug", logOutput.logs.get(0));
		assertEquals("INFO testinfo", logOutput.logs.get(1));
		assertEquals("ERROR testerror", logOutput.logs.get(2));
		assertEquals("WARNING testwarning", logOutput.logs.get(3));
	}

	@Test
	public void log_noOutputProvided_stdOut() {

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos);
		final PrintStream old_sysout = System.out;
		System.setOut(ps);

		try {
			final LoggingComponent component = new LoggingComponent();
			component.createComponent();

			component.logInput.debug("testdebug");
			component.logInput.info("testinfo");
			component.logInput.error("testerror");
			component.logInput.warning("testwarning");
			System.out.flush();
			final String output = baos.toString();
			assertEquals("[DEBUG] testdebug" + System.lineSeparator() + //
					"[INFO] testinfo" + System.lineSeparator() + //
					"[ERROR] testerror" + System.lineSeparator() + //
					"[WARNING] testwarning" + System.lineSeparator(), //

					output);

		} finally {
			System.setOut(old_sysout);
		}
	}

	@Test
	public void logException() {
		final LogRecorder logOutput = new LogRecorder();
		final LoggingComponent component = new LoggingComponent();
		component.logOutput = Optional.of(logOutput);
		component.createComponent();

		component.logInput.exception(new NullPointerException("blaat"), "exception message");

		assertEquals(2, logOutput.logs.size());
		assertEquals("EXCEPTION exception message", logOutput.logs.get(0));
		assertEquals("EXCEPTION blaat", logOutput.logs.get(1));
	}
}
