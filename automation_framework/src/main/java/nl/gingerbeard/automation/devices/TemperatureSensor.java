package nl.gingerbeard.automation.devices;

import java.util.Optional;

public class TemperatureSensor extends Device {

	public TemperatureSensor(final int idx, final Optional<Integer> batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}
