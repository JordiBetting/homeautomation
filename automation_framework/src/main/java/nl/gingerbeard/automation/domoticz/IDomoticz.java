package nl.gingerbeard.automation.domoticz;

public interface IDomoticz {
	void setAlarmListener(IDomoticzAlarmChanged alarmListener);

	void setDeviceListener(IDomoticzDeviceStatusChanged deviceListener);

	void setTimeListener(IDomoticzTimeOfDayChanged timeListener);

	IDomoticzClient getClients();
}