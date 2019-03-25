package nl.gingerbeard.automation;

import java.util.List;

import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.state.NextState;

public class AutoControlExample extends AutoControl {

	void triggerListener(final List<NextState<?>> updates) {
		super.updateActuators(updates);
	}

	@Override
	public List<IDevice<?>> getDevices() {
		return null;
	}
}
