package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

import nl.gingerbeard.automation.event.EventState;

public class State {

	// TODO: Initialize state somehow
	public AlarmState alarm = AlarmState.DISARMED;
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
		// TODO: home
		return timeOfDay.meets(eventState.timeOfDay()) && alarm.meets(eventState.alarmState());
	}

	public void setAlarmState(final AlarmState alarmState) {
		alarm = alarmState;
	}
}
