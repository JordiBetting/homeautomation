package nl.gingerbeard.automation;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class LoggingComponentTest {
	@Test
	public void activate_createsLoggingInterface() {
		final LoggingComponent component = new LoggingComponent();
		component.createComponent();

		assertNotNull(component.logInput);
	}
}
