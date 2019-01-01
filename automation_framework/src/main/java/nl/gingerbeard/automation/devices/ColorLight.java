package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.Color;

public class ColorLight extends Device<Color> {

	public ColorLight(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		// There seems to be no way in domoticz of capturing this.
		// Consider implementing when using differnet backend than Domoticz
		throw new UnsupportedOperationException("not implemented");
	}

}
