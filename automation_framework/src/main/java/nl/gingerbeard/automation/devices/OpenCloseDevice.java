package nl.gingerbeard.automation.devices;

import java.util.Locale;

import nl.gingerbeard.automation.devices.OpenCloseDevice.OpenCloseState;

public abstract class OpenCloseDevice extends Device<OpenCloseState> {

	public static enum OpenCloseState {
		OPEN, //
		CLOSE, //
		;
	}

	public OpenCloseDevice(final int idx, final int batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

	@Override
	public String getDomoticzParam() {
		return "switchlight";
	}

	@Override
	public String getDomoticzSwitchCmd() {
		return getState().name().toLowerCase(Locale.US);
	}
}
