package nl.gingerbeard.automation.domoticz;

import java.io.IOException;

import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.TimeOfDayValues;

public interface IDomoticzClient {

	TimeOfDayValues getCurrentTime() throws IOException;
	AlarmState getCurrentAlarmState() throws IOException;	
}
