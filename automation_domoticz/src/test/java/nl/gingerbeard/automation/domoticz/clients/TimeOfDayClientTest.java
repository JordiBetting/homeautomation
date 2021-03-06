package nl.gingerbeard.automation.domoticz.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fi.iki.elonen.NanoHTTPD.Response.Status;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.TestLogger;
import nl.gingerbeard.automation.state.TimeOfDayValues;
import nl.gingerbeard.automation.testutils.TestWebServer;

public class TimeOfDayClientTest {

	private static final String DOMOTICZ_URL = "/json.htm?type=command&param=getSunRiseSet";
	private TimeOfDayClient client;
	private TestWebServer webserver;

	@BeforeEach
	public void createClientAndWebserver() throws IOException {
		final DomoticzConfiguration config = setUp();
		createClient(config);
	}

	private void createClient(final DomoticzConfiguration config) throws IOException {
		client = new TimeOfDayClient(config, new TestLogger());
	}

	private DomoticzConfiguration setUp() throws IOException, MalformedURLException {
		webserver = new TestWebServer();
		webserver.start();
		final DomoticzConfiguration config = new DomoticzConfiguration(0,
				new URL("http://localhost:" + webserver.getListeningPort()));
		return config;
	}

	@Test
	public void goodweather() throws IOException {
		final int sunrise = 100;
		final int currentTime = 150;
		final int sunset = 200;
		webserver.setResponse(DOMOTICZ_URL, Status.OK,
				createSunRiseSetResponse(sunrise, sunset, currentTime, sunrise + 10, sunset + 10));

		final TimeOfDayValues timeOfDayValues = client.createTimeOfDayValues();

		assertEquals(currentTime, timeOfDayValues.getCurtime());
		assertEquals(sunrise, timeOfDayValues.getSunrise());
		assertEquals(sunset, timeOfDayValues.getSunset());
		assertEquals(sunrise + 10, timeOfDayValues.getCivilTwilightStart());
		assertEquals(sunset + 10, timeOfDayValues.getCivilTwilightEnd());
		assertEquals(currentTime, timeOfDayValues.getCurtime());
	}

	private String createSunRiseSetResponse(final int sunrise, final int sunset, final int currentTime,
			final int civilStart, final int civilEnd) {
		return String.format("{ " //
				+ "\"AstrTwilightEnd\" : \"19:51\", " //
				+ "\"AstrTwilightStart\" : \"05:56\", " //
				+ "\"CivTwilightEnd\" : \"%d:%d\", " /// FILLED
				+ "\"CivTwilightStart\" : \"%d:%d\", " // FILLED
				+ "\"DayLength\" : \"10:11\"," //
				+ "\"NautTwilightEnd\" : \"19:13\"," //
				+ "\"NautTwilightStart\" : \"06:35\"," //
				+ "\"ServerTime\" : \"2019-02-18 %d:%d\"," // FILLED
				+ "\"SunAtSouth\" : \"12:05\"," //
				+ "\"Sunrise\" : \"%d:%d\"," // FILLED
				+ "\"Sunset\" : \"%d:%d\"," // FILLED
				+ "\"status\" : \"OK\"," //
				+ "\"title\" : \"getSunRiseSet\"" //
				+ "}", //
				civilEnd / 60, //
				civilEnd % 60, //
				civilStart / 60, //
				civilStart % 60, //
				currentTime / 60, //
				currentTime % 60, //
				sunrise / 60, //
				sunrise % 60, //
				sunset / 60, //
				sunset % 60);
	}

	@Test
	public void invalidTime_throwsException() {
		webserver.setResponse(DOMOTICZ_URL, Status.OK, createSunRiseSunSet_invalidTime());

		final IOException e = assertThrows(IOException.class, () -> client.createTimeOfDayValues());

		assertEquals("Invalid input, could not find single : in 2040", e.getMessage());
	}

	@Test
	public void serverReturns404_throwsException() {
		webserver.setResponse(DOMOTICZ_URL, Status.NOT_FOUND, "woops");

		final IOException e = assertThrows(IOException.class, () -> client.createTimeOfDayValues());
		assertTrue(e.getMessage().startsWith("responsecode expected 200, but was: 404"));
	}

	@Test
	public void invalidNumber_throwsException() {
		webserver.setResponse(DOMOTICZ_URL, Status.OK, createSunRiseSunSet_invalidNumber());

		final IOException e = assertThrows(IOException.class, () -> client.createTimeOfDayValues());

		assertEquals("java.lang.NumberFormatException: For input string: \"WRONG\"", e.getMessage());
	}
	
	@Test
	public void invalidServerTime_throwsException() {
		webserver.setResponse(DOMOTICZ_URL, Status.OK, createSunRiseSunSet_invalidServerTime());
		
		final IOException e = assertThrows(IOException.class, () -> client.createTimeOfDayValues());

		assertEquals("Invalid input, expected hh:mm:ss in 2019-02-18___19:46:13", e.getMessage());
	}

	private String createSunRiseSunSet_invalidTime() {
		return "{ " //
				+ "\"AstrTwilightEnd\" : \"19:51\", " //
				+ "\"AstrTwilightStart\" : \"05:56\", " //
				+ "\"CivTwilightEnd\" : \"20:15\", " //
				+ "\"CivTwilightStart\" : \"2040\", " //
				+ "\"DayLength\" : \"10:11\"," //
				+ "\"NautTwilightEnd\" : \"19:13\"," //
				+ "\"NautTwilightStart\" : \"06:35\"," //
				+ "\"ServerTime\" : \"2019-02-18 19:46:13\"," //
				+ "\"SunAtSouth\" : \"12:05\"," //
				+ "\"Sunrise\" : \"07:48\"," //
				+ "\"Sunset\" : \"17:59\"," //
				+ "\"status\" : \"OK\"," //
				+ "\"title\" : \"getSunRiseSet\"" //
				+ "}";
	}

	private String createSunRiseSunSet_invalidNumber() {
		return "{ " //
				+ "\"AstrTwilightEnd\" : \"19:51\", " //
				+ "\"AstrTwilightStart\" : \"05:56\", " //
				+ "\"CivTwilightEnd\" : \"20:WRONG\", " //
				+ "\"CivTwilightStart\" : \"20:40\", " //
				+ "\"DayLength\" : \"10:11\"," //
				+ "\"NautTwilightEnd\" : \"19:13\"," //
				+ "\"NautTwilightStart\" : \"06:35\"," //
				+ "\"ServerTime\" : \"2019-02-18 19:46:13\"," //
				+ "\"SunAtSouth\" : \"12:05\"," //
				+ "\"Sunrise\" : \"07:48\"," //
				+ "\"Sunset\" : \"17:59\"," //
				+ "\"status\" : \"OK\"," //
				+ "\"title\" : \"getSunRiseSet\"" //
				+ "}";
	}


	private String createSunRiseSunSet_invalidServerTime() {
		return "{ " //
				+ "\"AstrTwilightEnd\" : \"19:51\", " //
				+ "\"AstrTwilightStart\" : \"05:56\", " //
				+ "\"CivTwilightEnd\" : \"20:14\", " //
				+ "\"CivTwilightStart\" : \"20:40\", " //
				+ "\"DayLength\" : \"10:11\"," //
				+ "\"NautTwilightEnd\" : \"19:13\"," //
				+ "\"NautTwilightStart\" : \"06:35\"," //
				+ "\"ServerTime\" : \"2019-02-18___19:46:13\"," //
				+ "\"SunAtSouth\" : \"12:05\"," //
				+ "\"Sunrise\" : \"07:48\"," //
				+ "\"Sunset\" : \"17:59\"," //
				+ "\"status\" : \"OK\"," //
				+ "\"title\" : \"getSunRiseSet\"" //
				+ "}";
	}

	@Test
	public void usesAuthorizationHeader() throws IOException {
		final DomoticzConfiguration config = setUp();
		config.setCredentials("pepernoten", "lekker");
		createClient(config);
		webserver.setResponse(DOMOTICZ_URL, Status.OK, createSunRiseSetResponse(1, 2, 3, 4, 5));
		
		client.createTimeOfDayValues();
		
		assertEquals(1, webserver.getRequestHeaders().size());
		assertEquals("Basic cGVwZXJub3RlbjpsZWtrZXI=", webserver.getRequestHeaders().get(0).get("authorization"));
	}
	
}
