package nl.gingerbeard.automation.domoticz;

public class DomoticzImpl implements IDomoticz {

	private DomoticzThreadHandler threadHandler;;
	private IDomoticzClient clients;

	public DomoticzImpl(DomoticzThreadHandler threadHandler, IDomoticzClient clients) {
		this.threadHandler = threadHandler;
		this.clients = clients;
	}

	@Override
	public void setAlarmListener(IDomoticzAlarmChanged alarmListener) {
		threadHandler.setAlarmListener(alarmListener);
	}

	@Override
	public void setDeviceListener(IDomoticzDeviceStatusChanged deviceListener) {
		threadHandler.setDeviceListener(deviceListener);
	}

	@Override
	public void setTimeListener(IDomoticzTimeOfDayChanged timeListener) {
		threadHandler.setTimeListener(timeListener);
	}

	@Override
	public IDomoticzClient getClients() {
		return clients;
	}

}