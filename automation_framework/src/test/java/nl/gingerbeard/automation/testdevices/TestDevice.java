package nl.gingerbeard.automation.testdevices;

import nl.gingerbeard.automation.devices.Device;

public class TestDevice extends Device<Void> {

	public TestDevice() {
		super(0);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}