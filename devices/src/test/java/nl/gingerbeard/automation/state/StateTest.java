package nl.gingerbeard.automation.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class StateTest {

	@ParameterizedTest
	@EnumSource(value = TimeOfDay.class, names = { "DAYTIME", "NIGHTTIME" })
	public void setTimeOfDay_valid_works(final TimeOfDay validTimeOfDay) {
		final State state = new State();

		state.setTimeOfDay(validTimeOfDay);
		assertEquals(validTimeOfDay, state.getTimeOfDay());
	}

	@ParameterizedTest
	@EnumSource(value = TimeOfDay.class, names = { "ALLDAY", })
	public void setTimeOfDay_invalid_throwsException(final TimeOfDay invalidTimeOfDay) {
		final State state = new State();

		assertThrows(IllegalArgumentException.class, () -> state.setTimeOfDay(invalidTimeOfDay));
	}

	@Test
	public void setTimeOfDay_null_throwsException() {
		final State state = new State();

		assertThrows(IllegalArgumentException.class, () -> state.setTimeOfDay(null));
	}

	@ParameterizedTest
	@EnumSource(value = HomeAway.class, names = { "HOME", "AWAY" })
	public void setHomeAway_valid_works(final HomeAway validHomeAway) {
		final State state = new State();

		state.setHomeAway(validHomeAway);
		assertEquals(validHomeAway, state.getHomeAway());
	}

	@ParameterizedTest
	@EnumSource(value = HomeAway.class, names = { "ALWAYS", })
	public void setHomeAway_invalid_throwsException(final HomeAway invalidHomeAway) {
		final State state = new State();

		assertThrows(IllegalArgumentException.class, () -> state.setHomeAway(invalidHomeAway));
	}

	@Test
	public void setHomeAway_null_throwsException() {
		final State state = new State();

		assertThrows(IllegalArgumentException.class, () -> state.setHomeAway(null));
	}

	@ParameterizedTest
	@EnumSource(value = AlarmState.class, names = { "DISARMED", "ARM_HOME", "ARM_AWAY" })
	public void setAlarmState_valid_works(final AlarmState validAlarmState) {
		final State state = new State();

		state.setAlarmState(validAlarmState);
		assertEquals(validAlarmState, state.getAlarmState());
	}

	@ParameterizedTest
	@EnumSource(value = AlarmState.class, names = { "ALWAYS", "ARMED" })
	public void setAlarmState_invalid_throwsException(final AlarmState invalidAlarmState) {
		final State state = new State();

		assertThrows(IllegalArgumentException.class, () -> state.setAlarmState(invalidAlarmState));
	}

	@Test
	public void setAlarmState_null_throwsException() {
		final State state = new State();

		assertThrows(IllegalArgumentException.class, () -> state.setAlarmState(null));
	}
}
