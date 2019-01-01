package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.Color;

public class WhiteAmbianceLight extends Device<Color> {

	public WhiteAmbianceLight(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		// There seems to be no way in domoticz of capturing this.
		// Consider implementing when using differnet backend than Domoticz
		throw new UnsupportedOperationException("Not implemented");
	}

}
