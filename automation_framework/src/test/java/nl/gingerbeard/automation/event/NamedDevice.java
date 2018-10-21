package nl.gingerbeard.automation.event;

import nl.gingerbeard.automation.devices.Device;

public class NamedDevice extends Device {
	private final String name;

	public NamedDevice(final String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
