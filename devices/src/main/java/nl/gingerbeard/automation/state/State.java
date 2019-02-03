package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

public final class State {

	// TODO: Initialize state via domoticz
	// state in domoticz is always leading
	public AlarmState alarm;
	public HomeAway home;
	private TimeOfDay timeOfDay;
	private Time time;

	public State() {
		time = new Time();
		timeOfDay = TimeOfDay.DAYTIME;
		home = HomeAway.HOME;
		alarm = AlarmState.DISARMED;
	}

	public void setTimeOfDay(final TimeOfDay newTimeOfDay) {
		Preconditions.checkArgument(newTimeOfDay != null && newTimeOfDay != TimeOfDay.ALLDAY);
		timeOfDay = newTimeOfDay;
	}

	public TimeOfDay getTimeOfDay() {
		return timeOfDay;
	}

	public void setAlarmState(final AlarmState alarmState) {
		Preconditions.checkArgument(alarmState != null && alarmState != AlarmState.ALWAYS && alarmState != AlarmState.ARMED);
		alarm = alarmState;
	}

	public void setHomeAway(final HomeAway homeAway) {
		Preconditions.checkArgument(homeAway != null && homeAway != HomeAway.ALWAYS);
		home = homeAway;
	}

	public AlarmState getAlarmState() {
		return alarm;
	}

	public HomeAway getHomeAway() {
		return home;
	}

	public Time getTime() {
		return time;
	}

	public void setTime(final Time time) {
		this.time = time;
	}
}
