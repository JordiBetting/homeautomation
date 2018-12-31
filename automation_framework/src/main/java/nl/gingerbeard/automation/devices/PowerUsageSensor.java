package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.devices.PowerUsageSensor.PowerUsage;

public class PowerUsageSensor extends Device<PowerUsage> {

	public static class PowerUsage {
		// TODO: Move to state and implement
	}

	public PowerUsageSensor(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}
