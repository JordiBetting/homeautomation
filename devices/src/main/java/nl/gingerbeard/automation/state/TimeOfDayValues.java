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

	public boolean isDayTime() {
		return isDayTime(0);
	}

	public boolean isNightTime() {
		return !isDayTime(0);
	}

	public boolean isDayTime(final int offsetMinutes) {
		return curtime - offsetMinutes <= sunset && //
				curtime + offsetMinutes >= sunrise;
	}

	public boolean isNightTime(final int offsetMinutes) {
		return !isDayTime(offsetMinutes);
	}

	@Override
	public String toString() {
		return "TimeOfDayValues [curtime=" + curtime + ", sunrise=" + sunrise + ", sunset=" + sunset + "]";
	}
}
