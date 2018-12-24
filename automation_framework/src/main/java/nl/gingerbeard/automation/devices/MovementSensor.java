package nl.gingerbeard.automation.devices;

public class MovementSensor extends OnOffDevice {

	public MovementSensor(final int idx, final int batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}
