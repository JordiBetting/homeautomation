package nl.gingerbeard.automation.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
		public void log(final LogLevel level, final String message) {
			logs.add(level.name() + " " + message);
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
		assertEquals("DEBUG testdebug", logOutput.logs.get(0));
		assertEquals("INFO testinfo", logOutput.logs.get(1));
		assertEquals("ERROR testerror", logOutput.logs.get(2));
		assertEquals("WARNING testwarning", logOutput.logs.get(3));
	}

	@Test
	public void log_noOutputProvided_stdOut() throws UnsupportedEncodingException {

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos, true, "UTF-8");
		final PrintStream old_sysout = System.out;
		System.setOut(ps);

		try {
			final LogRecorder logOutput = new LogRecorder();
			final ILogger logging = new Logging(Optional.empty());

			logging.debug("testdebug");
			logging.info("testinfo");
			logging.error("testerror");
			logging.warning("testwarning");

			System.out.flush();
			final String output = baos.toString("UTF-8");
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
		final ILogger logging = new Logging(Optional.of(logOutput));

		logging.exception(new NullPointerException("blaat"), "exception message");

		assertEquals(2, logOutput.logs.size());
		assertEquals("EXCEPTION exception message", logOutput.logs.get(0));
		assertEquals("EXCEPTION blaat", logOutput.logs.get(1));
	}
}
