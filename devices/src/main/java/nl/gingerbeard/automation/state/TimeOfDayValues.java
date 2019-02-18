package nl.gingerbeard.automation.state;

public final class TimeOfDayValues {

	private final int curtime;
	private final int sunrise;
	private final int sunset;
	private final int civilTwilightStart;
	private final int civilTwilightEnd;

	public TimeOfDayValues(final int curtime, final int sunrise, final int sunset, final int civilTwilightStart, final int civilTwilightEnd) {
		this.curtime = curtime;
		this.sunrise = sunrise;
		this.sunset = sunset;
		this.civilTwilightStart = civilTwilightStart;
		this.civilTwilightEnd = civilTwilightEnd;
	}

	public boolean isDayTime() {
		return isDayTime(0);
	}

	public boolean isNightTime() {
		return !isDayTime(0);
	}

	public boolean isDayTime(final int offsetMinutes) {
		return curtime - offsetMinutes <= civilTwilightEnd && //
				curtime + offsetMinutes >= civilTwilightStart;
	}

	public boolean isNightTime(final int offsetMinutes) {
		return !isDayTime(offsetMinutes);
	}

	public int getCurtime() {
		return curtime;
	}

	public int getSunrise() {
		return sunrise;
	}

	public int getSunset() {
		return sunset;
	}

	public int getCivilTwilightStart() {
		return civilTwilightStart;
	}

	public int getCivilTwilightEnd() {
		return civilTwilightEnd;
	}

	@Override
	public String toString() {
		return "TimeOfDayValues [curtime=" + curtime + ", sunrise=" + sunrise + ", sunset=" + sunset + ", civilTwilightStart=" + civilTwilightStart + ", civilTwilightEnd=" + civilTwilightEnd + "]";
	}

}
