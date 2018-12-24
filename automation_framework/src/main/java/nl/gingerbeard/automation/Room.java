package nl.gingerbeard.automation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.Switch;

public class Room {
	// public int domoticzId;
	// public Object allLights;
	// public Object allSensors;
	// public Object allActuators;

	private final List<Device<?>> allDevices = new ArrayList<>();

	protected void addDevice(final Switch device) {
		allDevices.add(device);
	}

	public List<Device<?>> getDevices() {
		return Collections.unmodifiableList(allDevices);
	}
}
