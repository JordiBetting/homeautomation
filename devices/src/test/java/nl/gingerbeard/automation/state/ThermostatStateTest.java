package nl.gingerbeard.automation.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.state.Temperature.Unit;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

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

	@Test
	public void testToString_setpoint() {
		final ThermostatState state = new ThermostatState();
		state.setTemperature(Temperature.celcius(1));

		assertEquals("ThermostatState [mode=SETPOINT, setPoint=Optional[Temperature [value=1.0, unit=CELSIUS]]]", state.toString());
	}

	@Test
	public void testToString_off() {
		final ThermostatState state = new ThermostatState();
		state.setOff();

		assertEquals("ThermostatState [mode=OFF, setPoint=Optional.empty]", state.toString());
	}

	@Test
	public void testToString_fullheat() {
		final ThermostatState state = new ThermostatState();
		state.setFullHeat();

		assertEquals("ThermostatState [mode=FULL_HEAT, setPoint=Optional.empty]", state.toString());
	}

	@Test
	public void equalsContract() {
		EqualsVerifier.forClass(ThermostatState.class).suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
