package nl.gingerbeard.automation.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import nl.gingerbeard.automation.logging.LogLevel;
import nl.gingerbeard.automation.logging.TestLogger;

public class StateTest {

	@ParameterizedTest
	@EnumSource(value = TimeOfDay.class, names = { "DAYTIME", "NIGHTTIME" })
	public void setTimeOfDay_valid_works(final TimeOfDay validTimeOfDay) {
		final IState state = new State();

		state.setTimeOfDay(validTimeOfDay);
		assertEquals(validTimeOfDay, state.getTimeOfDay());
	}

	@ParameterizedTest
	@EnumSource(value = TimeOfDay.class, names = { "ALLDAY", })
	public void setTimeOfDay_invalid_throwsException(final TimeOfDay invalidTimeOfDay) {
		final IState state = new State();

		assertThrows(IllegalArgumentException.class, () -> state.setTimeOfDay(invalidTimeOfDay));
	}

	@Test
	public void setTimeOfDay_null_throwsException() {
		final IState state = new State();

		assertThrows(IllegalArgumentException.class, () -> state.setTimeOfDay(null));
	}

	@ParameterizedTest
	@EnumSource(value = HomeAway.class, names = { "HOME", "AWAY" })
	public void setHomeAway_valid_works(final HomeAway validHomeAway) {
		final IState state = new State();

		state.setHomeAway(validHomeAway);
		assertEquals(validHomeAway, state.getHomeAway());
	}

	@ParameterizedTest
	@EnumSource(value = HomeAway.class, names = { "ALWAYS", })
	public void setHomeAway_invalid_throwsException(final HomeAway invalidHomeAway) {
		final IState state = new State();

		assertThrows(IllegalArgumentException.class, () -> state.setHomeAway(invalidHomeAway));
	}

	@Test
	public void setHomeAway_null_throwsException() {
		final IState state = new State();

		assertThrows(IllegalArgumentException.class, () -> state.setHomeAway(null));
	}

	@ParameterizedTest
	@EnumSource(value = AlarmState.class, names = { "DISARMED", "ARM_HOME", "ARM_AWAY" })
	public void setAlarmState_valid_works(final AlarmState validAlarmState) {
		final IState state = new State();

		state.setAlarmState(validAlarmState);
		assertEquals(validAlarmState, state.getAlarmState());
	}

	@ParameterizedTest
	@EnumSource(value = AlarmState.class, names = { "ALWAYS", "ARMED" })
	public void setAlarmState_invalid_throwsException(final AlarmState invalidAlarmState) {
		final IState state = new State();

		assertThrows(IllegalArgumentException.class, () -> state.setAlarmState(invalidAlarmState));
	}

	@Test
	public void setAlarmState_null_throwsException() {
		final IState state = new State();

		assertThrows(IllegalArgumentException.class, () -> state.setAlarmState(null));
	}

	@Test
	public void get_time_current() {
		final IState state = new State();
		final Time time = state.getTime();

		final LocalDateTime timestamp = time.getNow();
		final LocalDateTime curtime = LocalDateTime.now();

		assertTrue(Math.abs(curtime.compareTo(timestamp)) < 1000);
	}

	@Test
	public void override_time() {
		final IState state = new State();

		state.setTime(new Time(LocalDateTime.of(2019, 3, 2, 9, 5, 42)));

		final LocalDateTime now = state.getTime().getNow();

		assertEquals(LocalDateTime.of(2019, 3, 2, 9, 5, 42), now);
	}
	
	@Test
	public void getTimeOfDay_logs() {
		TestLogger logger = new TestLogger();
		IState state = new State(logger);
		state.setTimeOfDay(TimeOfDay.DAYTIME);
		logger.assertEmpty();
		
		state.getTimeOfDay();
		
		logger.assertContains(LogLevel.DEBUG, "getTimeOfDay() : DAYTIME");
	}
	
	@Test
	public void getAlarm_logs() {
		TestLogger logger = new TestLogger();
		IState state = new State(logger);
		state.setAlarmState(AlarmState.ARM_AWAY);
		logger.assertEmpty();
		
		state.getAlarmState();
		
		logger.assertContains(LogLevel.DEBUG, "getAlarmState() : ARM_AWAY");
	}
	
	@Test
	public void getHomeAway_logs() {
		TestLogger logger = new TestLogger();
		IState state = new State(logger);
		state.setHomeAway(HomeAway.AWAY);
		logger.assertEmpty();
		
		state.getHomeAway();
		
		logger.assertContains(LogLevel.DEBUG, "getHomeAway() : AWAY");
	}
}
