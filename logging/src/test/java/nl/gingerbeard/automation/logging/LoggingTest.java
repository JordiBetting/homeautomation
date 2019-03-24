package nl.gingerbeard.automation.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class LoggingTest {

	private static class LogRecorder implements ILogOutput {

		private final List<String> logs = new ArrayList<>();

		@Override
		public void log(final LogLevel level, final String context, final String message) {
			logs.add(level.name() + " [" + context + "] " + message);
		}

	}

	@Test
	public void log_wired() {
		final LogRecorder logOutput = new LogRecorder();
		final ILogger logging = new Logging(Optional.of(logOutput));

		logging.debug("testdebug");
		logging.info("testinfo");
		logging.error("testerror");
		logging.warning("testwarning");

		assertEquals(4, logOutput.logs.size());
		assertEquals("DEBUG [root] testdebug", logOutput.logs.get(0));
		assertEquals("INFO [root] testinfo", logOutput.logs.get(1));
		assertEquals("ERROR [root] testerror", logOutput.logs.get(2));
		assertEquals("WARNING [root] testwarning", logOutput.logs.get(3));
	}

	@Test
	public void log_noOutputProvided_stdOut() throws UnsupportedEncodingException {

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos, true, "UTF-8");
		final PrintStream old_sysout = System.out;
		System.setOut(ps);

		try {
			final ILogger logging = new Logging(Optional.empty());

			logging.debug("testdebug");
			logging.info("testinfo");
			logging.error("testerror");
			logging.warning("testwarning");

			System.out.flush();
			final String output = baos.toString("UTF-8");
			assertEquals("[DEBUG] [root] testdebug" + System.lineSeparator() + //
					"[INFO] [root] testinfo" + System.lineSeparator() + //
					"[ERROR] [root] testerror" + System.lineSeparator() + //
					"[WARNING] [root] testwarning" + System.lineSeparator(), //
					output);

		} finally {
			System.setOut(old_sysout);
		}
	}

	@Test
	public void logException() {
		final LogRecorder logOutput = new LogRecorder();
		final ILogger logging = new Logging(Optional.of(logOutput));

		logging.exception(new NullPointerException("blaat"), "exception message");

		assertEquals(3, logOutput.logs.size());
		assertEquals("EXCEPTION [root] exception message", logOutput.logs.get(0));
		assertEquals("EXCEPTION [root] blaat", logOutput.logs.get(1));
		assertTrue(logOutput.logs.get(2).contains("at " + this.getClass().getName() + ".logException(" + this.getClass().getSimpleName() + ".java:"));
	}

	@Test
	public void logException_asWarning() {
		final LogRecorder logOutput = new LogRecorder();
		final ILogger logging = new Logging(Optional.of(logOutput));

		logging.warning(new NullPointerException("blaat"), "exception message");

		assertEquals(3, logOutput.logs.size());
		assertEquals("WARNING [root] exception message", logOutput.logs.get(0));
		assertEquals("WARNING [root] blaat", logOutput.logs.get(1));
		assertTrue(logOutput.logs.get(2).contains("at " + this.getClass().getName() + ".logException_asWarning(" + this.getClass().getSimpleName() + ".java:"));
	}

	@Test
	public void logContext_defaultRoot() {
		final LogRecorder logOutput = new LogRecorder();
		final ILogger logging = new Logging(Optional.of(logOutput));

		logging.info("test");
		assertEquals(1, logOutput.logs.size());
		assertEquals("INFO [root] test", logOutput.logs.get(0));
	}

	@Test
	public void logContext_switched() {
		final LogRecorder logOutput = new LogRecorder();
		final ILogger logging = new Logging(Optional.of(logOutput));

		logging.warning("message1");
		final ILogger newLogger = logging.createContext("test");
		newLogger.warning("message2");
		assertEquals(2, logOutput.logs.size());
		assertEquals("WARNING [root] message1", logOutput.logs.get(0));
		assertEquals("WARNING [test] message2", logOutput.logs.get(1));
	}
}
