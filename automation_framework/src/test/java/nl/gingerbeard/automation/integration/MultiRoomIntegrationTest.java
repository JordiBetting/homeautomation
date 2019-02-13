package nl.gingerbeard.automation.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.Room;
import nl.gingerbeard.automation.devices.DimmeableLight;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.event.EventResult;
import nl.gingerbeard.automation.event.annotations.EventState;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.Level;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.TimeOfDay;

public class MultiRoomIntegrationTest extends IntegrationTest {

	public static abstract class Room1 extends Room {
		protected final Switch input = new Switch(1);
		protected final DimmeableLight output = new DimmeableLight(2);

		public Room1() {
			addDevice(input).and(output);
		}

	}

	@EventState(timeOfDay = TimeOfDay.DAYTIME)
	public static class Room1Daytime extends Room1 {

		@Subscribe
		public EventResult setBright(final Switch changedDevice) {
			if (input.getIdx() == changedDevice.getIdx()) {
				return EventResult.of(new NextState<>(output, new Level(100)));
			}
			return EventResult.empty();
		}
	}

	@EventState(timeOfDay = TimeOfDay.NIGHTTIME)
	public static class Room1Nighttime extends Room1 {
		@Subscribe
		public EventResult setDimmed(final Switch changedDevice) {
			if (input.getIdx() == changedDevice.getIdx()) {
				return EventResult.of(new NextState<>(output, new Level(50)));
			}
			return EventResult.empty();
		}
	}

	public static class Room2 extends Room {
		private final Switch input = new Switch(1);
		private final Switch output = new Switch(3);

		public Room2() {
			addDevice(input).and(output);
		}

		@Subscribe
		public EventResult updateOutput(final Switch changedDevice) {
			if (input.getIdx() == changedDevice.getIdx()) {
				return EventResult.of(new NextState<>(output, changedDevice.getState()));
			}
			return EventResult.empty();
		}
	}

	@Test
	public void multiroom() throws IOException {
		final List<String> requests = webserver.getRequests();
		final Room1Daytime room1day = new Room1Daytime();
		final Room1Nighttime room1night = new Room1Nighttime();
		final Room2 room2 = new Room2();

		automation.addRooms(room1day, room1night, room2);

		setDaytime();
		deviceChanged(1, "ON");

		assertEquals(2, requests.size());
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=2&switchcmd=Set%20Level&level=100", requests.get(0));
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=3&switchcmd=On", requests.get(1));

		setNightTime();
		deviceChanged(1, "OFF");

		assertEquals(4, requests.size());
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=2&switchcmd=Set%20Level&level=50", requests.get(2));
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=3&switchcmd=Off", requests.get(3));

	}

}
