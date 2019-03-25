package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PowerUsageSensorTest {

	@Test
	public void updateState_validInput() {
		final PowerUsageSensor device = new PowerUsageSensor(1);

		final boolean result = device.updateState("1");

		assertTrue(result);
		assertEquals(1, device.getState().getUsageWatt());
	}

	@Test
	public void updateState_negativeInput_throwsException() {
		final PowerUsageSensor device = new PowerUsageSensor(1);
		assertThrows(IllegalArgumentException.class, () -> device.updateState("-1"));
	}

	@Test
	public void updateState_invalidInput() {
		final PowerUsageSensor device = new PowerUsageSensor(1);

		final boolean result = device.updateState("#invalid#");

		assertFalse(result);
	}
}
