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
}
