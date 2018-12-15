package nl.gingerbeard.automation.devices;

import java.util.Optional;

public abstract class OpenCloseDevice extends Device {

	public OpenCloseDevice(final int idx, final Optional<Integer> batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}
}
