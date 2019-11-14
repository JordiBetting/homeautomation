package nl.gingerbeard.automation.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import fi.iki.elonen.NanoHTTPD.Response.Status;
import nl.gingerbeard.automation.AutomationFrameworkContainer;
import nl.gingerbeard.automation.IAutomationFrameworkInterface;
import nl.gingerbeard.automation.configuration.ConfigurationServerSettings;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.TestLogger.LogOutputToTestLogger;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.TimeOfDay;
import nl.gingerbeard.automation.testutils.TestWebServer;

public class StartupStateTest {


	@Test
	public void startDayArmAway() throws IOException {
		TestWebServer webserver = startWebserver();
		
		//configure webserver for testcase
		webserver.setResponse("/json.htm?type=command&param=getSunRiseSet", Status.OK, createSunRiseSetResponse(100, 200, 150, 80, 220));
		webserver.setResponse("/json.htm?type=command&param=getsecstatus", Status.OK, createSecstatusResponse(2));

		Optional<IState> state = startFramework(webserver);
		
		assertTrue(state.isPresent());
		assertEquals(AlarmState.ARM_AWAY, state.get().getAlarmState());
		assertEquals(TimeOfDay.DAYTIME, state.get().getTimeOfDay());
	}
	
	@Test
	public void startNighDisarmed() throws IOException {
		TestWebServer webserver = startWebserver();
		
		//configure webserver for testcase
		webserver.setResponse("/json.htm?type=command&param=getSunRiseSet", Status.OK, createSunRiseSetResponse(100, 200, 50, 80, 220));
		webserver.setResponse("/json.htm?type=command&param=getsecstatus", Status.OK, createSecstatusResponse(0));

		Optional<IState> state = startFramework(webserver);
		
		assertTrue(state.isPresent());
		assertEquals(AlarmState.DISARMED, state.get().getAlarmState());
		assertEquals(TimeOfDay.NIGHTTIME, state.get().getTimeOfDay());
	}

	
	private String createSecstatusResponse(int i) {
		return "{\r\n" + //
				"   \"secondelay\" : 12,\r\n" + //
				"   \"secstatus\" : " + i + ",\r\n" + //
				"   \"status\" : \"OK\",\r\n" + //
				"   \"title\" : \"GetSecStatus\"\r\n" + //
				"}";
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
	
	private TestWebServer startWebserver() throws IOException {
		TestWebServer webserver = new TestWebServer();
		webserver.start();
		return webserver;
	}
	private Optional<IState> startFramework(TestWebServer webserver) throws MalformedURLException {
		DomoticzConfiguration config = new DomoticzConfiguration(0, new URL("http://localhost:" + webserver.getListeningPort()));
		config.setEventHandlingSynchronous();
		AutomationFrameworkContainer container = IAutomationFrameworkInterface.createFrameworkContainer(config, new LogOutputToTestLogger(), new ConfigurationServerSettings(0));
		container.start();
		return container.getRuntime().getService(IState.class);
	}
	
}
