package nl.gingerbeard.automation.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.Room;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public class EnableDisableIntegrationTest extends IntegrationTest {
	public static class MyRoom extends Room {

		private static final Switch ACTUATOR = new Switch(1);
		private static final Switch SENSOR = new Switch(2);

		public MyRoom() {
			super();
			addDevice(SENSOR);
			addDevice(ACTUATOR);
		}

		@Subscribe
		public NextState<OnOffState> process(final Switch trigger) {
			return new NextState<>(ACTUATOR, trigger.getState());
		}
	}

	public static class MyRoom2 extends Room {

		@Subscribe
		public void whatever(final Object ignore) {
		}
	}

	@Test
	public void enableDisableIntegrationTest() throws IOException, InterruptedException {
		start(MyRoom.class, MyRoom2.class);
		final List<String> requests = webserver.getRequests();

		deviceChanged(2, "on");
		assertEquals(1, requests.size());

		disableRoom("MyRoom");
		deviceChanged(2, "off");
		assertEquals(1, requests.size());

		enableRoom("MyRoom");
		deviceChanged(2, "on");
		assertEquals(2, requests.size());

		final List<String> rooms = getRooms();
		assertEquals(2, rooms.size(), "Actual: " + rooms.toString());
		assertTrue(rooms.contains("MyRoom"));
		assertTrue(rooms.contains("MyRoom2"));
	}

	@Test
	public void isEnabled() throws IOException, InterruptedException {
		start(MyRoom.class, MyRoom2.class);

		disableRoom("MyRoom");

		assertFalse(isEnabled("MyRoom"));
		assertTrue(super.isEnabled("MyRoom2"));
	}
}
