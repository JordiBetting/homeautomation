package nl.gingerbeard.automation.devices;

import java.util.Optional;

public class WindowSensor extends OpenCloseDevice {

	public WindowSensor(final int idx, final Optional<Integer> batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

}
