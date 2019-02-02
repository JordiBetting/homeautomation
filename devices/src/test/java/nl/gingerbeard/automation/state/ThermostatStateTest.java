package nl.gingerbeard.automation.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.state.Temperature.Unit;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;

public class ThermostatStateTest {

	@Test
	public void fullheat() {
		final ThermostatState state = new ThermostatState();

		state.setFullHeat();

		assertEquals(ThermostatMode.FULL_HEAT, state.getMode());
		assertFalse(state.getSetPoint().isPresent());
	}

	@Test
	public void off() {
		final ThermostatState state = new ThermostatState();

		state.setOff();

		assertEquals(ThermostatMode.OFF, state.getMode());
		assertFalse(state.getSetPoint().isPresent());
	}

	@Test
	public void setPoint() {
		final ThermostatState state = new ThermostatState();

		state.setTemperature(Temperature.celcius(1));

		assertEquals(ThermostatMode.SETPOINT, state.getMode());
		assertTrue(state.getSetPoint().isPresent());
		assertEquals(1, state.getSetPoint().get().get(Unit.CELSIUS));
	}
}
