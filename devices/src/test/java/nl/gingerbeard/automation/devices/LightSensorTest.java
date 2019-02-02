package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LightSensorTest {

	@Test
	public void updateState_validInput() {
		final LightSensor device = new LightSensor(0);

		final boolean result = device.updateState("1");

		assertTrue(result);
		assertEquals(1, device.getState().getLux());
	}

	@Test
	public void updateState_negativeInput_throwsException() {
		final LightSensor device = new LightSensor(0);
		assertThrows(IllegalArgumentException.class, () -> device.updateState("-1"));
	}

	@Test
	public void updateState_invalidInput() {
		final LightSensor device = new LightSensor(0);

		final boolean result = device.updateState("#invalid#");

		assertFalse(result);
	}
}
