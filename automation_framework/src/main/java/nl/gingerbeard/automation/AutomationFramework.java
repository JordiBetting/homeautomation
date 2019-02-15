package nl.gingerbeard.automation;

import java.util.Arrays;
import java.util.Set;

import com.google.common.base.Preconditions;

import nl.gingerbeard.automation.deviceregistry.DeviceRegistry;
import nl.gingerbeard.automation.devices.CompositeDevice;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.event.IEvents;

public class AutomationFramework implements IAutomationFrameworkInterface {

	private final IEvents events;
	private final DeviceRegistry deviceRegistry;

	public AutomationFramework(final IEvents events, final DeviceRegistry deviceRegistry) {
		this.events = events;
		this.deviceRegistry = deviceRegistry;
	}

	@Override
	public void addRoom(final Room room) {
		events.subscribe(room);
		for (final IDevice<?> device : room.getDevices()) {
			addDevice(device);
		}
	}

	private void addDevice(final IDevice<?> device) {
		if (device instanceof CompositeDevice) {
			for (final Device<?> childDevice : getDevices(device)) {
				deviceRegistry.addDevice(childDevice);
			}
		} else if (device instanceof Device) {
			deviceRegistry.addDevice((Device<?>) device);
		} else {
			throw new UnsupportedOperationException("Type not supported: " + device.getClass().getName());
		}
	}

	private Set<Device<?>> getDevices(final IDevice<?> device) {
		final CompositeDevice<?> composite = (CompositeDevice<?>) device;
		final Set<Device<?>> devices = composite.getDevices();
		return devices;
	}

	// TODO: responsibility does not feel right. Not an external interface as it shall be controlled via state receiver
	@Override
	public void deviceChanged(final Device<?> changedDevice) {
		events.trigger(changedDevice);
	}

	@Override
	public void addRooms(final Room... rooms) {
		Preconditions.checkArgument(rooms != null, "addRooms() shall be passed a non-null array");
		Arrays.stream(rooms).forEach((room) -> addRoom(room));
	}

	// TODO: Add synchronization method to ensure devices are not updated while processing an update, etc. Consider grouping events that come in right after each other.

}
