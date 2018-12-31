package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.OpenCloseState;

public abstract class OpenCloseDevice extends Device<OpenCloseState> {

	public OpenCloseDevice(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}
