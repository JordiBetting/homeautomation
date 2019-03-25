package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LevelDeviceTest {

	private static class MyLevelDevice extends LevelDevice {

		public MyLevelDevice() {
			super(1);
		}

	}

	@Test
	public void updateState_validInput() {
		final MyLevelDevice device = new MyLevelDevice();

		final boolean result = device.updateState("1");

		assertTrue(result);
		assertEquals(1, device.getState().getLevel());
	}

	@Test
	public void updateState_negativeInput_throwsException() {
		final MyLevelDevice device = new MyLevelDevice();
		assertThrows(IllegalArgumentException.class, () -> device.updateState("-1"));
	}

	@Test
	public void updateState_invalidInput() {
		final MyLevelDevice device = new MyLevelDevice();

		final boolean result = device.updateState("#invalid#");

		assertFalse(result);
	}
}
