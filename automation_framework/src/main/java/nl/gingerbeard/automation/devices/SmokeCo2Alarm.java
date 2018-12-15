package nl.gingerbeard.automation.devices;

import java.util.Optional;

public class SmokeCo2Alarm extends OnOffDevice {

	public SmokeCo2Alarm(final int idx, final Optional<Integer> batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}
