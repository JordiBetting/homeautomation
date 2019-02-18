package nl.gingerbeard.automation.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import fi.iki.elonen.NanoHTTPD.Response.Status;
import nl.gingerbeard.automation.AutomationFrameworkContainer;
import nl.gingerbeard.automation.IAutomationFrameworkInterface;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.helpers.TestWebServer;
import nl.gingerbeard.automation.logging.TestLogger.LogOutputToTestLogger;

public abstract class IntegrationTest {

	private int port;
	private DomoticzConfiguration config;
	private AutomationFrameworkContainer container;
	protected TestWebServer webserver;
	protected IAutomationFrameworkInterface automation;

	@BeforeEach
	public void start() throws IOException {
		webserver = new TestWebServer();
		webserver.start();

		config = new DomoticzConfiguration(0, new URL("http://localhost:" + webserver.getListeningPort()));
		container = IAutomationFrameworkInterface.createFrameworkContainer(config, new LogOutputToTestLogger());
		container.start();

		port = config.getListenPort();
		automation = container.getRuntime().getService(IAutomationFrameworkInterface.class).get();
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

}
