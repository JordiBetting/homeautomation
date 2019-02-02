package nl.gingerbeard.automation.state;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TimeOfDayTest {

	@Test
	public void meets_daytime() {
		assertTrue(TimeOfDay.DAYTIME.meets(TimeOfDay.DAYTIME));
		assertFalse(TimeOfDay.DAYTIME.meets(TimeOfDay.NIGHTTIME));
		assertTrue(TimeOfDay.DAYTIME.meets(TimeOfDay.ALLDAY));
	}

	@Test
	public void meets_nighttime() {
		assertFalse(TimeOfDay.NIGHTTIME.meets(TimeOfDay.DAYTIME));
		assertTrue(TimeOfDay.NIGHTTIME.meets(TimeOfDay.NIGHTTIME));
		assertTrue(TimeOfDay.NIGHTTIME.meets(TimeOfDay.ALLDAY));
	}

	@Test
	public void meets_allday() {
		assertTrue(TimeOfDay.ALLDAY.meets(TimeOfDay.DAYTIME));
		assertTrue(TimeOfDay.ALLDAY.meets(TimeOfDay.NIGHTTIME));
		assertTrue(TimeOfDay.ALLDAY.meets(TimeOfDay.ALLDAY));
	}
}
