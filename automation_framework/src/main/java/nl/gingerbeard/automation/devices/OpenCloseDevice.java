package nl.gingerbeard.automation.devices;

import java.util.Locale;

import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OpenCloseState;

public abstract class OpenCloseDevice extends Device<OpenCloseState> {

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
	public String getDomoticzSwitchCmd(final NextState<OpenCloseState> nextState) {
		return nextState.get().name().toLowerCase(Locale.US);
	}
}
