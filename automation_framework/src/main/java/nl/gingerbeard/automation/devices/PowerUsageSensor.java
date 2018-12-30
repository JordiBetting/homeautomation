package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.devices.PowerUsageSensor.PowerUsage;

public class PowerUsageSensor extends Device<PowerUsage> {

	public static class PowerUsage {

	}

	public PowerUsageSensor(final int idx, final int batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}
