package nl.gingerbeard.automation.devices;

import java.util.Optional;

public class MovementSensor extends Device {

	public MovementSensor(final int idx, final Optional<Integer> batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}
