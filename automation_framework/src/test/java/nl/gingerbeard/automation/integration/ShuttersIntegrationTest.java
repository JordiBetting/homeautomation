package nl.gingerbeard.automation.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.Room;
import nl.gingerbeard.automation.devices.Shutters;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OpenCloseState;

public class ShuttersIntegrationTest extends IntegrationTest {

	public static class ShuttersRoom extends Room {

		private final Shutters shutters = new Shutters(1);
		private final Switch sensor = new Switch(2);

		public ShuttersRoom() {
			addDevice(shutters).and(sensor);
		}

		@Subscribe
		public NextState<OpenCloseState> closeShutters(final Switch anySwitch) {
			return new NextState<>(shutters, OpenCloseState.CLOSE);
		}

	}

	@Test
	public void shutters_integration() throws IOException {
		automation.addRoom(new ShuttersRoom());

		deviceChanged(2, "off");

		assertEquals(1, webserver.getRequests().size());
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=1&switchcmd=Off&level=0", webserver.getRequests().get(0));

	}
}
