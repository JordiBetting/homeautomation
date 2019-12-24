package nl.gingerbeard.automation.state;

public class TimeOfDayValues {

	private final int curtime;
	private final int sunrise;
	private final int sunset;
	private final int civilTwilightStart;
	private final int civilTwilightEnd;

	public TimeOfDayValues(final int curtime, final int sunrise, final int sunset, final int civilTwilightStart,
			final int civilTwilightEnd) {
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
		return "TimeOfDayValues [curtime=" + curtime + ", sunrise=" + sunrise + ", sunset=" + sunset
				+ ", civilTwilightStart=" + civilTwilightStart + ", civilTwilightEnd=" + civilTwilightEnd + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + civilTwilightEnd;
		result = prime * result + civilTwilightStart;
		result = prime * result + curtime;
		result = prime * result + sunrise;
		result = prime * result + sunset;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final TimeOfDayValues other = (TimeOfDayValues) obj;
		if (civilTwilightEnd != other.civilTwilightEnd) {
			return false;
		}
		if (civilTwilightStart != other.civilTwilightStart) {
			return false;
		}
		if (curtime != other.curtime) {
			return false;
		}
		if (sunrise != other.sunrise) {
			return false;
		}
		if (sunset != other.sunset) {
			return false;
		}
		return true;
	}

}
