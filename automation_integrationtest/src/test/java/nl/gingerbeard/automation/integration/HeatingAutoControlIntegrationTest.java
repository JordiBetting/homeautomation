package nl.gingerbeard.automation.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;

import fi.iki.elonen.NanoHTTPD.Response.Status;
import nl.gingerbeard.automation.Room;
import nl.gingerbeard.automation.autocontrol.HeatingAutoControl;
import nl.gingerbeard.automation.devices.DoorSensor;
import nl.gingerbeard.automation.devices.Thermostat;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration.DomoticzInitBehaviorConfig;
import nl.gingerbeard.automation.logging.TestLogger.LogOutputToTestLogger;
import nl.gingerbeard.automation.state.Temperature;

public class HeatingAutoControlIntegrationTest extends IntegrationTest {

	public static class TestRoom extends Room {
		
		private final Thermostat thermostat = new Thermostat(2,1);
		private final DoorSensor doorSensor = new DoorSensor(3);
		
		public TestRoom() {
			HeatingAutoControl autoControl = new HeatingAutoControl();
			autoControl.addThermostat(thermostat);
			autoControl.addPauseDevice(doorSensor);
			
			// Proves NPE bug (using context before initialized, now refactored to prevent)
			autoControl.setDaytimeTemperature(Temperature.celcius(18.5));
			
			assertTrue(super.getAutoControls().isEmpty());
			addAutoControl(autoControl);
			assertFalse(super.getAutoControls().isEmpty());
		}
	}
	
	@Override
	DomoticzConfiguration createConfig() throws MalformedURLException {
		DomoticzConfiguration config = super.createConfig();
		config.setInitConfiguration(new DomoticzInitBehaviorConfig());
		return config;
	}
	
	@Test
	public void theTest() throws IOException, InterruptedException {
		start(TestRoom.class);
		
		updateAlarm("disarmed");
		assertHeatingOn();
		
		deviceChanged(3, "Open");
		assertHeatingOff();
		
		deviceChanged(3, "Closed");
		assertHeatingOn();
		
		updateAlarm("arm_away");
		assertHeatingOff();
	}
	
	@Override
	protected void initWebserver() {
		//non default values
		webserver_nightTime();
		webserver_armedAway();
		webserver_pauseDevice();
		webserver_temperature();
	}

	private void webserver_pauseDevice() {
		String response = "{ \"ActTime\" : 1577191853, \"AstrTwilightEnd\" : \"18:38\", \"AstrTwilightStart\" : \"06:40\", \"CivTwilightEnd\" : \"17:14\", \"CivTwilightStart\" : \"08:04\", \"DayLength\" : \"07:49\", \"NautTwilightEnd\" : \"17:58\", \"NautTwilightStart\" : \"07:21\", \"ServerTime\" : \"2019-12-24 13:50:53\", \"SunAtSouth\" : \"12:05\", \"Sunrise\" : \"08:45\", \"Sunset\" : \"16:34\", \"app_version\" : \"4.10364\", \"result\" : [ { \"AddjMulti\" : 1.0, \"AddjMulti2\" : 1.0, \"AddjValue\" : 0.0, \"AddjValue2\" : 0.0, \"BatteryLevel\" : 255, \"CustomImage\" : 0, \"Data\" : \"On\", \"Description\" : \"\", \"DimmerType\" : \"none\", \"Favorite\" : 1, \"HardwareID\" : 2, \"HardwareName\" : \"zwave\", \"HardwareType\" : \"OpenZWave USB\", \"HardwareTypeVal\" : 21, \"HaveDimmer\" : true, \"HaveGroupCmd\" : true, \"HaveTimeout\" : true, \"ID\" : \"00000A01\", \"Image\" : \"Light\", \"IsSubDevice\" : false, \"LastUpdate\" : \"2019-12-23 16:21:50\", \"Level\" : 0, \"LevelInt\" : 0, \"MaxDimLevel\" : 100, \"Name\" : \"light-houtlamp\", \"Notifications\" : \"false\", \"PlanID\" : \"0\", \"PlanIDs\" : [ 0 ], \"Protected\" : false, \"ShowNotifications\" : true, \"SignalLevel\" : \"-\", \"Status\" : \"Closed\", \"StrParam1\" : \"\", \"StrParam2\" : \"\", \"SubType\" : \"Switch\", \"SwitchType\" : \"On/Off\", \"SwitchTypeVal\" : 0, \"Timers\" : \"false\", \"Type\" : \"Light/Switch\", \"TypeImg\" : \"lightbulb\", \"Unit\" : 1, \"Used\" : 1, \"UsedByCamera\" : false, \"XOffset\" : \"0\", \"YOffset\" : \"0\", \"idx\" : \"204\" } ], \"status\" : \"OK\", \"title\" : \"Devices\"}";
		webserver.setResponse("/json.htm?type=devices&rid=3", Status.OK, response);
	}
	
	private void webserver_temperature() {
		String response = "{ \"ActTime\" : 1577191853, \"AstrTwilightEnd\" : \"18:38\", \"AstrTwilightStart\" : \"06:40\", \"CivTwilightEnd\" : \"17:14\", \"CivTwilightStart\" : \"08:04\", \"DayLength\" : \"07:49\", \"NautTwilightEnd\" : \"17:58\", \"NautTwilightStart\" : \"07:21\", \"ServerTime\" : \"2019-12-24 13:50:53\", \"SunAtSouth\" : \"12:05\", \"Sunrise\" : \"08:45\", \"Sunset\" : \"16:34\", \"app_version\" : \"4.10364\", \"result\" : [ { \"AddjMulti\" : 1.0, \"AddjMulti2\" : 1.0, \"AddjValue\" : 0.0, \"AddjValue2\" : 0.0, \"BatteryLevel\" : 255, \"CustomImage\" : 0, \"Data\" : \"On\", \"Description\" : \"\", \"DimmerType\" : \"none\", \"Favorite\" : 1, \"HardwareID\" : 2, \"HardwareName\" : \"zwave\", \"HardwareType\" : \"OpenZWave USB\", \"HardwareTypeVal\" : 21, \"HaveDimmer\" : true, \"HaveGroupCmd\" : true, \"HaveTimeout\" : true, \"ID\" : \"00000A01\", \"Image\" : \"Light\", \"IsSubDevice\" : false, \"LastUpdate\" : \"2019-12-23 16:21:50\", \"Level\" : 0, \"LevelInt\" : 0, \"MaxDimLevel\" : 100, \"Name\" : \"light-houtlamp\", \"Notifications\" : \"false\", \"PlanID\" : \"0\", \"PlanIDs\" : [ 0 ], \"Protected\" : false, \"ShowNotifications\" : true, \"SignalLevel\" : \"-\", \"Status\" : \"17.0\", \"StrParam1\" : \"\", \"StrParam2\" : \"\", \"SubType\" : \"Switch\", \"SwitchType\" : \"On/Off\", \"SwitchTypeVal\" : 0, \"Timers\" : \"false\", \"Type\" : \"Light/Switch\", \"TypeImg\" : \"lightbulb\", \"Unit\" : 1, \"Used\" : 1, \"UsedByCamera\" : false, \"XOffset\" : \"0\", \"YOffset\" : \"0\", \"idx\" : \"204\" } ], \"status\" : \"OK\", \"title\" : \"Devices\"}";
		webserver.setResponse("/json.htm?type=devices&rid=2", Status.OK, response);
	}
	
	private void assertHeatingOn() {
		assertEquals(2, webserver.getRequests().size());
		assertEquals("GET /json.htm?type=setused&idx=2&tmode=2&protected=false&used=true", webserver.getRequests().get(0));
		assertEquals("GET /json.htm?type=setused&idx=1&setpoint=20.0&protected=false&used=true", webserver.getRequests().get(1));
		webserver.getRequests().clear();
	}

	private void assertHeatingOff() {
		assertEquals(2, webserver.getRequests().size());
		assertEquals("GET /json.htm?type=setused&idx=2&tmode=2&protected=false&used=true", webserver.getRequests().get(0));
		assertEquals("GET /json.htm?type=setused&idx=1&setpoint=15.0&protected=false&used=true", webserver.getRequests().get(1));
		webserver.getRequests().clear();
	}


	private void webserver_armedAway() {
		String response = "{ \"secondelay\" : 12, \"secstatus\" : 2, \"status\" : \"OK\", \"title\" : \"GetSecStatus\"}";
		webserver.setResponse("/json.htm?type=command&param=getsecstatus", Status.OK, response);
	}

	private void webserver_nightTime() {
		final int sunrise = 100;
		final int currentTime = 300;
		final int sunset = 200;
		webserver.setResponse("/json.htm?type=command&param=getSunRiseSet", Status.OK,
				createSunRiseSetResponse(sunrise, sunset, currentTime, sunrise + 10, sunset + 10));
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
}
