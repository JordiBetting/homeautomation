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
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiverServer;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiverServer.EventReceived;
import nl.gingerbeard.automation.logging.LogLevel;
import nl.gingerbeard.automation.logging.TestLogger;

public class DomoticzEventReceiverTest {

	private DomoticzConfiguration config;
	private DomoticzEventReceiverServer receiver;
	private TestLogger log;

	@BeforeEach
	public void initConfig() throws IOException {
		config = new DomoticzConfiguration(0, new URL("http://localhost/"));
		log = new TestLogger();
		receiver = new DomoticzEventReceiverServer(config, log);
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
	public void succes_logging_device() throws IOException {
		final EventReceived listener = mock(EventReceived.class);
		when(listener.deviceChanged(anyInt(), any())).thenReturn(true);
		receiver.setEventListener(listener);

		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/device/1234/hello/");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode());
		// log.assertContains(LogLevel.DEBUG, "GET /device/1234/hello/ from 127.0.0.1");
		log.assertContains(LogLevel.DEBUG, "Success");
	}

	@Test
	public void succes_logging_time() throws IOException {
		final EventReceived listener = mock(EventReceived.class);
		when(listener.timeChanged(anyInt(), anyInt(), anyInt())).thenReturn(true);
		receiver.setEventListener(listener);

		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/time/1/2/3/");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode());
		// log.assertContains(LogLevel.DEBUG, "GET /time/1/2/3/ from 127.0.0.1");
		log.assertContains(LogLevel.DEBUG, "Updated time to ResponseTimeParameters [currentTime=1, sunriseTime=2, sunsetTime=3]");
	}

	@Test
	public void wrong_url_404() throws IOException {
		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/doesnotexist");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(404, con.getResponseCode());
		// log.assertContains(LogLevel.WARNING, "Returning 404 after unrecognized URL: /doesnotexist");
	}

	@Test
	public void device_noListener_noException() throws IOException {
		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/device/1234/test");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode());
	}

	@Test
	public void time_noListener_noException() throws IOException {
		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/time/1/2/3");
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
	public void deviceListenerReturnsFalse_errorCode404() throws IOException {
		final EventReceived listener = mock(EventReceived.class);
		when(listener.deviceChanged(anyInt(), any())).thenReturn(false);
		receiver.setEventListener(listener);

		final int port = receiver.getListeningPort();
		final URL url = new URL("http://localhost:" + port + "/device/1234/hello/");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(404, con.getResponseCode());
	}

	@Test
	public void timerListenerReturnsFalse_errorCode404() throws IOException {
		final EventReceived listener = mock(EventReceived.class);
		when(listener.timeChanged(anyInt(), anyInt(), anyInt())).thenReturn(false);
		receiver.setEventListener(listener);

		final int port = receiver.getListeningPort();
		final URL url = new URL("http://localhost:" + port + "/time/1/2/3");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(404, con.getResponseCode());
	}

	@Test
	public void parse_timeinput() throws IOException {
		final String path = "/time/1234/5678/9012";
		testTimePath(path, 1234, 5678, 9012);
	}

	@Test
	public void parse_timeinput_trailingSlash() throws IOException {
		final String path = "/time/1/2/3/";
		testTimePath(path, 1, 2, 3);
	}

	private void testTimePath(final String path, final int expectedCurtime, final int expectedSunrise, final int expectedSunset) throws MalformedURLException, IOException, ProtocolException {
		final EventReceived listener = mock(EventReceived.class);
		when(listener.timeChanged(anyInt(), anyInt(), anyInt())).thenReturn(true);
		receiver.setEventListener(listener);

		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + path);
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode());
		verify(listener, times(1)).timeChanged(expectedCurtime, expectedSunrise, expectedSunset);
		verifyNoMoreInteractions(listener);
	}

	@Test
	public void invalidTime_returns404() throws IOException {
		receiver.setEventListener(mock(EventReceived.class));

		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/time/blaat");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(404, con.getResponseCode());
	}

	@Test
	public void invalidDevice_returns404() throws IOException {
		receiver.setEventListener(mock(EventReceived.class));

		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/device/blaat");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(404, con.getResponseCode());
	}

	@Test
	public void alarm_received() throws IOException {
		final EventReceived listener = mock(EventReceived.class);
		when(listener.alarmChanged(any())).thenReturn(true);
		receiver.setEventListener(listener);

		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/alarm/arm_away");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode());
	}

	@Test
	public void alarm_sameTwice_onceUpdated() throws IOException {
		final EventReceived listener = mock(EventReceived.class);
		when(listener.alarmChanged(any())).thenReturn(true);
		receiver.setEventListener(listener);

		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/alarm/arm_away");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		assertEquals(200, con.getResponseCode());
	}

	@Test
	public void alarm_novar_404() throws IOException {
		final EventReceived listener = mock(EventReceived.class);
		receiver.setEventListener(listener);

		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/alarm/");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(404, con.getResponseCode());
	}

	@Test
	public void alarm_nolistener_noException() throws IOException {
		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/alarm/arm_away");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode());
	}

	@Test
	public void alarm_receiverFalse_404() throws IOException {
		final EventReceived listener = mock(EventReceived.class);
		when(listener.alarmChanged(any())).thenReturn(false);
		receiver.setEventListener(listener);

		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/alarm/arm_away");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(404, con.getResponseCode());
	}
}
