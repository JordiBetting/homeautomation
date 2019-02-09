package nl.gingerbeard.automation.state;

public final class TimeOfDayValues {

	private final int curtime;
	private final int sunrise;
	private final int sunset;

	public TimeOfDayValues(final int curtime, final int sunrise, final int sunset) {
		this.curtime = curtime;
		this.sunrise = sunrise;
		this.sunset = sunset;
	}

	boolean isDayTime() {
		return isDayTime(0);
	}

	boolean isNightTime() {
		return !isDayTime(0);
	}

	boolean isDayTime(final int offsetMinutes) {
		return curtime + offsetMinutes <= sunset && //
				curtime - offsetMinutes >= sunrise;
	}

	boolean isNightTime(final int offsetMinutes) {
		return !isDayTime(offsetMinutes);
	}
}
