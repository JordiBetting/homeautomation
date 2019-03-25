package nl.gingerbeard.automation.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;

public class ConfigurationServerTest {

	private ConfigurationServer server;
	private IConfigurationProvider providerMock;
	private int port;

	public ConfigurationServerTest() throws IOException {
		// I'm getting sick of FindBugs. It complains that providerMock might be null.
		// FindBugs doesn't understand @BeforeEach apparently
		create();
	}

	@BeforeEach
	public void create() throws IOException {
		final ConfigurationServerSettings settings = new ConfigurationServerSettings(0);
		providerMock = mock(IConfigurationProvider.class);
		server = new ConfigurationServer(settings, providerMock);
		port = settings.getListenPort();
	}

	@AfterEach
	public void stop() {
		if (server != null) {
			server.stop();
			server = null;
		}
	}

	@Test
	public void listAllRooms() throws IOException {
		final HttpURLConnection con = openGETConnection("/room");
		when(providerMock.getRooms()).thenReturn(Lists.newArrayList("one", "two", "three"));

		assertEquals(200, con.getResponseCode());
		assertEquals("one,two,three", read(con.getInputStream()));
	}

	@Test
	public void unknownUrl_404() throws IOException {
		final HttpURLConnection con = openGETConnection("/unknown");

		assertEquals(404, con.getResponseCode());
	}

	@Test
	public void list_otherMethod_405() throws IOException {
		final HttpURLConnection con = openPOSTConnection("/room");

		assertEquals(405, con.getResponseCode());
	}

	@Test
	public void disableWorks() throws IOException {
		final HttpURLConnection con = openPOSTConnection("/room/living/disable");

		assertEquals(200, con.getResponseCode());
		verify(providerMock, times(1)).disable("living");
	}

	@Test
	public void disable_otherMethod_405() throws IOException {
		final HttpURLConnection con = openGETConnection("/room/living/disable");

		assertEquals(405, con.getResponseCode());
	}

	@Test
	public void enableWorks() throws IOException {
		final HttpURLConnection con = openPOSTConnection("/room/living/enable");

		assertEquals(200, con.getResponseCode());
		verify(providerMock, times(1)).enable("living");
	}

	@Test
	public void enable_otherMethod_405() throws IOException {
		final HttpURLConnection con = openGETConnection("/room/living/enable");

		assertEquals(405, con.getResponseCode());
	}

	@Test
	public void unsupportedRoomCommand_404() throws IOException {
		final HttpURLConnection con = openPOSTConnection("/rooms/living/notsupported");

		assertEquals(404, con.getResponseCode());
	}

	private HttpURLConnection openGETConnection(final String path) throws IOException {
		return openConnection(path, "GET");
	}

	private HttpURLConnection openPOSTConnection(final String path) throws IOException {
		return openConnection(path, "POST");
	}

	private HttpURLConnection openConnection(final String path, final String method) throws IOException {
		final URL url = new URL("http://localhost:" + port + path);
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod(method);
		return con;
	}

	private String read(final InputStream is) throws IOException {
		if (is != null) {
			try (InputStreamReader reader = new InputStreamReader(is, Charset.defaultCharset())) {
				return CharStreams.toString(reader);
			}
		}
		return "";
	}

}
