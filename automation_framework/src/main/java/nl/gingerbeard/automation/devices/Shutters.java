package nl.gingerbeard.automation.devices;

import java.util.Optional;

public class Shutters extends OpenCloseDevice {

	public Shutters(final int idx, final Optional<Integer> batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

}
