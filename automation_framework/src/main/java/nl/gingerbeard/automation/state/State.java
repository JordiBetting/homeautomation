package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

public class State {

	public Object alarm;
	public Object home;
	private TimeOfDay timeOfDay;

	public void setTimeOfDay(final TimeOfDay newTimeOfDay) {
		Preconditions.checkArgument(newTimeOfDay != null && newTimeOfDay != TimeOfDay.ALLDAY);
		timeOfDay = newTimeOfDay;
	}

	public TimeOfDay getTimeOfDay() {
		return timeOfDay;
	}
}
