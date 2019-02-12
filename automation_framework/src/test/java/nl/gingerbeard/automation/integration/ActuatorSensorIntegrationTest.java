package nl.gingerbeard.automation.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.Room;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public class ActuatorSensorIntegrationTest extends IntegrationTest {
	public static class MyRoom extends Room {

		private static final Switch ACTUATOR = new Switch(1);
		private static final Switch SENSOR = new Switch(0);

		public MyRoom() {
			super();
			addDevice(SENSOR);
			addDevice(ACTUATOR);
		}

		@Subscribe
		public NextState<OnOffState> process(final Switch trigger) {
			if (trigger.getIdx() == SENSOR.getIdx()) {
				return new NextState<>(ACTUATOR, OnOffState.ON);
			} else if (trigger.getIdx() == ACTUATOR.getIdx()) {
				return new NextState<>(ACTUATOR, OnOffState.OFF);
			}
			return null;
		}
	}

	@Test
	public void actuatorUpdatedBySensorUpdate() throws IOException {
		automation.addRoom(new MyRoom());

		deviceChanged(0, "on");

		List<String> requests = webserver.getRequests();
		assertEquals(1, requests.size());
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=1&switchcmd=on", requests.get(0));

		// reply with device update 'in Domoticz'
		deviceChanged(1, "on");
		requests = webserver.getRequests();
		assertEquals(2, requests.size());
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=1&switchcmd=off", requests.get(1));

	}
}
