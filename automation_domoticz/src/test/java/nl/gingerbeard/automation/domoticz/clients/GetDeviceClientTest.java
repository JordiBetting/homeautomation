package nl.gingerbeard.automation.domoticz.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fi.iki.elonen.NanoHTTPD.Response.Status;
import nl.gingerbeard.automation.domoticz.clients.json.DeviceJSON;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.TestLogger;
import nl.gingerbeard.automation.testutils.TestWebServer;

public class GetDeviceClientTest {
	private static final String DOMOTICZ_URL = "/json.htm?type=devices&rid=%d";
	private GetDeviceClient client;
	private TestWebServer webserver;

	@BeforeEach
	public void createClientAndWebserver() throws IOException {
		final DomoticzConfiguration config = setUp();
		createClient(config);
	}

	private DomoticzConfiguration setUp() throws IOException, MalformedURLException {
		createWebserver();
		final DomoticzConfiguration config = createDomoticzConfig();
		return config;
	}

	private void createClient(final DomoticzConfiguration config) throws IOException {
		client = new GetDeviceClient(config, new TestLogger());
	}

	private DomoticzConfiguration createDomoticzConfig() throws MalformedURLException {
		final DomoticzConfiguration config = new DomoticzConfiguration(0,
				new URL("http://localhost:" + webserver.getListeningPort()));
		return config;
	}

	private void createWebserver() throws IOException {
		webserver = new TestWebServer();
		webserver.start();
	}

	@Test
	public void goodWeather() throws IOException {
		webserver.setResponse(String.format(DOMOTICZ_URL, 42), Status.OK, response);
		
		DeviceJSON details = client.getDeviceDetails(42);
		
		assertEquals("OK", details.status);
		assertNotNull(details.result);
		assertEquals(1, details.result.length);
		assertNotNull(details.result[0]);
		assertEquals("blaat", details.result[0].status);
		assertEquals("3", details.result[0].level);
		assertEquals(3, details.result[0].levelInt);
	}
	
	private String response = "{ \"ActTime\" : 1577191853, \"AstrTwilightEnd\" : \"18:38\", \"AstrTwilightStart\" : \"06:40\", \"CivTwilightEnd\" : \"17:14\", \"CivTwilightStart\" : \"08:04\", \"DayLength\" : \"07:49\", \"NautTwilightEnd\" : \"17:58\", \"NautTwilightStart\" : \"07:21\", \"ServerTime\" : \"2019-12-24 13:50:53\", \"SunAtSouth\" : \"12:05\", \"Sunrise\" : \"08:45\", \"Sunset\" : \"16:34\", \"app_version\" : \"4.10364\", \"result\" : [ { \"AddjMulti\" : 1.0, \"AddjMulti2\" : 1.0, \"AddjValue\" : 0.0, \"AddjValue2\" : 0.0, \"BatteryLevel\" : 255, \"CustomImage\" : 0, \"Data\" : \"On\", \"Description\" : \"\", \"DimmerType\" : \"none\", \"Favorite\" : 0, \"HardwareID\" : 4, \"HardwareName\" : \"Hue\", \"HardwareType\" : \"Philips Hue Bridge\", \"HardwareTypeVal\" : 38, \"HaveDimmer\" : true, \"HaveGroupCmd\" : false, \"HaveTimeout\" : false, \"ID\" : \"00000801\", \"Image\" : \"Push\", \"InternalState\" : \"On\", \"IsSubDevice\" : false, \"LastUpdate\" : \"2019-12-23 17:15:00\", \"Level\" : 3, \"LevelInt\" : 3, \"MaxDimLevel\" : 100, \"Name\" : \"scene-backyard-home\", \"Notifications\" : \"false\", \"PlanID\" : \"2\", \"PlanIDs\" : [ 2 ], \"Protected\" : false, \"ShowNotifications\" : true, \"SignalLevel\" : \"-\", \"Status\" : \"blaat\", \"StrParam1\" : \"\", \"StrParam2\" : \"\", \"SubType\" : \"RGBW\", \"SwitchType\" : \"Push On Button\", \"SwitchTypeVal\" : 9, \"Timers\" : \"false\", \"Type\" : \"Color Switch\", \"TypeImg\" : \"push\", \"Unit\" : 1, \"Used\" : 1, \"UsedByCamera\" : false, \"XOffset\" : \"830\", \"YOffset\" : \"114\", \"idx\" : \"141\" } ], \"status\" : \"OK\", \"title\" : \"Devices\"}";
}
