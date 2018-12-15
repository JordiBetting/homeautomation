package nl.gingerbeard.automation.devices;

import java.util.Optional;

public class Switch extends OnOffDevice {

	public Switch(final int idx, final Optional<Integer> batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

}
