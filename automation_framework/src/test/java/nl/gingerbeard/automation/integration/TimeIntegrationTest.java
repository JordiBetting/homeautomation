package nl.gingerbeard.automation.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.Room;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.event.annotations.EventState;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.TimeOfDay;

public class TimeIntegrationTest extends IntegrationTest {

	@EventState(timeOfDay = TimeOfDay.DAYTIME)
	public static class DayTimeSwitchRoom extends Room {

		private static final Switch SENSOR = new Switch(0);
		private static final Switch ACTUATOR = new Switch(1);
		private int callCount = 0;

		DayTimeSwitchRoom() {
			super();
			addDevice(SENSOR);
			addDevice(ACTUATOR);
		}

		@Subscribe
		public void receiveEvent(final Switch input) {
			callCount++;
		}

		public int getCallCount() {
			return callCount;
		}
	}

	@Test
	public void switch_daytime_received() throws IOException {
		final DayTimeSwitchRoom room = new DayTimeSwitchRoom();
		automation.addRoom(room);

		// initial, no calls
		assertEquals(0, room.getCallCount());

		// daytime event should not be triggered on room
		setDaytime();
		assertEquals(0, room.getCallCount());

		// it is daytime, switch changed, expect to receive event
		deviceChanged(0, "on");
		assertEquals(1, room.getCallCount());

		// switch to nighttime, should not be triggered in room
		setNightTime();
		assertEquals(1, room.getCallCount());

		// it is nighttime, switch changed, expect no event
		deviceChanged(0, "off");
		assertEquals(1, room.getCallCount());
	}

	public static class TimeRoom extends Room {

		private static final Switch ACTUATOR = new Switch(1);
		private int nighttime_count = 0;
		private int daytime_count;

		TimeRoom() {
			super();
			addDevice(ACTUATOR);
		}

		@Subscribe
		public void receiveTime(final TimeOfDay timeofday) {
			switch (timeofday) {
			case DAYTIME:
				daytime_count++;
				break;
			case NIGHTTIME:
				nighttime_count++;
				break;
			default:
				throw new RuntimeException();
			}
		}

		public int getNighttime_count() {
			return nighttime_count;
		}

		public int getDaytime_count() {
			return daytime_count;
		}

	}

	@Test
	public void updateTime_eventReceived() throws IOException {
		final TimeRoom room = new TimeRoom();
		automation.addRoom(room);

		assertEquals(0, room.getDaytime_count());
		assertEquals(0, room.getNighttime_count());

		setNightTime();

		assertEquals(0, room.getDaytime_count());
		assertEquals(1, room.getNighttime_count());

		setDaytime();

		assertEquals(1, room.getDaytime_count());
		assertEquals(1, room.getNighttime_count());
	}
}
