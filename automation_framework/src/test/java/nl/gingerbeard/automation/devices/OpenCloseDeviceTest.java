package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.state.OpenCloseState;

public class OpenCloseDeviceTest {

	private static class MyOpenCloseDevice extends OpenCloseDevice {

		public MyOpenCloseDevice() {
			super(0);
		}

	}

	@Test
	public void updateState_open() {
		final OpenCloseDevice device = new MyOpenCloseDevice();

		device.updateState("open");

		assertEquals(OpenCloseState.OPEN, device.getState());
	}

	@Test
	public void updateState_closed() {
		final OpenCloseDevice device = new MyOpenCloseDevice();

		device.updateState("closed");

		assertEquals(OpenCloseState.CLOSE, device.getState());
	}

	@Test
	public void updateState_close() {
		final OpenCloseDevice device = new MyOpenCloseDevice();

		device.updateState("close");

		assertEquals(OpenCloseState.CLOSE, device.getState());
	}

	@Test
	public void updateState_invalid_returnsFalse() {
		final OpenCloseDevice device = new MyOpenCloseDevice();

		final boolean result = device.updateState("invalid");

		assertFalse(result);
	}
}
