package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.Level;

public class DimmeableLight extends Device<Level> {
	// TODO: consider 'PercentageDevice' as it may also count for shutters, etc. I don't know if control is the same. Check domoticz

	public DimmeableLight(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		try {
			setState(new Level(Integer.parseInt(newState)));
			return true;
		} catch (final NumberFormatException e) {
			return false;
			// TODO: log
		}
	}
}
