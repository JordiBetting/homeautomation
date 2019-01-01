package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.state.OnOffState;

public class OnOffDeviceTest {

	private static class MyOnOffDevice extends OnOffDevice {
		public MyOnOffDevice() {
			super(32);
		}
	}

	@Test
	public void updateState_on() {
		final OnOffDevice device = new MyOnOffDevice();

		final boolean result = device.updateState("on");

		assertTrue(result);
		assertEquals(OnOffState.ON, device.getState());
	}

	@Test
	public void updateState_off() {
		final OnOffDevice device = new MyOnOffDevice();

		final boolean result = device.updateState("off");

		assertTrue(result);
		assertEquals(OnOffState.OFF, device.getState());
	}

	@Test
	public void updateState_invalid() {
		final OnOffDevice device = new MyOnOffDevice();

		final boolean result = device.updateState("invalid");

		assertFalse(result);
	}
}
