package nl.gingerbeard.automation.state;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.StringTestDevice;

public class NextStateTest {

	@Test
	public void init_noException() {
		new NextState<>(new StringTestDevice(), "test");
	}

	@Test
	public void init_nullDevice_throwsException() {
		try {
			new NextState<>(null, "test");
			fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void init_nullState_throwsException() {
		try {
			new NextState<>(new StringTestDevice(), null);
			fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// expected
		}
	}

}
