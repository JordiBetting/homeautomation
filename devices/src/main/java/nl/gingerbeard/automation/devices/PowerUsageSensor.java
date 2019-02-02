package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.PowerUsage;

public class PowerUsageSensor extends Device<PowerUsage> {

	public PowerUsageSensor(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		try {
			final int newValue = Integer.parseInt(newState);
			setState(new PowerUsage(newValue));
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}

}
