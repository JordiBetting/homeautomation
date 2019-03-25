package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;

public class ThermostatModeDeviceTest {

	@Test
	public void updateState_setpoint() {
		final ThermostatModeDevice device = new ThermostatModeDevice(1);

		final boolean result = device.updateState("setpoint");

		assertTrue(result);
		assertEquals(ThermostatMode.SETPOINT, device.getState());
	}

	@Test
	public void updateState_fullheat() {
		final ThermostatModeDevice device = new ThermostatModeDevice(1);

		final boolean result = device.updateState("full_heat");

		assertTrue(result);
		assertEquals(ThermostatMode.FULL_HEAT, device.getState());
	}

	@Test
	public void updateState_off() {
		final ThermostatModeDevice device = new ThermostatModeDevice(1);

		final boolean result = device.updateState("off");

		assertTrue(result);
		assertEquals(ThermostatMode.OFF, device.getState());
	}

	@Test
	public void updateState_invalid() {
		final ThermostatModeDevice device = new ThermostatModeDevice(1);

		final boolean result = device.updateState("invalid");

		assertFalse(result);
	}

}
