package nl.gingerbeard.automation.domoticz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiver;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiver.EventReceived;
import nl.gingerbeard.automation.logging.LogLevel;
import nl.gingerbeard.automation.logging.TestLogger;

public class DomoticzEventReceiverTest {

	private DomoticzConfiguration config;
	private DomoticzEventReceiver receiver;
	private TestLogger log;

	@BeforeEach
	public void initConfig() throws IOException {
		config = new DomoticzConfiguration(0, new URL("http://localhost/"));
		log = new TestLogger();
		receiver = new DomoticzEventReceiver(config, log);
	}

	@AfterEach
	public void closeReceiver() {
		if (receiver != null) {
			receiver.stop();
			receiver = null;
		}
		log = null;
		config = null;
	}

	@Test
	public void create_no_exception() throws IOException {

	}

	@Test
	public void create_port_chosen() throws IOException {
		final int port = receiver.getListeningPort();

		assertNotEquals(0, port);
	}

	@Test
	public void parse_input() throws IOException {
		final EventReceived listener = mock(EventReceived.class);
		when(listener.deviceChanged(anyInt(), any())).thenReturn(true);
		receiver.setEventListener(listener);

		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/device/1234/hello");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode());
		verify(listener, times(1)).deviceChanged(1234, "hello");
		verifyNoMoreInteractions(listener);
	}

	@Test
	public void parse_input_trailingSlash() throws IOException {
		final EventReceived listener = mock(EventReceived.class);
		when(listener.deviceChanged(anyInt(), any())).thenReturn(true);
		receiver.setEventListener(listener);

		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/device/1234/hello/");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode());
		verify(listener, times(1)).deviceChanged(1234, "hello");
		verifyNoMoreInteractions(listener);
	}

	@Test
	public void succes_logging() throws IOException {
		final EventReceived listener = mock(EventReceived.class);
		when(listener.deviceChanged(anyInt(), any())).thenReturn(true);
		receiver.setEventListener(listener);

		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/device/1234/hello/");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode());
		log.assertContains(LogLevel.DEBUG, "GET /device/1234/hello/ from 127.0.0.1");
		log.assertContains(LogLevel.DEBUG, "Success");
	}

	@Test
	public void wrong_url_404() throws IOException {
		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/doesnotexist");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(404, con.getResponseCode());
		log.assertContains(LogLevel.WARNING, "Returning 404 after unrecognized URL: /doesnotexist");
	}

	@Test
	public void noListener_noException() throws IOException {
		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/device/1234/test");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode());
	}

	@Test
	public void unsupportedMethod_returns405() throws IOException {
		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/device/1234/test");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("PUT");

		assertEquals(405, con.getResponseCode());
		log.assertContains(LogLevel.WARNING, "Received unsupported method PUT on /device/1234/test");
	}

	@Test
	public void listener_throwsException_internalServerError() throws IOException {
		final EventReceived listener = mock(EventReceived.class);
		receiver.setEventListener(listener);
		final int port = receiver.getListeningPort();

		when(listener.deviceChanged(anyInt(), any())).thenThrow(new UnsupportedOperationException());

		final URL url = new URL("http://localhost:" + port + "/device/1234/hello/");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(500, con.getResponseCode());
		log.assertContains(LogLevel.EXCEPTION, "Failure in processing request");
	}

	@Test
	public void listenerReturnsFalse_errorCode404() throws IOException {
		final EventReceived listener = mock(EventReceived.class);
		when(listener.deviceChanged(anyInt(), any())).thenReturn(false);
		receiver.setEventListener(listener);

		final int port = receiver.getListeningPort();
		final URL url = new URL("http://localhost:" + port + "/device/1234/hello/");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(404, con.getResponseCode());
	}
}
