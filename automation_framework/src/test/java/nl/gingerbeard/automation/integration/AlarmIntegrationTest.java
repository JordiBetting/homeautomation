package nl.gingerbeard.automation.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.Room;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.event.annotations.EventState;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public class AlarmIntegrationTest extends IntegrationTest {

	public static class AlarmRoom extends Room {

		private int away_count = 0;
		private int home_count = 0;
		private int disarmed_count = 0;

		AlarmRoom() {
			super();
		}

		@Subscribe
		public void receiveTime(final AlarmState alarm) {
			switch (alarm) {
			case ARM_AWAY:
				away_count++;
				break;
			case ARM_HOME:
				home_count++;
				break;
			case DISARMED:
				disarmed_count++;
				break;
			default:
				throw new RuntimeException();
			}
		}

		public int getAway_count() {
			return away_count;
		}

		public int getHome_count() {
			return home_count;
		}

		public int getDisarmed_count() {
			return disarmed_count;
		}
	}

	@Test
	public void sendAlarm() throws IOException {
		final AlarmRoom room = new AlarmRoom();
		automation.addRoom(room);

		assertEquals(0, room.getAway_count());
		assertEquals(0, room.getHome_count());
		assertEquals(0, room.getDisarmed_count());

		updateAlarm("arm_away");
		assertEquals(1, room.getAway_count());
		assertEquals(0, room.getHome_count());
		assertEquals(0, room.getDisarmed_count());

		updateAlarm("arm_home");
		assertEquals(1, room.getAway_count());
		assertEquals(1, room.getHome_count());
		assertEquals(0, room.getDisarmed_count());

		updateAlarm("disarmed");
		assertEquals(1, room.getAway_count());
		assertEquals(1, room.getHome_count());
		assertEquals(1, room.getDisarmed_count());

	}

	@EventState(alarmState = AlarmState.ARM_HOME)
	public static class AlarmSwitchRoom extends Room {

		private static final Switch SENSOR = new Switch(0);
		private int callCount = 0;

		AlarmSwitchRoom() {
			super();
			addDevice(SENSOR);
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
	public void switch_alarmstate_received() throws IOException {
		final AlarmSwitchRoom room = new AlarmSwitchRoom();
		automation.addRoom(room);

		// intial state = disarmed. No receiving stuff
		deviceChanged(0, "on");
		assertEquals(0, room.getCallCount());

		updateAlarm("arm_home");
		deviceChanged(0, "on");
		assertEquals(1, room.getCallCount());

		updateAlarm("arm_away");
		deviceChanged(0, "on");
		assertEquals(1, room.getCallCount());

		updateAlarm("disarmed");
		deviceChanged(0, "on");
		assertEquals(1, room.getCallCount());

		updateAlarm("arm_home");
		deviceChanged(0, "on");
		assertEquals(2, room.getCallCount());
	}

	@Test
	public void alarmState_invalid_404() throws IOException {
		updateAlarm("invalid", 404);
	}

	public static class AlarmWithDeviceOutput extends Room {
		private final Switch output = new Switch(1);

		public AlarmWithDeviceOutput() {
			addDevice(output);
		}

		@Subscribe
		public NextState<?> updateAlarm(final AlarmState alarmState) {
			return new NextState<>(output, OnOffState.ON);
		}
	}

	@Test
	public void alarmChange_triggersOutput() throws IOException {
		final AlarmWithDeviceOutput room = new AlarmWithDeviceOutput();
		automation.addRoom(room);

		updateAlarm("arm_away");

		final List<String> requests = webserver.getRequests();
		assertEquals(1, requests.size());
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=1&switchcmd=on", requests.get(0));
	}
}
