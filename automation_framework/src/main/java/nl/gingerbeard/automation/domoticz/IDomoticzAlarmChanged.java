package nl.gingerbeard.automation.domoticz;

import nl.gingerbeard.automation.state.AlarmState;

public interface IDomoticzAlarmChanged {
	void alarmChanged(AlarmState newState);
}
