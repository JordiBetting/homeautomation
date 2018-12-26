package nl.gingerbeard.automation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fi.iki.elonen.NanoHTTPD.Response.Status;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.helpers.TestWebServer;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.service.Container;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public class IntegrationTest {

	private TestWebServer webserver;
	private int port;
	private DomoticzConfiguration config;
	private Container container;
	private IAutomationFrameworkInterface automation;

	// TODO: use local webserver, trigger transmitter, ensure room updates actuator based on sensor value.

	@BeforeEach
	public void start() throws MalformedURLException {
		webserver = new TestWebServer();
		webserver.setResponse(Status.OK, TestWebServer.JSON_OK);

		config = new DomoticzConfiguration(0, new URL("http://localhost:" + webserver.getListeningPort()));
		container = IAutomationFrameworkInterface.createFrameworkContainer();
		container.register(DomoticzConfiguration.class, config);
		container.start();

		port = config.getListenPort();
		automation = container.getComponent(IAutomationFrameworkInterface.class).get();
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
				final OnOffState nextState = ACTUATOR.getState() == OnOffState.ON ? OnOffState.OFF : OnOffState.ON;
				return new NextState<>(ACTUATOR, nextState);
			}
			return null;
		}
	}

	private void sendRequest() throws IOException {
		final URL url = new URL("http://localhost:" + port + "/0/On");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		assertEquals(200, con.getResponseCode());
	}

	@Test
	public void actuatorUpdatedBySensorUpdate() throws IOException {
		automation.addRoom(new MyRoom());

		sendRequest();

		final List<String> requests = webserver.getRequests();

		assertEquals(1, requests.size());
		assertEquals("GET theExpectedUpdate", requests.get(0));
	}

}
