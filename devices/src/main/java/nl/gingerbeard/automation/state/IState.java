package nl.gingerbeard.automation.state;

public interface IState {

	void setTimeOfDay(TimeOfDay newTimeOfDay);

	TimeOfDay getTimeOfDay();

	void setAlarmState(AlarmState alarmState);

	void setHomeAway(HomeAway homeAway);

	AlarmState getAlarmState();

	HomeAway getHomeAway();

	Time getTime();

	void setTime(Time time);

}