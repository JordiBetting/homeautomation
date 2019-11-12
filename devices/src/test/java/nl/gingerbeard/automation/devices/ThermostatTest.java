package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.Temperature.Unit;
import nl.gingerbeard.automation.state.ThermostatState;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;

public class ThermostatTest {

	@Test
	public void updateMode_parentUpdated() {
		final Thermostat thermostat = new Thermostat(2, 1);

		thermostat.getModeDevice().updateState("SETPOINT");

		assertEquals(ThermostatMode.SETPOINT, thermostat.getState().getMode());
	}

	@Test
	public void updateSetpoint_parentUpdated() {
		final Thermostat thermostat = new Thermostat(2, 1);

		thermostat.getSetpointDevice().get(0).updateState("-14");

		assertTrue(thermostat.getState().getSetPoint().isPresent());
		assertEquals(-14, thermostat.getState().getSetPoint().get().get(Unit.CELSIUS));
	}

	@Test
	public void thermostatSubdevices_parentSet() {
		final Thermostat thermostat = new Thermostat(2, 1);

		assertTrue(thermostat.getModeDevice().getParent().isPresent());
		assertEquals(thermostat, thermostat.getModeDevice().getParent().get());

		assertTrue(thermostat.getSetpointDevice().get(0).getParent().isPresent());
		assertEquals(thermostat, thermostat.getSetpointDevice().get(0).getParent().get());
	}

	@Test
	public void thermostat_createNextState_off() {
		final Thermostat thermostat = new Thermostat(2, 1);
		final ThermostatState state = new ThermostatState();
		state.setOff();

		final List<NextState<?>> nextState = thermostat.createNextState(state);

		assertEquals(1, nextState.size());
		assertEquals(ThermostatModeDevice.class, nextState.get(0).getDevice().getClass());
		assertEquals(ThermostatMode.OFF, nextState.get(0).get());
	}

	@Test
	public void thermostat_createNextState_fullheat() {
		final Thermostat thermostat = new Thermostat(2, 1);
		final ThermostatState state = new ThermostatState();
		state.setFullHeat();

		final List<NextState<?>> nextState = thermostat.createNextState(state);

		assertEquals(1, nextState.size());
		assertEquals(ThermostatModeDevice.class, nextState.get(0).getDevice().getClass());
		assertEquals(ThermostatMode.FULL_HEAT, nextState.get(0).get());
	}

	@Test
	public void thermostat_createNextState_setpoint() {
		final Thermostat thermostat = new Thermostat(2, 1);
		final ThermostatState state = new ThermostatState();
		state.setTemperature(Temperature.celcius(1));

		final List<NextState<?>> nextState = thermostat.createNextState(state);

		assertEquals(2, nextState.size());
		assertEquals(ThermostatModeDevice.class, nextState.get(0).getDevice().getClass());
		assertEquals(ThermostatMode.SETPOINT, nextState.get(0).get());
		assertEquals(ThermostatSetpointDevice.class, nextState.get(1).getDevice().getClass());
		assertEquals(Temperature.class, nextState.get(1).get().getClass());
		assertEquals(1, ((Temperature) nextState.get(1).get()).get(Unit.CELSIUS));
	}
	
	@Test
	public void thermostat_mutlipleSetpoints_equal() {
		final Thermostat thermostat = new Thermostat(1, 2, 3);
		final ThermostatState state = new ThermostatState();
		state.setTemperature(Temperature.celcius(1));

		final List<NextState<?>> nextState = thermostat.createNextState(state);

		assertEquals(3, nextState.size());
		
		assertEquals(ThermostatModeDevice.class, nextState.get(0).getDevice().getClass());
		assertEquals(ThermostatMode.SETPOINT, nextState.get(0).get());
		
		assertEquals(ThermostatSetpointDevice.class, nextState.get(1).getDevice().getClass());
		assertEquals(Temperature.class, nextState.get(1).get().getClass());
		assertEquals(1, ((Temperature) nextState.get(1).get()).get(Unit.CELSIUS));

		assertEquals(ThermostatSetpointDevice.class, nextState.get(2).getDevice().getClass());
		assertEquals(Temperature.class, nextState.get(2).get().getClass());
		assertEquals(1, ((Temperature) nextState.get(2).get()).get(Unit.CELSIUS));
	}
	
	@Test
	public void thermostat_nullList_throwsException() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new Thermostat(1, null));
		assertEquals("idxSetpointDevices should not be null", e.getMessage());
	}
	
	@Test
	public void thermostat_emptyList_throwsException() {
		int[] empty = {};
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new Thermostat(1, empty));
		assertEquals("idxSetPointDevices array should not be empty", e.getMessage());
	}
}
