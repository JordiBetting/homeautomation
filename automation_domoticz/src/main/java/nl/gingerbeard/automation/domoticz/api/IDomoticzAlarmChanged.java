package nl.gingerbeard.automation.domoticz.api;

import nl.gingerbeard.automation.state.AlarmState;

public interface IDomoticzAlarmChanged {
	void alarmChanged(AlarmState newState);
}
