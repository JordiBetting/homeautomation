package nl.gingerbeard.automation.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.Room;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.devices.Thermostat;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.Temperature.Unit;
import nl.gingerbeard.automation.state.ThermostatState;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;

public class ThermostatIntegrationTest extends IntegrationTest {
	public static class RoomWithThermostat extends Room {

		public static final int IDX_SENSOR = 1;
		public static final int IDX_SETPOINT = 2;
		public static final int IDX_MODE = 3;

		private final Thermostat thermostat;
		private static final Switch SENSOR = new Switch(IDX_SENSOR);
		private final ThermostatState nextState;
		private int thermostatChanges;

		public RoomWithThermostat(final ThermostatState nextState) {
			this.nextState = nextState;
			thermostat = new Thermostat(IDX_SETPOINT, IDX_MODE);
			addDevice(thermostat).and(SENSOR);
		}

		@Subscribe
		public List<NextState<?>> process(final Switch trigger) {
			return thermostat.createNextState(nextState);
		}

		@Subscribe
		public void countThermostatChanges(final Thermostat device) {
			thermostatChanges++;
		}

		public int getThermostatChangeCount() {
			return thermostatChanges;
		}

		public Thermostat getThermostat() {
			return thermostat;
		}

	}

	@Test
	public void thermostatSetpointUpdated_bySensorUpdate() throws IOException {
		final ThermostatState thermostatState = new ThermostatState();
		thermostatState.setTemperature(Temperature.celcius(15));

		automation.addRoom(new RoomWithThermostat(thermostatState));

		deviceChanged(RoomWithThermostat.IDX_SENSOR, "on");

		final List<String> requests = webserver.getRequests();
		assertEquals(2, requests.size());

		assertEquals("GET /json.htm?type=setused&idx=" + RoomWithThermostat.IDX_MODE + "&tmode=2&protected=false&used=true", requests.get(0));
		assertEquals("GET /json.htm?type=setused&idx=" + RoomWithThermostat.IDX_SETPOINT + "&setpoint=15.0&protected=false&used=true", requests.get(1));
	}

	@Test
	public void thermostatModeOff_bySensorUpdate() throws IOException {
		final ThermostatState thermostatState = new ThermostatState();
		thermostatState.setOff();
		automation.addRoom(new RoomWithThermostat(thermostatState));

		deviceChanged(RoomWithThermostat.IDX_SENSOR, "on");

		final List<String> requests = webserver.getRequests();
		assertEquals(1, requests.size());

		assertEquals("GET /json.htm?type=setused&idx=" + RoomWithThermostat.IDX_MODE + "&tmode=0&protected=false&used=true", requests.get(0));
	}

	@Test
	public void thermostatModeFull_bySensorUpdate() throws IOException {
		final ThermostatState thermostatState = new ThermostatState();
		thermostatState.setFullHeat();
		automation.addRoom(new RoomWithThermostat(thermostatState));

		deviceChanged(RoomWithThermostat.IDX_SENSOR, "on");

		final List<String> requests = webserver.getRequests();
		assertEquals(1, requests.size());

		assertEquals("GET /json.htm?type=setused&idx=" + RoomWithThermostat.IDX_MODE + "&tmode=3&protected=false&used=true", requests.get(0));
	}

	@Test
	public void thermostatUpdateReceived_compositeTriggered() throws IOException {
		final ThermostatState thermostatState = new ThermostatState();
		thermostatState.setFullHeat();
		final RoomWithThermostat room = new RoomWithThermostat(thermostatState);
		automation.addRoom(room);

		deviceChanged(RoomWithThermostat.IDX_MODE, "off");
		assertEquals(1, room.getThermostatChangeCount());
		assertEquals(ThermostatMode.OFF, room.getThermostat().getState().getMode());
		assertFalse(room.getThermostat().getState().getSetPoint().isPresent());

		deviceChanged(RoomWithThermostat.IDX_MODE, "full_heat");
		assertEquals(2, room.getThermostatChangeCount());
		assertEquals(ThermostatMode.FULL_HEAT, room.getThermostat().getState().getMode());
		assertFalse(room.getThermostat().getState().getSetPoint().isPresent());

		deviceChanged(RoomWithThermostat.IDX_MODE, "setpoint");
		assertEquals(3, room.getThermostatChangeCount());
		assertEquals(ThermostatMode.SETPOINT, room.getThermostat().getState().getMode());
		assertTrue(room.getThermostat().getState().getSetPoint().isPresent());

		deviceChanged(RoomWithThermostat.IDX_SETPOINT, "14");
		assertEquals(4, room.getThermostatChangeCount());
		assertTrue(room.getThermostat().getState().getSetPoint().isPresent());
		assertEquals(14, room.getThermostat().getState().getSetPoint().get().get(Unit.CELSIUS));
	}
}
