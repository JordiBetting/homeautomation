package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

public final class State {

	// TODO: Initialize state via domoticz
	// state in domoticz is always leading
	public AlarmState alarm = AlarmState.DISARMED;
	public HomeAway home = HomeAway.HOME;
	private TimeOfDay timeOfDay = TimeOfDay.DAYTIME;

	public void setTimeOfDay(final TimeOfDay newTimeOfDay) {
		Preconditions.checkArgument(newTimeOfDay != null && newTimeOfDay != TimeOfDay.ALLDAY);
		timeOfDay = newTimeOfDay;
	}

	public TimeOfDay getTimeOfDay() {
		return timeOfDay;
	}

	public void setAlarmState(final AlarmState alarmState) {
		alarm = alarmState;
	}

	public void setHomeAway(final HomeAway homeAway) {
		home = homeAway;
	}

	public AlarmState getAlarm() {
		return alarm;
	}

	public HomeAway getHomeAway() {
		return home;
	}
}
