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

	private final RoomBuilder builder = new RoomBuilder();

	public class RoomBuilder {
		public RoomBuilder and(final IDevice<?> device) {
			allDevices.add(device);
			return this;
		}
	}

	protected final RoomBuilder addDevice(final IDevice<?> device) {
		return builder.and(device);
	}

	public final List<IDevice<?>> getDevices() {
		return Collections.unmodifiableList(allDevices);
	}
}
