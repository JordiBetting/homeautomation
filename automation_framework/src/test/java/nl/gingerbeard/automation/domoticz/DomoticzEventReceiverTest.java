package nl.gingerbeard.automation.domoticz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.domoticz.DomoticzEventReceiver.EventReceived;

public class DomoticzEventReceiverTest {

	private DomoticzEventReceiver receiver;

	@AfterEach
	public void closeReceiver() {
		if (receiver != null) {
			receiver.stop();
			receiver = null;
		}
	}

	@Test
	public void create_no_exception() throws IOException {
		receiver = new DomoticzEventReceiver(0);
	}

	@Test
	public void create_port_chosen() throws IOException {
		receiver = new DomoticzEventReceiver(0);
		final int port = receiver.getListeningPort();

		assertNotEquals(0, port);
	}

	private static class TestEventListener implements EventReceived {

		private Optional<Integer> id = Optional.empty();
		private Optional<String> newState = Optional.empty();

		@Override
		public void deviceChanged(final int id, final String newState) {
			this.id = Optional.of(id);
			this.newState = Optional.ofNullable(newState);
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
		receiver = new DomoticzEventReceiver(0);
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
		receiver = new DomoticzEventReceiver(0);
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
		receiver = new DomoticzEventReceiver(0);
		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/doesnotexist");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(404, con.getResponseCode());
	}

	@Test
	public void noListener_noException() throws IOException {
		receiver = new DomoticzEventReceiver(0);
		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/1234/test");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode());
	}

	@Test
	public void unsupportedMethod_returns405() throws IOException {
		receiver = new DomoticzEventReceiver(0);
		final int port = receiver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/1234/test");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("PUT");

		assertEquals(405, con.getResponseCode());
	}

	@Test
	public void defaultPort_8080() throws IOException {
		receiver = new DomoticzEventReceiver();
		final int port = receiver.getListeningPort();

		assertEquals(8080, port);
	}
}
