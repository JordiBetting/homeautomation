package nl.gingerbeard.automation.devices;

import java.util.Optional;

public class ColorLight extends WhiteAmbianceLight {

	public ColorLight(final int idx, final Optional<Integer> batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

}
