package nl.gingerbeard.automation.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

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

	@Test
	public void timeofdayValues_toString() {
		final TimeOfDayValues todv = new TimeOfDayValues(1, 2, 3);

		assertEquals("TimeOfDayValues [curtime=1, sunrise=2, sunset=3]", todv.toString());
	}

	@Test
	public void time_toString() {
		final Time time = new Time(LocalDateTime.of(2019, 2, 1, 1, 1));
		assertEquals("Time [fixedTime=Optional[2019-02-01T01:01]]", time.toString());

		final Time timenow = new Time();
		assertEquals("Time [fixedTime=Optional.empty]", timenow.toString());
	}
}
