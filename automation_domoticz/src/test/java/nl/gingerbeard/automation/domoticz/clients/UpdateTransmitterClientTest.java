package nl.gingerbeard.automation.domoticz.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fi.iki.elonen.NanoHTTPD.Response.Status;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.DimmeableLight;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.devices.ThermostatSetpointDevice;
import nl.gingerbeard.automation.domoticz.api.DomoticzException;
import nl.gingerbeard.automation.domoticz.clients.UpdateTransmitterClient;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.TestLogger;
import nl.gingerbeard.automation.state.Level;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.Temperature.Unit;
import nl.gingerbeard.automation.testutils.TestWebServer;

public class UpdateTransmitterClientTest {

	private TestWebServer webserver;
	private DomoticzConfiguration domoticzConfig;

	public static class StringTestDevice extends Device<String> {

		public StringTestDevice() {
			super(42);
		}

		@Override
		public boolean updateState(final String newState) {
			return false;
		}

	}

	@BeforeEach
	public void createTestServer() throws Exception {
		webserver = new TestWebServer();
		webserver.start();
		domoticzConfig = new DomoticzConfiguration(0, new URL("http://localhost:" + webserver.getListeningPort()));
	}

	@AfterEach
	public void stopWebserver() {
		if (webserver != null) {
			webserver.stop();
		}
		webserver = null;
		domoticzConfig = null;
	}

	@Test
	public void transmitUpdate_urlCorrect() throws IOException, DomoticzException {
		final UpdateTransmitterClient transmitter = new UpdateTransmitterClient(domoticzConfig, new TestLogger());
		final Switch device = new Switch(1);

		transmitter.transmitDeviceUpdate(new NextState<>(device, OnOffState.ON));

		assertEquals(1, webserver.getRequests().size());
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=1&switchcmd=On", webserver.getRequests().get(0));
	}

	@Test
	public void transmitUpdateOnOff_correct() throws IOException, DomoticzException {
		final UpdateTransmitterClient transmitter = new UpdateTransmitterClient(domoticzConfig, new TestLogger());
		final Switch device = new Switch(1);

		// on
		transmitter.transmitDeviceUpdate(new NextState<>(device, OnOffState.ON));
		assertEquals(1, webserver.getRequests().size());
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=1&switchcmd=On", webserver.getRequests().get(0));

		// off
		transmitter.transmitDeviceUpdate(new NextState<>(device, OnOffState.OFF));
		assertEquals(2, webserver.getRequests().size());
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=1&switchcmd=Off",
				webserver.getRequests().get(1));
	}

	@Test
	public void transmitUpdateLevel_correct() throws IOException, DomoticzException {
		final UpdateTransmitterClient transmitter = new UpdateTransmitterClient(domoticzConfig, new TestLogger());
		final DimmeableLight device = new DimmeableLight(1);

		// on
		transmitter.transmitDeviceUpdate(new NextState<>(device, new Level(42)));
		assertEquals(1, webserver.getRequests().size());
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=1&switchcmd=Set%20Level&level=42",
				webserver.getRequests().get(0));
	}

	@Test
	public void transmitUnsupportedDevice_throwsException() throws IOException {
		final UpdateTransmitterClient transmitter = new UpdateTransmitterClient(domoticzConfig, new TestLogger());
		final StringTestDevice device = new StringTestDevice();

		try {
			transmitter.transmitDeviceUpdate(new NextState<>(device, "someString"));
			fail("Expected exception");
		} catch (final DomoticzException e) {
			assertEquals("Cannot construct url from unsupported state: class java.lang.String", e.getMessage());
		}

	}

	@Test
	public void transmitTemperature_correct() throws IOException, DomoticzException {
		final UpdateTransmitterClient transmitter = new UpdateTransmitterClient(domoticzConfig, new TestLogger());
		final ThermostatSetpointDevice device = new ThermostatSetpointDevice(1);

		// on
		transmitter.transmitDeviceUpdate(new NextState<>(device, new Temperature(21, Unit.CELSIUS)));
		assertEquals(1, webserver.getRequests().size());
		assertEquals("GET /json.htm?type=setused&idx=1&setpoint=21.0&protected=false&used=true",
				webserver.getRequests().get(0));
	}

	@Test
	public void learn_urlAppendTest() throws MalformedURLException {
		final URL base = new URL("http://localhost:1234/base/");
		final URL url = new URL(base, "test");

		assertEquals("http://localhost:1234/base/test", url.toString());
	}

	@Test
	public void errorResponse_throwsException() throws IOException {
		final UpdateTransmitterClient transmitter = new UpdateTransmitterClient(domoticzConfig, new TestLogger());
		final Switch device = new Switch(1);
		webserver.setDefaultResponse(Status.NOT_FOUND, TestWebServer.JSON_ERROR);

		try {
			transmitter.transmitDeviceUpdate(new NextState<>(device, OnOffState.ON));
			fail("Expected exception");
		} catch (final DomoticzException e) {
			assertEquals("responsecode expected 200, but was: 404" + System.lineSeparator() + "http://localhost:"
					+ webserver.getListeningPort()
					+ "/json.htm?type=command&param=switchlight&idx=1&switchcmd=On Not Found" + System.lineSeparator()
					+ "{ \"status\" : \"error\" }", e.getMessage());
		}
	}

	@Test
	public void domoticzError_exceptionThrown() throws IOException {
		final UpdateTransmitterClient transmitter = new UpdateTransmitterClient(domoticzConfig, new TestLogger());
		final Switch device = new Switch(1);
		webserver.setDefaultResponse(Status.OK, TestWebServer.JSON_ERROR); // Domoticz does this apparently :-(

		try {
			transmitter.transmitDeviceUpdate(new NextState<>(device, OnOffState.ON));
			fail("Expected exception");
		} catch (final DomoticzException e) {
			assertEquals("Failed setting value in domoticz: error", e.getMessage());
		}
	}

	@Test
	public void malformedJSON_throwsException() throws IOException {
		final UpdateTransmitterClient transmitter = new UpdateTransmitterClient(domoticzConfig, new TestLogger());
		final Switch device = new Switch(1);
		webserver.setDefaultResponse(Status.OK, TestWebServer.JSON_MALFORMED);

		try {
			transmitter.transmitDeviceUpdate(new NextState<>(device, OnOffState.ON));
			fail("Expected exception");
		} catch (final DomoticzException e) {
			assertEquals("Unable to parse JSON from domoticz", e.getMessage());
		}
	}

	@Test
	public void testFailedConnection() throws IOException {
		final DomoticzConfiguration domoticzConfiguration = new DomoticzConfiguration(0,
				new URL("http://doesnotexist"));
		domoticzConfiguration.setConnectTimeoutMS(5000);
		final UpdateTransmitterClient transmitter = new UpdateTransmitterClient(domoticzConfiguration,
				new TestLogger());
		final NextState<OnOffState> newState = new NextState<>(new Switch(42), OnOffState.OFF);

		assertThrows(DomoticzException.class, () -> transmitter.transmitDeviceUpdate(newState));
	}

	@Test
	public void unexpectedErrorCode_bodyInException() throws IOException {
		final UpdateTransmitterClient transmitter = new UpdateTransmitterClient(domoticzConfig, new TestLogger());
		final Switch device = new Switch(1);
		final String body = "I Expect this in the exception";
		webserver.setDefaultResponse(Status.NOT_FOUND, body);

		final DomoticzException e = assertThrows(DomoticzException.class,
				() -> transmitter.transmitDeviceUpdate(new NextState<>(device, OnOffState.ON)));
		assertTrue(e.getMessage().contains(body));
	}

	@Test
	public void unexpectedErrorCode_emptyResponse_notInException() throws IOException {
		final UpdateTransmitterClient transmitter = new UpdateTransmitterClient(domoticzConfig, new TestLogger());
		final Switch device = new Switch(1);
		webserver.setDefaultResponse(Status.NOT_FOUND, "");

		final DomoticzException e = assertThrows(DomoticzException.class,
				() -> transmitter.transmitDeviceUpdate(new NextState<>(device, OnOffState.ON)));
		assertEquals("responsecode expected 200, but was: 404" + System.lineSeparator() + "http://localhost:"
				+ webserver.getListeningPort()
				+ "/json.htm?type=command&param=switchlight&idx=1&switchcmd=On Not Found", e.getMessage());
	}

	@Test
	public void usesAuthentication() throws DomoticzException, IOException {
		domoticzConfig.setCredentials("rootOfAllEvil", "money");
		final UpdateTransmitterClient transmitter = new UpdateTransmitterClient(domoticzConfig, new TestLogger());
		final Switch device = new Switch(1);

		transmitter.transmitDeviceUpdate(new NextState<>(device, OnOffState.ON));

		assertEquals(1, webserver.getRequestHeaders().size());
		assertEquals("Basic cm9vdE9mQWxsRXZpbDptb25leQ==", webserver.getRequestHeaders().get(0).get("authorization"));
	}
}
