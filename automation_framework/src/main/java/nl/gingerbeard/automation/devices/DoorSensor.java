package nl.gingerbeard.automation.devices;

import java.util.Optional;

public class DoorSensor extends OpenCloseDevice {

	public DoorSensor(final int idx, final Optional<Integer> batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

}
