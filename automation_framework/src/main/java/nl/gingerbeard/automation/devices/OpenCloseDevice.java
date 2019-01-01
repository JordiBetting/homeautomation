package nl.gingerbeard.automation.devices;

import java.util.Locale;

import nl.gingerbeard.automation.state.OpenCloseState;

public abstract class OpenCloseDevice extends Device<OpenCloseState> {

	public OpenCloseDevice(final int idx) {
		super(idx);
	}

	@Override
	public final boolean updateState(final String newStateString) {
		try {
			final OpenCloseState newState = OpenCloseState.valueOf(newStateString.toUpperCase(Locale.US));
			setState(newState);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

}
