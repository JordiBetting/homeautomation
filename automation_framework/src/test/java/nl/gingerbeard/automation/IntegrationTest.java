package nl.gingerbeard.automation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fi.iki.elonen.NanoHTTPD.Response.Status;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.devices.Thermostat;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.helpers.TestWebServer;
import nl.gingerbeard.automation.event.annotations.EventState;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.logging.TestLogger.LogOutputToTestLogger;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.Temperature.Unit;
import nl.gingerbeard.automation.state.ThermostatState;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;
import nl.gingerbeard.automation.state.TimeOfDay;

public class IntegrationTest {

	private TestWebServer webserver;
	private int port;
	private DomoticzConfiguration config;
	private AutomationFrameworkContainer container;
	private IAutomationFrameworkInterface automation;

	@BeforeEach
	public void start() throws IOException {
		webserver = new TestWebServer();
		webserver.setResponse(Status.OK, TestWebServer.JSON_OK);
		webserver.start();

		config = new DomoticzConfiguration(0, new URL("http://localhost:" + webserver.getListeningPort()));
		container = IAutomationFrameworkInterface.createFrameworkContainer(config, new LogOutputToTestLogger());
		container.start();

		port = config.getListenPort();
		automation = container.getRuntime().getService(IAutomationFrameworkInterface.class).get();
	}

	@AfterEach
	public void stop() {
		container.stop();
		container = null;

		webserver.stop();
		webserver = null;

		automation = null;
		port = 0;
		config = null;
	}

	public static class MyRoom extends Room {

		private static final Switch ACTUATOR = new Switch(1);
		private static final Switch SENSOR = new Switch(0);

		public MyRoom() {
			super();
			addDevice(SENSOR);
			addDevice(ACTUATOR);
		}

		@Subscribe
		public NextState<OnOffState> process(final Switch trigger) {
			if (trigger.getIdx() == SENSOR.getIdx()) {
				return new NextState<>(ACTUATOR, OnOffState.ON);
			} else if (trigger.getIdx() == ACTUATOR.getIdx()) {
				return new NextState<>(ACTUATOR, OnOffState.OFF);
			}
			return null;
		}
	}

	private void sendRequest(final int idx, final String state) throws IOException {
		final URL url = new URL("http://localhost:" + port + "/device/" + idx + "/" + state);
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		assertEquals(200, con.getResponseCode(), "Status expected: 200 but was: " + con.getResponseCode() + ". Content: " + con.getContent());
	}

	@Test
	public void actuatorUpdatedBySensorUpdate() throws IOException {
		automation.addRoom(new MyRoom());

		sendRequest(0, "on");

		List<String> requests = webserver.getRequests();
		assertEquals(1, requests.size());
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=1&switchcmd=on", requests.get(0));

		// reply with device update 'in Domoticz'
		sendRequest(1, "on");
		requests = webserver.getRequests();
		assertEquals(2, requests.size());
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=1&switchcmd=off", requests.get(1));

	}

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

		sendRequest(RoomWithThermostat.IDX_SENSOR, "on");

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

		sendRequest(RoomWithThermostat.IDX_SENSOR, "on");

		final List<String> requests = webserver.getRequests();
		assertEquals(1, requests.size());

		assertEquals("GET /json.htm?type=setused&idx=" + RoomWithThermostat.IDX_MODE + "&tmode=0&protected=false&used=true", requests.get(0));
	}

	@Test
	public void thermostatModeFull_bySensorUpdate() throws IOException {
		final ThermostatState thermostatState = new ThermostatState();
		thermostatState.setFullHeat();
		automation.addRoom(new RoomWithThermostat(thermostatState));

		sendRequest(RoomWithThermostat.IDX_SENSOR, "on");

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

		sendRequest(RoomWithThermostat.IDX_MODE, "off");
		assertEquals(1, room.getThermostatChangeCount());
		assertEquals(ThermostatMode.OFF, room.getThermostat().getState().getMode());
		assertFalse(room.getThermostat().getState().getSetPoint().isPresent());

		sendRequest(RoomWithThermostat.IDX_MODE, "full_heat");
		assertEquals(2, room.getThermostatChangeCount());
		assertEquals(ThermostatMode.FULL_HEAT, room.getThermostat().getState().getMode());
		assertFalse(room.getThermostat().getState().getSetPoint().isPresent());

		sendRequest(RoomWithThermostat.IDX_MODE, "setpoint");
		assertEquals(3, room.getThermostatChangeCount());
		assertEquals(ThermostatMode.SETPOINT, room.getThermostat().getState().getMode());
		assertTrue(room.getThermostat().getState().getSetPoint().isPresent());

		sendRequest(RoomWithThermostat.IDX_SETPOINT, "14");
		assertEquals(4, room.getThermostatChangeCount());
		assertTrue(room.getThermostat().getState().getSetPoint().isPresent());
		assertEquals(14, room.getThermostat().getState().getSetPoint().get().get(Unit.CELSIUS));
	}

	public static class TimeRoom extends Room {

		private static final Switch ACTUATOR = new Switch(1);
		private int nighttime_count = 0;
		private int daytime_count;

		TimeRoom() {
			super();
			addDevice(ACTUATOR);
		}

		@Subscribe
		public void receiveTime(final TimeOfDay timeofday) {
			switch (timeofday) {
			case DAYTIME:
				daytime_count++;
				break;
			case NIGHTTIME:
				nighttime_count++;
				break;
			}
		}

		public int getNighttime_count() {
			return nighttime_count;
		}

		public int getDaytime_count() {
			return daytime_count;
		}

	}

	@Test
	public void updateTime_eventReceived() throws IOException {
		final TimeRoom room = new TimeRoom();
		automation.addRoom(room);

		assertEquals(0, room.getDaytime_count());
		assertEquals(0, room.getNighttime_count());

		sendNightTime();

		assertEquals(0, room.getDaytime_count());
		assertEquals(1, room.getNighttime_count());

		sendDaytime();

		assertEquals(1, room.getDaytime_count());
		assertEquals(1, room.getNighttime_count());
	}

	private void sendRequest(final int curTime, final int sunrise, final int sunset) throws IOException {
		final URL url = new URL("http://localhost:" + port + "/time/" + curTime + "/" + sunrise + "/" + sunset);
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		assertEquals(200, con.getResponseCode(), "Status expected: 200 but was: " + con.getResponseCode() + ". Content: " + con.getContent());
	}

	@EventState(timeOfDay = TimeOfDay.DAYTIME)
	public static class DayTimeSwitchRoom extends Room {

		private static final Switch SENSOR = new Switch(0);
		private static final Switch ACTUATOR = new Switch(1);
		private int callCount = 0;

		DayTimeSwitchRoom() {
			super();
			addDevice(SENSOR);
			addDevice(ACTUATOR);
		}

		@Subscribe
		public void receiveEvent(final Switch input) {
			callCount++;
		}

		public int getCallCount() {
			return callCount;
		}
	}

	@Test
	public void switch_daytime_received() throws IOException {
		final DayTimeSwitchRoom room = new DayTimeSwitchRoom();
		automation.addRoom(room);

		// initial, no calls
		assertEquals(0, room.getCallCount());

		// daytime event should not be triggered on room
		sendDaytime();
		assertEquals(0, room.getCallCount());

		// it is daytime, switch changed, expect to receive event
		sendRequest(0, "on");
		assertEquals(1, room.getCallCount());

		// switch to nighttime, should not be triggered in room
		sendNightTime();
		assertEquals(1, room.getCallCount());

		// it is nighttime, switch changed, expect no event
		sendRequest(0, "off");
		assertEquals(1, room.getCallCount());
	}

	private void sendDaytime() throws IOException {
		sendRequest(150, 100, 200);
	}

	private void sendNightTime() throws IOException {
		sendRequest(10, 100, 200);
	}
}
