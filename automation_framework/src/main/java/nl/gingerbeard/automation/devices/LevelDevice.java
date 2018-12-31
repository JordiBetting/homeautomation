package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.Level;

public abstract class LevelDevice extends Device<Level> {

	public LevelDevice(final int idx) {
		super(idx);
	}

	@Override
	public final boolean updateState(final String newState) {
		try {
			final Level newLevel = new Level(Integer.parseInt(newState));
			setState(newLevel);
		} catch (final NumberFormatException e) {
			return false;
		}
		return true;
	}
}