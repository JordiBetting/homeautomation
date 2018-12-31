package nl.gingerbeard.automation;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.service.Container;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.ThermostatState;

public class IntegrationTest {

	private TestWebServer webserver;
	private int port;
	private DomoticzConfiguration config;
	private Container container;
	private IAutomationFrameworkInterface automation;

	// TODO: use local webserver, trigger transmitter, ensure room updates actuator based on sensor value.

	@BeforeEach
	public void start() throws IOException {
		webserver = new TestWebServer();
		webserver.setResponse(Status.OK, TestWebServer.JSON_OK);
		webserver.start();

		config = new DomoticzConfiguration(0, new URL("http://localhost:" + webserver.getListeningPort()));
		container = IAutomationFrameworkInterface.createFrameworkContainer();
		container.register(DomoticzConfiguration.class, config);
		container.start();

		port = config.getListenPort();
		automation = container.getService(IAutomationFrameworkInterface.class).get();
	}

	@AfterEach
	public void stop() {
		container.shutDown();
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
		final URL url = new URL("http://localhost:" + port + "/" + idx + "/" + state);
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		assertEquals(200, con.getResponseCode());
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

		private final Thermostat thermostat;
		private static final Switch SENSOR = new Switch(1);
		private final ThermostatState nextState;

		public RoomWithThermostat(final ThermostatState nextState) {
			this.nextState = nextState;
			thermostat = new Thermostat(2, 3);
			addDevice(thermostat).and(SENSOR);
		}

		@Subscribe
		public List<NextState<?>> process(final Switch trigger) {
			return thermostat.createNextState(nextState);
		}

	}

	@Test
	public void thermostatSetpointUpdated_bySensorUpdate() throws IOException {
		final ThermostatState thermostatState = new ThermostatState();
		thermostatState.setTemperature(Temperature.celcius(15));

		automation.addRoom(new RoomWithThermostat(thermostatState));

		sendRequest(1, "on");

		final List<String> requests = webserver.getRequests();
		assertEquals(2, requests.size());

		assertEquals("GET /json.htm?type=setused&idx=3&tmode=2&protected=false&used=true", requests.get(0));
		assertEquals("GET /json.htm?type=setused&idx=2&setpoint=15.0&protected=false&used=true", requests.get(1));
	}

	@Test
	public void thermostatModeOff_bySensorUpdate() throws IOException {
		final ThermostatState thermostatState = new ThermostatState();
		thermostatState.setOff();
		automation.addRoom(new RoomWithThermostat(thermostatState));

		sendRequest(1, "on");

		final List<String> requests = webserver.getRequests();
		assertEquals(1, requests.size());

		assertEquals("GET /json.htm?type=setused&idx=3&tmode=0&protected=false&used=true", requests.get(0));
	}

	@Test
	public void thermostatModeFull_bySensorUpdate() throws IOException {
		final ThermostatState thermostatState = new ThermostatState();
		thermostatState.setFullHeat();
		;
		automation.addRoom(new RoomWithThermostat(thermostatState));

		sendRequest(1, "on");

		final List<String> requests = webserver.getRequests();
		assertEquals(1, requests.size());

		assertEquals("GET /json.htm?type=setused&idx=3&tmode=3&protected=false&used=true", requests.get(0));
	}
}
