package nl.gingerbeard.automation.devices;

import java.util.Optional;

public class WallPlug extends OnOffDevice {

	public WallPlug(final int idx, final Optional<Integer> batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	@Override
	public boolean updateState(final String newState) {
		// TODO Auto-generated method stub
		return false;
	}
}
