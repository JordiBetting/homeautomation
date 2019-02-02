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
			String upperCase = newStateString.toUpperCase(Locale.US);
			if ("CLOSED".equals(upperCase)) {
				upperCase = "CLOSE";
			}
			final OpenCloseState newState = OpenCloseState.valueOf(upperCase);
			setState(newState);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

}
