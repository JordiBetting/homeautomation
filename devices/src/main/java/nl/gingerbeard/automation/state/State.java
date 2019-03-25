package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

public final class State implements IState {

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

	@Override
	public void setTimeOfDay(final TimeOfDay newTimeOfDay) {
		Preconditions.checkArgument(newTimeOfDay != null && newTimeOfDay != TimeOfDay.ALLDAY);
		timeOfDay = newTimeOfDay;
	}

	@Override
	public TimeOfDay getTimeOfDay() {
		return timeOfDay;
	}

	@Override
	public void setAlarmState(final AlarmState alarmState) {
		Preconditions.checkArgument(alarmState != null && alarmState != AlarmState.ALWAYS && alarmState != AlarmState.ARMED);
		alarm = alarmState;
	}

	@Override
	public void setHomeAway(final HomeAway homeAway) {
		Preconditions.checkArgument(homeAway != null && homeAway != HomeAway.ALWAYS);
		home = homeAway;
	}

	@Override
	public AlarmState getAlarmState() {
		return alarm;
	}

	@Override
	public HomeAway getHomeAway() {
		return home;
	}

	@Override
	public Time getTime() {
		return time;
	}

	@Override
	public void setTime(final Time time) {
		this.time = time;
	}
}
