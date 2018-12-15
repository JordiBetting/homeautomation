package nl.gingerbeard.automation.devices;

import java.util.Optional;

import nl.gingerbeard.automation.devices.OpenCloseDevice.OpenCloseState;

public abstract class OpenCloseDevice extends Device<OpenCloseState> {

	public static enum OpenCloseState {
		OPEN, //
		CLOSE, //
		;
	}

	public OpenCloseDevice(final int idx, final Optional<Integer> batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}
}
