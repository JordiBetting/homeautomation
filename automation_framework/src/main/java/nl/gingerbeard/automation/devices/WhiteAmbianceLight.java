package nl.gingerbeard.automation.devices;

import java.util.Optional;

public class WhiteAmbianceLight extends DimmeableLight {

	public WhiteAmbianceLight(final int idx, final Optional<Integer> batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

}
