package nl.gingerbeard.automation.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.Room;
import nl.gingerbeard.automation.autocontrol.HeatingAutoControl;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.devices.Thermostat;
import nl.gingerbeard.automation.state.Temperature;

public class HeatingAutoControlIntegrationTest extends IntegrationTest {

	public static class TestRoom extends Room {
		
		private final Thermostat thermostat = new Thermostat(2,1);
		private final Switch doorSensor = new Switch(3);
		
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
	
	@Test
	public void theTest() throws IOException, InterruptedException {
		start(TestRoom.class);
		updateAlarm("arm_away");
		setNightTime();
		assertEquals(0, webserver.getRequests().size());

		updateAlarm("disarmed");
		assertHeatingOn();
		
		deviceChanged(3, "on");
		assertHeatingOff();
		
		deviceChanged(3, "off");
		assertHeatingOn();
		
		updateAlarm("arm_away");
		assertHeatingOff();
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
	
}
