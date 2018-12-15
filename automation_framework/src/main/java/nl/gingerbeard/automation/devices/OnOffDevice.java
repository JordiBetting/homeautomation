package nl.gingerbeard.automation.devices;

import java.util.Optional;

import nl.gingerbeard.automation.devices.OnOffDevice.OnOff;

public abstract class OnOffDevice extends Device<OnOff> {

	public static enum OnOff {
		ON, //
		OFF,//
		;
	}

	public OnOffDevice(final int idx, final Optional<Integer> batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	@Override
	public boolean updateState(final String newStateString) {
		try {
			final OnOff newState = OnOff.valueOf(newStateString.toUpperCase());
			setState(newState);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

}
