package nl.gingerbeard.automation.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.google.common.io.CharStreams;

import fi.iki.elonen.NanoHTTPD.Response.Status;
import nl.gingerbeard.automation.AutomationFrameworkContainer;
import nl.gingerbeard.automation.IAutomationFrameworkInterface;
import nl.gingerbeard.automation.configuration.ConfigurationServerSettings;
import nl.gingerbeard.automation.domoticz.DomoticzThreadHandler;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.TestLogger.LogOutputToTestLogger;
import nl.gingerbeard.automation.testutils.TestWebServer;

public abstract class IntegrationTest {

	private int port;
	private DomoticzConfiguration config;
	private AutomationFrameworkContainer container;
	protected TestWebServer webserver;
	protected IAutomationFrameworkInterface automation;
	protected int configPort;

	@BeforeEach
	public void start() throws IOException {
		webserver = new TestWebServer();
		webserver.start();

		config = new DomoticzConfiguration(0, new URL("http://localhost:" + webserver.getListeningPort()));
		final ConfigurationServerSettings configSettings = new ConfigurationServerSettings(0);
		container = IAutomationFrameworkInterface.createFrameworkContainer(config, new LogOutputToTestLogger(), configSettings);
		container.start();

		port = config.getListenPort();
		configPort = configSettings.getListenPort();
		automation = container.getRuntime().getService(IAutomationFrameworkInterface.class).get();
		final Optional<DomoticzThreadHandler> threadHandler = container.getRuntime().getService(DomoticzThreadHandler.class);
		assertTrue(threadHandler.isPresent());
		threadHandler.get().setSynchronous();
	}

	@AfterEach
	public void stop() {
		container.stop();
		container = null;

		webserver.stop();
		webserver = null;

		automation = null;
		port = 0;
		config = null;
	}

	protected void deviceChanged(final int idx, final String state) throws IOException {
		final URL url = new URL("http://localhost:" + port + "/device/" + idx + "/" + state);
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		assertEquals(200, con.getResponseCode(), "Status expected: 200 but was: " + con.getResponseCode() + ". Content: " + con.getContent());
	}

	private void sendRequest(final int curTime, final int sunrise, final int sunset) throws IOException {
		final String uri = "/json.htm?type=command&param=getSunRiseSet";
		webserver.setResponse(uri, Status.OK, createSunRiseSetResponse(sunrise, sunset));

		final URL url = new URL("http://localhost:" + port + "/time/" + curTime + "/" + sunrise + "/" + sunset);
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		assertEquals(200, con.getResponseCode(), "Status expected: 200 but was: " + con.getResponseCode() + ". Content: " + con.getContent());

		webserver.forgetRequest("GET " + uri);
	}

	private String createSunRiseSetResponse(final int sunrise, final int sunset) {
		return String.format("{ " //
				+ "\"AstrTwilightEnd\" : \"19:51\", " //
				+ "\"AstrTwilightStart\" : \"05:56\", " //
				+ "\"CivTwilightEnd\" : \"%d:%d\", " /// FILLED
				+ "\"CivTwilightStart\" : \"%d:%d\", " // FILLED
				+ "\"DayLength\" : \"10:11\"," //
				+ "\"NautTwilightEnd\" : \"19:13\"," //
				+ "\"NautTwilightStart\" : \"06:35\"," //
				+ "\"ServerTime\" : \"2019-02-18 19:46:13\"," //
				+ "\"SunAtSouth\" : \"12:05\"," //
				+ "\"Sunrise\" : \"07:48\"," //
				+ "\"Sunset\" : \"17:59\"," //
				+ "\"status\" : \"OK\"," //
				+ "\"title\" : \"getSunRiseSet\"" //
				+ "}", //
				sunset / 60, //
				sunset % 60, //
				sunrise / 60, //
				sunrise % 60);
	}

	protected void setDaytime() throws IOException {
		sendRequest(150, 100, 200);
	}

	protected void setNightTime() throws IOException {
		sendRequest(10, 100, 200);
	}

	protected void updateAlarm(final String alarmCommand) throws IOException {
		updateAlarm(alarmCommand, 200);
	}

	protected void updateAlarm(final String alarmCommand, final int expectedHttpStatus) throws IOException {
		final URL url = new URL("http://localhost:" + port + "/alarm/" + alarmCommand);
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		assertEquals(expectedHttpStatus, con.getResponseCode(), "Status expected: " + expectedHttpStatus + " but was: " + con.getResponseCode());
	}

	public void disableRoom(final String room) throws IOException {
		changeRoom(room, "disable");
	}

	private void changeRoom(final String room, final String enableString) throws IOException {
		final URL url = new URL("http://localhost:" + configPort + "/room/" + room + "/" + enableString);
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		assertEquals(200, con.getResponseCode(), "Status expected: " + 200 + " but was: " + con.getResponseCode());
	}

	public void enableRoom(final String room) throws IOException {
		changeRoom(room, "enable");
	}

	public List<String> getRooms() throws IOException {
		final URL url = new URL("http://localhost:" + configPort + "/room");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode(), "Status expected: " + 200 + " but was: " + con.getResponseCode());

		final String body = read(con.getInputStream());
		final String[] rooms = body.split("\n");
		return Arrays.stream(rooms).filter((roomLine) -> roomLine.length() > 1).map((roomLine) -> roomLine.split(",")[0]).collect(Collectors.toList());
	}

	private String read(final InputStream is) throws IOException {
		if (is != null) {
			try (InputStreamReader reader = new InputStreamReader(is, Charset.defaultCharset())) {
				return CharStreams.toString(reader);
			}
		}
		return "";
	}

	public boolean isEnabled(final String room) throws IOException {
		final URL url = new URL("http://localhost:" + configPort + "/room/" + room + "/isenabled");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode(), "Status expected: " + 200 + " but was: " + con.getResponseCode());

		return "true".equals(read(con.getInputStream()));
	}
}
