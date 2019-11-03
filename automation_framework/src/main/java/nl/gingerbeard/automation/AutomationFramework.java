package nl.gingerbeard.automation;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import com.google.common.base.Preconditions;

import nl.gingerbeard.automation.autocontrol.AutoControl;
import nl.gingerbeard.automation.autocontrol.AutoControlToDomoticz;
import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.devices.CompositeDevice;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.IState;

public class AutomationFramework implements IAutomationFrameworkInterface {

	private final IEvents events;
	private final IDeviceRegistry deviceRegistry;
	private final AutoControlToDomoticz autoControlToDomoticz;
	private final IState state;
	private ILogger log;

	public AutomationFramework(final IEvents events, final IDeviceRegistry deviceRegistry, final IState state, final AutoControlToDomoticz autoControlToDomoticz, ILogger log) {
		this.events = events;
		this.deviceRegistry = deviceRegistry;
		this.state = state;
		this.autoControlToDomoticz = autoControlToDomoticz;
		this.log = log;
	}

	@Override
	public <T extends Room> T addRoom(final Class<T> roomClass) {
		Preconditions.checkArgument(roomClass != null, "Please provide a non-null room");
		final T room = createRoom(roomClass);
		room.setState(state);
		room.getDevices().stream().forEach((device) -> addDevice(device));
		room.getAutoControls().stream().forEach((autoControl) -> {
			addAutoControl(autoControl);
			autoControl.getDevices().forEach((device) -> addDevice(device));
			events.subscribe(autoControl);
		});
		events.subscribe(room);		
		return room;
	}

	final <T extends Room> T createRoom(final Class<T> roomClass) {
		try {
			return roomClass.getConstructor().newInstance();
		} catch (final InvocationTargetException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else {
				throw new RuntimeException(cause);
			}
			// } catch (final InvocationTargetException e) {
			// throw new RuntimeException(e.getCause());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
			final RuntimeException rte = new RuntimeException("Is the room and its default constructor public?");
			rte.initCause(e);
			throw rte;
		}
	}

	private void addAutoControl(final AutoControl autoControl) {
		autoControl.init(autoControlToDomoticz, state, log);
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

}
