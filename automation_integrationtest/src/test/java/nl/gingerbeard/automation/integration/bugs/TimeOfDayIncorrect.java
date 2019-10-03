package nl.gingerbeard.automation.integration.bugs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.junit.After;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.Room;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.integration.IntegrationTest;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.TimeOfDay;

public class TimeOfDayIncorrect  extends IntegrationTest{

	public static class TimeAndAlarmRoom extends Room {

		public Optional<TimeOfDay> timeOfDayDuringAlarmTrigger = Optional.empty();

		public TimeAndAlarmRoom() {
		}

		@Subscribe
		public void updatedTime(final TimeOfDay tod) {
		}

		@Subscribe
		public void updatedAlarm(final AlarmState alarm) {
			timeOfDayDuringAlarmTrigger = Optional.ofNullable(getState().getTimeOfDay());
		}
	}
	
	@After
	public void logOut() {
		logOutput.printAll();
	}
	
	@Test
	public void proveTimeOfDayIncorrect() throws IOException {
		//create precondition
		setNightTime();
		updateAlarm("arm_away");
		
		TimeAndAlarmRoom room = automation.addRoom(TimeAndAlarmRoom.class);
		updateAlarm("disarmed");
		
		assertTrue(room.timeOfDayDuringAlarmTrigger.isPresent());
		assertEquals(TimeOfDay.NIGHTTIME, room.timeOfDayDuringAlarmTrigger.get());
	}
	
}
