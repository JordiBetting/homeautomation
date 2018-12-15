package nl.gingerbeard.automation.devices;

import java.util.Optional;

public class TestDevice extends Device<Void> {

	public TestDevice() {
		super(0, Optional.empty());
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}