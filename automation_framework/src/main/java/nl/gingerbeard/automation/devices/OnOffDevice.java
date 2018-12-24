package nl.gingerbeard.automation.devices;

import java.util.Locale;

import nl.gingerbeard.automation.devices.OnOffDevice.OnOff;

public abstract class OnOffDevice extends Device<OnOff> {

	public static enum OnOff {
		ON, //
		OFF,//
		;
	}

	public OnOffDevice(final int idx, final int batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	public OnOffDevice(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newStateString) {
		try {
			final OnOff newState = OnOff.valueOf(newStateString.toUpperCase(Locale.ENGLISH));
			setState(newState);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

}
