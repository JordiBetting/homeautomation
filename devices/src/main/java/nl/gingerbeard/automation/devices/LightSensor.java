package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.LightIntensity;

public class LightSensor extends Device<LightIntensity> {

	public LightSensor(final int idx) {
		super(idx);
	}

	@Override
	public final boolean updateState(final String newState) {
		try {
			final LightIntensity intensity = new LightIntensity(Integer.parseInt(newState));
			setState(intensity);
		} catch (final NumberFormatException e) {
			return false;
		}
		return true;
	}

}
