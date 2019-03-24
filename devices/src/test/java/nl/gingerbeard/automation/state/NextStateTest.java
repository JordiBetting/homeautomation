package nl.gingerbeard.automation.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import nl.gingerbeard.automation.devices.Device;

public class NextStateTest {

	@Test
	public void init_noException() {
		new NextState<>(createMockDevice(), "test");
	}

	@Test
	public void nextState_returnsComponents() {
		final Device<String> mock = createMockDevice();

		final NextState<String> nextState = new NextState<>(mock, "test");

		assertEquals("test", nextState.get());
		assertEquals(mock, nextState.getDevice());
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
			new NextState<>(createMockDevice(), null);
			fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// expected
		}
	}

	private Device<String> createMockDevice() {
		@SuppressWarnings("unchecked")
		final Device<String> mock = Mockito.mock(Device.class);
		return mock;
	}

	@Test
	public void getTrigger_returnsCaller() {
		final NextState<String> sut = new NextState<>(createMockDevice(), "");
		assertEquals(this.getClass().getSimpleName(), sut.getTrigger());
	}

	@Test
	public void overrideTrigger_returnsOverridden() {
		final NextState<String> sut = new NextState<>(createMockDevice(), "");
		sut.setTrigger("blaat");
		assertEquals("blaat", sut.getTrigger());
	}
}
