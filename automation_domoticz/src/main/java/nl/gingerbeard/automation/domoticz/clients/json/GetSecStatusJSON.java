package nl.gingerbeard.automation.domoticz.clients.json;

import java.io.IOException;

import com.google.gson.annotations.SerializedName;

import nl.gingerbeard.automation.state.AlarmState;

public class GetSecStatusJSON {

	@SerializedName("secstatus")
	private int secstatus;

	public AlarmState getAlarmState() throws IOException {
		if (secstatus == 0) {
			return AlarmState.DISARMED;
		} else if (secstatus == 1) {
			return AlarmState.ARM_HOME;
		} else if (secstatus == 2) {
			return AlarmState.ARM_AWAY;
		} else {
			throw new IOException("Unknown secstatus code " + secstatus + " received"); 
		}
	}
}
