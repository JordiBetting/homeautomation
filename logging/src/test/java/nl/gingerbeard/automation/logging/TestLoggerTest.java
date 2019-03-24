package nl.gingerbeard.automation.logging;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class TestLoggerTest {

	@Test
	public void doTest() {
		final TestLogger logger = new TestLogger();
		logger.debug("test");
		logger.assertContains(LogLevel.DEBUG, "test");

		assertThrows(RuntimeException.class, () -> logger.assertContains(LogLevel.INFO, "blaat"));

		logger.exception(new NullPointerException(), "blaat");
		logger.printAll();
		logger.assertContains(LogLevel.DEBUG, "t");
		logger.createContext("t");
	}
}
