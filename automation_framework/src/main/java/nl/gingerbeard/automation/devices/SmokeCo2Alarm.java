package nl.gingerbeard.automation.devices;

public class SmokeCo2Alarm extends OnOffDevice {

	public SmokeCo2Alarm(final int idx, final int batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}
