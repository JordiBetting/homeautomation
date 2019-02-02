package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.state.Temperature.Unit;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;

public class ThermostatTest {

	@Test
	public void updateMode_parentUpdated() {
		final Thermostat thermostat = new Thermostat(1, 2);

		thermostat.getModeDevice().updateState("SETPOINT");

		assertEquals(ThermostatMode.SETPOINT, thermostat.getState().getMode());
	}

	@Test
	public void updateSetpoint_parentUpdated() {
		final Thermostat thermostat = new Thermostat(1, 2);

		thermostat.getSetpointDevice().updateState("-14");

		assertTrue(thermostat.getState().getSetPoint().isPresent());
		assertEquals(-14, thermostat.getState().getSetPoint().get().get(Unit.CELSIUS));
	}

	@Test
	public void thermostatSubdevices_parentSet() {
		final Thermostat thermostat = new Thermostat(1, 2);

		assertTrue(thermostat.getModeDevice().getParent().isPresent());
		assertEquals(thermostat, thermostat.getModeDevice().getParent().get());

		assertTrue(thermostat.getSetpointDevice().getParent().isPresent());
		assertEquals(thermostat, thermostat.getSetpointDevice().getParent().get());
	}
}
