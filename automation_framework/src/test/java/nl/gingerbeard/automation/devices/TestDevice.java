package nl.gingerbeard.automation.devices;

public class TestDevice extends Device<Void> {

	public TestDevice() {
		super(0);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}