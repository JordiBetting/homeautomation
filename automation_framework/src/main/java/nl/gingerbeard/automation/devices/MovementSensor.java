package nl.gingerbeard.automation.devices;

public class MovementSensor extends OnOffDevice {

	public MovementSensor(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}
