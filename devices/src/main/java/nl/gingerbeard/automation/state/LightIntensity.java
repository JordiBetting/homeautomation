package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

public class LightIntensity {
	private final int lux;

	public LightIntensity(final int lux) {
		Preconditions.checkArgument(lux > 0);
		this.lux = lux;
	}

	public int getLux() {
		return lux;
	}
}
