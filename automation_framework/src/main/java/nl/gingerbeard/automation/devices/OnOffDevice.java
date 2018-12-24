package nl.gingerbeard.automation.devices;

import java.util.Locale;

import nl.gingerbeard.automation.state.OnOffState;

public abstract class OnOffDevice extends Device<OnOffState> {

	public OnOffDevice(final int idx, final int batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	public OnOffDevice(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newStateString) {
		try {
			final OnOffState newState = OnOffState.valueOf(newStateString.toUpperCase(Locale.ENGLISH));
			setState(newState);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public String getDomoticzSwitchCmd() {
		return getState().name().toLowerCase(Locale.US);
	}

	@Override
	public String getDomoticzParam() {
		return "switchlight";
	}

}
