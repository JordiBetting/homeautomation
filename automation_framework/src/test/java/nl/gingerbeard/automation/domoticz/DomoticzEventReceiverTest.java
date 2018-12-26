package nl.gingerbeard.automation.domoticz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiver;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiver.EventReceived;

public class DomoticzEventReceiverTest {

	private static DomoticzConfiguration CONFIG;
	private static DomoticzEventReceiver receiver;

	@BeforeEach
	public static void initConfig() throws IOException {
		CONFIG = new DomoticzConfiguration(0, new URL("http://localhost/"));
		receiver = new DomoticzEventReceiver(CONFIG);
	}

	@AfterEach
	public void closeReceiver() {
		if (receiver != null) {
			receiver.stop();
			receiver = null;
		}
	}

	@Test
	public void create_no_exception() throws IOException {

	}

	@Test
	public void create_port_chosen() throws IOException {
		final int port = receiver.getListeningPort();

		assertNotEquals(0, port);
	}

	private static class TestEventListener implements EventReceived {

		private Optional<Integer> id = Optional.empty();
		private Optional<String> newState = Optional.empty();

		@Override
		public boolean deviceChanged(final int id, final String newState) {
			this.id = Optional.of(id);
			this.newState = Optional.ofNullable(newState);
			return true;
		}

		public Optional<Integer> getId() {
			return id;
		}

		public Optional<String> getNewState() {
			return newState;
		}

	}

	@Test
	public void parse_input() throws IOException {
		final TestEventListener listener = new TestEventListener();
		receiver.setEventListener(listener);
		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/1234/hello/");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode());

		assertTrue(listener.getId().isPresent());
		assertTrue(listener.getNewState().isPresent());
		assertEquals(1234, (int) listener.getId().get());
		assertEquals("hello", listener.getNewState().get());
	}

	@Test
	public void parse_input_trailingSlash() throws IOException {
		final TestEventListener listener = new TestEventListener();
		receiver.setEventListener(listener);
		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/1234/hello/");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode());

		assertTrue(listener.getId().isPresent());
		assertTrue(listener.getNewState().isPresent());
		assertEquals(1234, (int) listener.getId().get());
		assertEquals("hello", listener.getNewState().get());
	}

	@Test
	public void wrong_url_404() throws IOException {
		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/doesnotexist");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(404, con.getResponseCode());
	}

	@Test
	public void noListener_noException() throws IOException {
		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/1234/test");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode());
	}

	@Test
	public void unsupportedMethod_returns405() throws IOException {
		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/1234/test");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("PUT");

		assertEquals(405, con.getResponseCode());
	}

	private static class ThrowingEventListener implements EventReceived {

		@Override
		public boolean deviceChanged(final int idx, final String newState) {
			throw new UnsupportedOperationException();
		}

	}

	@Test
	public void listener_throwsException_internalServerError() throws IOException {
		receiver.setEventListener(new ThrowingEventListener());
		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/1234/hello/");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(500, con.getResponseCode());
	}

	private static class ReturningFalseEventListener implements EventReceived {

		@Override
		public boolean deviceChanged(final int idx, final String newState) {
			return false;
		}

	}

	@Test
	public void listenerReturnsFalse_errorCode404() throws IOException {
		receiver.setEventListener(new ReturningFalseEventListener());
		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/1234/hello/");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(404, con.getResponseCode());
	}
}
