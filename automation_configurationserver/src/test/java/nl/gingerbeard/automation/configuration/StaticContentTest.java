package nl.gingerbeard.automation.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.io.CharStreams;

public class StaticContentTest {

	private ConfigurationServer server;
	private IConfigurationProvider providerMock;
	private int port;

	@BeforeEach
	public void create() throws IOException {
		final ConfigurationServerSettings settings = new ConfigurationServerSettings(0);
		providerMock = mock(IConfigurationProvider.class);
		server = new ConfigurationServer(settings, providerMock);
		port = settings.getListenPort();
	}

	@AfterEach
	public void stopServer() {
		if (server != null) {
			server.stop();
			server = null;
		}
	}

	@Test
	public void testStatic() throws IOException {
		final HttpURLConnection test = openConnection("test.txt");
		assertEquals(200, test.getResponseCode());
		final String testContent = read(test.getInputStream());

		assertEquals("Howdy!", testContent);
	}

	@Test
	public void testStaticNotFound() throws IOException {
		final HttpURLConnection test = openConnection("doesnotexist");

		assertEquals(404, test.getResponseCode());
	}

	@Test
	public void illegalAccess_gives404() throws IOException {
		final HttpURLConnection test = openConnection("../bin/main/nl/gingerbeard/automation/configuration/IConfigurationProvider.class");

		assertEquals(404, test.getResponseCode());
	}

	@Test
	public void configurationPageServed() throws IOException {
		final HttpURLConnection con = openConnection("configuration.html");

		assertTrue(read(con.getInputStream()).startsWith("<html>"));
	}

	private HttpURLConnection openConnection(final String path) throws IOException {
		final URL url = new URL("http://localhost:" + port + "/static/" + path);
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
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
