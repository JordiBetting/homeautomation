package nl.gingerbeard.automation.domoticz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fi.iki.elonen.NanoHTTPD.Response.Status;
import nl.gingerbeard.automation.devices.DimmeableLight;
import nl.gingerbeard.automation.devices.StringTestDevice;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.devices.ThermostatSetpointDevice;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.helpers.TestWebServer;
import nl.gingerbeard.automation.domoticz.transmitter.DomoticzUpdateTransmitter;
import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;
import nl.gingerbeard.automation.state.Level;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.Temperature.Unit;

public class DomoticzUpdateTransmitterTest {

	private TestWebServer webserver;
	private DomoticzConfiguration domoticzConfig;

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
	public void transmitUpdate_urlCorrect() throws IOException {
		final IDomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig);
		final Switch device = new Switch(1);

		transmitter.transmitDeviceUpdate(new NextState<>(device, OnOffState.ON));

		assertEquals(1, webserver.getRequests().size());
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=1&switchcmd=on", webserver.getRequests().get(0));
	}

	@Test
	public void transmitUpdateOnOff_correct() throws IOException {
		final IDomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig);
		final Switch device = new Switch(1);

		// on
		transmitter.transmitDeviceUpdate(new NextState<>(device, OnOffState.ON));
		assertEquals(1, webserver.getRequests().size());
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=1&switchcmd=on", webserver.getRequests().get(0));

		// off
		transmitter.transmitDeviceUpdate(new NextState<>(device, OnOffState.OFF));
		assertEquals(2, webserver.getRequests().size());
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=1&switchcmd=off", webserver.getRequests().get(1));
	}

	@Test
	public void transmitUpdateLevel_correct() throws IOException {
		final IDomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig);
		final DimmeableLight device = new DimmeableLight(1);

		// on
		transmitter.transmitDeviceUpdate(new NextState<>(device, new Level(42)));
		assertEquals(1, webserver.getRequests().size());
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=1&switchcmd=Set%20Level&level=42", webserver.getRequests().get(0));
	}

	@Test
	public void transmitUnsupportedDevice_throwsException() {
		final IDomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig);
		final StringTestDevice device = new StringTestDevice();

		try {
			transmitter.transmitDeviceUpdate(new NextState<>(device, "someString"));
			fail("Expected exception");
		} catch (final IOException e) {
			assertEquals("Cannot construct url from unsupported state: class java.lang.String", e.getMessage());
		}

	}

	@Test
	public void transmitTemperature_correct() throws IOException {
		final IDomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig);
		final ThermostatSetpointDevice device = new ThermostatSetpointDevice(1);

		// on
		transmitter.transmitDeviceUpdate(new NextState<>(device, new Temperature(21, Unit.CELSIUS)));
		assertEquals(1, webserver.getRequests().size());
		assertEquals("GET /json.htm?type=setused&idx=1&setpoint=21.0&protected=false&used=true", webserver.getRequests().get(0));
	}

	@Test
	public void learn_urlAppendTest() throws MalformedURLException {
		final URL base = new URL("http://localhost:1234/base/");
		final URL url = new URL(base, "test");

		assertEquals("http://localhost:1234/base/test", url.toString());
	}

	@Test
	public void errorResponse_throwsException() throws IOException {
		final IDomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig);
		final Switch device = new Switch(1);
		webserver.setResponse(Status.NOT_FOUND, TestWebServer.JSON_ERROR);

		try {
			transmitter.transmitDeviceUpdate(new NextState<>(device, OnOffState.ON));
			fail("Expected exception");
		} catch (final IOException e) {
			assertEquals("http://localhost:" + webserver.getListeningPort() + "/json.htm?type=command&param=switchlight&idx=1&switchcmd=on Not Found", e.getMessage());
		}
	}

	@Test
	public void domoticzError_exceptionThrown() {
		final IDomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig);
		final Switch device = new Switch(1);
		webserver.setResponse(Status.OK, TestWebServer.JSON_ERROR); // Domoticz does this apparently :-(

		try {
			transmitter.transmitDeviceUpdate(new NextState<>(device, OnOffState.ON));
			fail("Expected exception");
		} catch (final IOException e) {
			assertEquals("Failed setting value in domotics: {\"status\":\"error\"}", e.getMessage());
		}
	}

	@Test
	public void malformedJSON_throwsException() {
		final IDomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig);
		final Switch device = new Switch(1);
		webserver.setResponse(Status.OK, TestWebServer.JSON_MALFORMED);

		try {
			transmitter.transmitDeviceUpdate(new NextState<>(device, OnOffState.ON));
			fail("Expected exception");
		} catch (final IOException e) {
			assertEquals("Unable to parse JSON from domoticz", e.getMessage());
		}

	}

}
