package nl.gingerbeard.automation.devices;

public class StringTestDevice extends Device<String> {

	public StringTestDevice() {
		super(0);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}
