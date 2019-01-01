package nl.gingerbeard.automation.testdevices;

import nl.gingerbeard.automation.devices.Device;

public class StringTestDevice extends Device<String> {

	public StringTestDevice() {
		super(0);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}
