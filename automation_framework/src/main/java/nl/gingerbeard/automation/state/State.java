package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

import nl.gingerbeard.automation.event.EventState;

public class State {

	public Object alarm;
	public Object home;
	private TimeOfDay timeOfDay = TimeOfDay.DAYTIME;

	public void setTimeOfDay(final TimeOfDay newTimeOfDay) {
		Preconditions.checkArgument(newTimeOfDay != null && newTimeOfDay != TimeOfDay.ALLDAY);
		timeOfDay = newTimeOfDay;
	}

	public TimeOfDay getTimeOfDay() {
		return timeOfDay;
	}

	public boolean meets(final EventState eventState) {
		// TODO: alarm + home
		return timeOfDay.equals(eventState.timeOfDay()) || eventState.timeOfDay() == TimeOfDay.ALLDAY;
	}
}
