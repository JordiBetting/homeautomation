package nl.gingerbeard.automation.state;

// For sunset, sunrise
public enum TimeOfDay {
	ALLDAY, DAYTIME, NIGHTTIME
	// /json.htm?type=command&param=getSunRiseSet
	;

	public boolean meets(final TimeOfDay other) {
		return this == TimeOfDay.ALLDAY || equals(other) || other == TimeOfDay.ALLDAY;
	}
}
