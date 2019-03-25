package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.state.Temperature.Unit;

public class TemperatureSensorTest {

	@Test
	public void thermostatSetPoint_updateWorks() {
		final TemperatureSensor device = new TemperatureSensor(1);

		final boolean result = device.updateState("1");

		assertTrue(result);
		assertEquals(1, device.getState().get(Unit.CELSIUS));
	}

	@Test
	public void thermostatSetpoint_updateInvalid_returnsFalse() {
		final TemperatureSensor device = new TemperatureSensor(1);

		final boolean result = device.updateState("invalid");

		assertFalse(result);
	}
}
