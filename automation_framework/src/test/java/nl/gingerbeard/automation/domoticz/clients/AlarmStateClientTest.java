package nl.gingerbeard.automation.domoticz.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URL;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import fi.iki.elonen.NanoHTTPD.Response.Status;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.testutils.TestWebServer;

public class AlarmStateClientTest {

	private static final String DOMOTICZ_URL = "/json.htm?type=command&param=getsecstatus";
	private AlarmStateClient client;
	private TestWebServer webserver;

	@BeforeEach
	public void createClientAndWebserver() throws IOException {
		webserver = new TestWebServer();
		webserver.start();
		final DomoticzConfiguration config = new DomoticzConfiguration(0,
				new URL("http://localhost:" + webserver.getListeningPort()));

		client = new AlarmStateClient(config);
	}

	@ParameterizedTest(name = "secstatus={0}, ExpectedAlarmState={1}")
	@MethodSource("goodWeatherTests")
	public void testGoodWeather(int secstatus, AlarmState expected) throws IOException {
		webserver.setResponse(DOMOTICZ_URL, Status.OK, createJson(secstatus));
		
		AlarmState actual = client.getAlarmState();
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void invalidNumber_throwsException() {
		webserver.setResponse(DOMOTICZ_URL, Status.OK, createJson(42));
		
		IOException e = assertThrows(IOException.class, () -> client.getAlarmState());
		assertEquals("Unknown secstatus code 42 received", e.getMessage());
	}
	
	@Test
	public void serverReturns404_throwsException() {
		webserver.setResponse(DOMOTICZ_URL, Status.NOT_FOUND, "woops");

		final IOException e = assertThrows(IOException.class, () -> client.getAlarmState());
		assertEquals("responsecode expected 200, but was: 404", e.getMessage());
	}

	static Stream<Arguments> goodWeatherTests() {
		return Stream.of(//
				Arguments.of(0, AlarmState.DISARMED), //
				Arguments.of(1, AlarmState.ARM_HOME), //
				Arguments.of(2, AlarmState.ARM_AWAY) //
		);
	}
	
	private String createJson(int i) {
		return "{\r\n" + //
				"   \"secondelay\" : 12,\r\n" + //
				"   \"secstatus\" : " + i + ",\r\n" + //
				"   \"status\" : \"OK\",\r\n" + //
				"   \"title\" : \"GetSecStatus\"\r\n" + //
				"}";
	}

}
