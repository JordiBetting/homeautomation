package nl.gingerbeard.automation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.gingerbeard.automation.devices.IDevice;

public class Room {
	// public int domoticzId;
	// public Object allLights;
	// public Object allSensors;
	// public Object allActuators;

	private final List<IDevice<?>> allDevices = new ArrayList<>();

	protected void addDevice(final IDevice device) {
		allDevices.add(device);
	}

	public List<IDevice<?>> getDevices() {
		return Collections.unmodifiableList(allDevices);
	}
}
