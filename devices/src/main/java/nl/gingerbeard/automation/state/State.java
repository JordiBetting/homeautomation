package nl.gingerbeard.automation.state;

import java.util.Optional;

import com.google.common.base.Preconditions;

import nl.gingerbeard.automation.logging.ILogger;

public final class State implements IState {

	// TODO: Initialize state via domoticz
	// state in domoticz is always leading
	public AlarmState alarm;
	public HomeAway home;
	private TimeOfDay timeOfDay; 
	private Time time;
	private final Optional<ILogger> log;

	public State(ILogger logger) {
		this(Optional.of(logger));
	}

	public State() {
		this(Optional.empty());
	}
	
	private State(Optional<ILogger> logger) {
		time = new Time();
		timeOfDay = TimeOfDay.DAYTIME;
		home = HomeAway.HOME;
		alarm = AlarmState.DISARMED;
		log = logger;
	}
	
	@Override
	public void setTimeOfDay(final TimeOfDay newTimeOfDay) {
		Preconditions.checkArgument(newTimeOfDay != null && newTimeOfDay != TimeOfDay.ALLDAY);
		timeOfDay = newTimeOfDay;
	}

	@Override
	public TimeOfDay getTimeOfDay() {
		log("getTimeOfDay() : " + timeOfDay);
		return timeOfDay;
	}

	private void log(String message) {
		log.ifPresent((log) -> log.debug(message));
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
		log("getAlarmState() : " + alarm);
		return alarm;
	}

	@Override
	public HomeAway getHomeAway() {
		log("getHomeAway() : " + home);
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
