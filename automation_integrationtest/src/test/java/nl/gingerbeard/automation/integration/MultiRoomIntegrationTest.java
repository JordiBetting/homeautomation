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
import nl.gingerbeard.automation.state.OnOffState;
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
	public void multiroom() throws IOException, InterruptedException {
		start(Room1Daytime.class, Room1Nighttime.class, Room2.class);
		final List<String> requests = webserver.getRequests();

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

	@Test
	public void multiroom_deviceSync() throws IOException, InterruptedException {
		start(Room1Daytime.class, Room1Nighttime.class);
		final Room1Daytime room1day = automation.getRoom(Room1Daytime.class);
		final Room1Nighttime room1night = automation.getRoom(Room1Nighttime.class);

		setDaytime();
		deviceChanged(1, "ON");

		assertEquals(OnOffState.ON, room1day.input.getState());
		assertEquals(OnOffState.ON, room1night.input.getState());

		deviceChanged(1, "OFF");

		assertEquals(OnOffState.OFF, room1day.input.getState());
		assertEquals(OnOffState.OFF, room1night.input.getState());
	}

}
