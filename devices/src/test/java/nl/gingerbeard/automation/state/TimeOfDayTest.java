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
		final TimeOfDayValues todv = new TimeOfDayValues(1, 2, 3, 4, 5);

		assertEquals("TimeOfDayValues [curtime=1, sunrise=2, sunset=3, civilTwilightStart=4, civilTwilightEnd=5]", todv.toString());
	}

	@Test
	public void time_toString() {
		final Time time = new Time(LocalDateTime.of(2019, 2, 1, 1, 1));
		assertEquals("Time [fixedTime=Optional[2019-02-01T01:01]]", time.toString());

		final Time timenow = new Time();
		assertEquals("Time [fixedTime=Optional.empty]", timenow.toString());
	}

	@Test
	public void curtime() {
		final TimeOfDayValues tod = new TimeOfDayValues(100, 99, 101, 99, 101);

		assertEquals(100, tod.getCurtime());
	}

	@Test
	public void timeofdayvalues_isDay() {
		final TimeOfDayValues tod = new TimeOfDayValues(100, 99, 101, 99, 101);

		assertTrue(tod.isDayTime());
		assertFalse(tod.isNightTime());
	}

	@Test
	public void timeOfDayValue_isNight_beforeSunrise() {
		final TimeOfDayValues tod = new TimeOfDayValues(90, 100, 110, 100, 110);

		assertTrue(tod.isNightTime());
		assertFalse(tod.isDayTime());
	}

	@Test
	public void timeOfDayValue_isNight_afterSunset() {
		final TimeOfDayValues tod = new TimeOfDayValues(120, 100, 110, 100, 110);

		assertTrue(tod.isNightTime());
		assertFalse(tod.isDayTime());
	}

	@Test
	public void timeOfDay_offsetDaytime() {
		TimeOfDayValues tod;

		tod = new TimeOfDayValues(125, 110, 120, 110, 120);

		assertFalse(tod.isDayTime());
		assertTrue(tod.isDayTime(5));
		assertFalse(tod.isDayTime(4));
	}

	@Test
	public void timeOfDay_offsetNighttime() {
		TimeOfDayValues tod;

		tod = new TimeOfDayValues(125, 100, 130, 100, 130);

		assertFalse(tod.isNightTime());
		assertTrue(tod.isNightTime(-6));
		assertFalse(tod.isNightTime(-5));
	}

	@Test
	public void civilTwilightTest() {
		final TimeOfDayValues tod = new TimeOfDayValues(1, 2, 3, 4, 5);

		assertEquals(2, tod.getSunrise());
		assertEquals(3, tod.getSunset());
		assertEquals(4, tod.getCivilTwilightStart());
		assertEquals(5, tod.getCivilTwilightEnd());
	}
}
