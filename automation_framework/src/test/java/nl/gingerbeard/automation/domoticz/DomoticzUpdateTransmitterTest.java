package nl.gingerbeard.automation.domoticz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.transmitter.DomoticzUpdateTransmitter;
import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;

public class DomoticzUpdateTransmitterTest {

	private static class TestWebServer extends NanoHTTPD {

		private Status status = Status.OK;
		private final List<String> requests = new ArrayList<>();

		public TestWebServer() {
			super(0);
		}

		public void setResponse(final Status status) {
			this.status = status;
		}

		@Override
		public Response serve(final IHTTPSession session) {
			getRequests().add(session.getMethod() + " " + session.getUri() + "?" + session.getQueryParameterString());
			return super.newFixedLengthResponse(status, MIME_PLAINTEXT, "");
		}

		public List<String> getRequests() {
			return requests;
		}

	}

	@Test
	public void transmitUpdate_urlCorrect() throws IOException {
		final IDomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig);
		final Switch device = new Switch(1);
		device.updateState("on");

		transmitter.transmitDeviceUpdate(device);

		assertEquals(1, webserver.getRequests().size());
		assertEquals("GET /json.html?type=command&param=switchlight&idx=1&switchcmd=on", webserver.getRequests().get(0));
	}

	@Test
	public void transmitUpdateOnOff_correct() throws IOException {
		final IDomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig);
		final Switch device = new Switch(1);

		// on

		device.updateState("on");
		transmitter.transmitDeviceUpdate(device);

		assertEquals(1, webserver.getRequests().size());
		assertEquals("GET /json.html?type=command&param=switchlight&idx=1&switchcmd=on", webserver.getRequests().get(0));

		// off
		device.updateState("off");
		transmitter.transmitDeviceUpdate(device);

		assertEquals(2, webserver.getRequests().size());
		assertEquals("GET /json.html?type=command&param=switchlight&idx=1&switchcmd=off", webserver.getRequests().get(1));

	}

	private TestWebServer webserver;
	private DomoticzConfiguration domoticzConfig;

	@BeforeEach
	public void createTestServer() throws Exception {
		webserver = new TestWebServer();
		webserver.start();
		domoticzConfig = new DomoticzConfiguration(0, new URL("http://localhost:" + webserver.getListeningPort()));
	}

	@AfterEach
	private void stopWebserver() {
		if (webserver != null) {
			webserver.stop();
		}
		webserver = null;
		domoticzConfig = null;
	}

	@Test
	public void urlAppendTest() throws MalformedURLException {
		final URL base = new URL("http://localhost:1234/base/");
		final URL url = new URL(base, "test");

		assertEquals("http://localhost:1234/base/test", url.toString());
	}

	@Test
	public void errorResponse_throwsException() throws IOException {
		final IDomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig);
		final Switch device = new Switch(1);
		device.updateState("on");
		webserver.setResponse(Status.NOT_FOUND);

		try {
			transmitter.transmitDeviceUpdate(device);
			fail("Expected exception");
		} catch (final IOException e) {
			assertEquals("Not Found", e.getMessage());
		}
	}
}
