package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.Level;

public class DimmeableLight extends Device<Level> {

	public DimmeableLight(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		setState(new Level(Integer.parseInt(newState)));
		return true;
	}
}
