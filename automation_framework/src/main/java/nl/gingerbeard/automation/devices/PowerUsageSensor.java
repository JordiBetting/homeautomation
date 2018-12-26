package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.devices.PowerUsageSensor.PowerUsage;
import nl.gingerbeard.automation.state.NextState;

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

	@Override
	public String getDomoticzParam() {
		throw new UnsupportedOperationException("Can't set sensor value");
	}

	@Override
	public String getDomoticzSwitchCmd(final NextState<PowerUsage> nextState) {
		throw new UnsupportedOperationException("Can't set sensor value");
	}
}
