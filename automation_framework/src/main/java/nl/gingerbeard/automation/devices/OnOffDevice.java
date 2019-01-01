package nl.gingerbeard.automation.devices;

import java.util.Locale;

import nl.gingerbeard.automation.state.OnOffState;

public abstract class OnOffDevice extends Device<OnOffState> {

	public OnOffDevice(final int idx) {
		super(idx);
	}

	@Override
	public final boolean updateState(final String newStateString) {
		try {
			final OnOffState newState = OnOffState.valueOf(newStateString.toUpperCase(Locale.US));
			setState(newState);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

}
