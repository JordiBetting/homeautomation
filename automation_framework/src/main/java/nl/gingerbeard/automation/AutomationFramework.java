package nl.gingerbeard.automation;

import java.util.Arrays;
import java.util.Set;

import com.google.common.base.Preconditions;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.devices.CompositeDevice;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.state.State;

public class AutomationFramework implements IAutomationFrameworkInterface {

	private final IEvents events;
	private final IDeviceRegistry deviceRegistry;
	private final AutoControlToDomoticz autoControlToDomoticz;
	private final State state;

	public AutomationFramework(final IEvents events, final IDeviceRegistry deviceRegistry, final State state, final AutoControlToDomoticz autoControlToDomoticz) {
		this.events = events;
		this.deviceRegistry = deviceRegistry;
		this.state = state;
		this.autoControlToDomoticz = autoControlToDomoticz;
	}

	@Override
	public void addRoom(final Room room) {
		Preconditions.checkArgument(room != null, "Please provide a non-null room");
		room.setState(state);
		room.getDevices().stream().forEach((device) -> addDevice(device));
		room.getAutoControls().stream().forEach((autoControl) -> {
			addAutoControl(autoControl);
			autoControl.getDevices().forEach((device) -> addDevice(device));
			events.subscribe(autoControl);
		});
		events.subscribe(room);

	}

	private void addAutoControl(final AutoControl autoControl) {
		autoControl.setListener(autoControlToDomoticz);
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

}
