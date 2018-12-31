package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.Level;

public class DimmeableLight extends Device<Level> {

	public DimmeableLight(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		try {
			final Level newLevel = new Level(Integer.parseInt(newState));
			setState(newLevel);
		} catch (final NumberFormatException e) {
			return false;
		}
		return true;
	}
}
