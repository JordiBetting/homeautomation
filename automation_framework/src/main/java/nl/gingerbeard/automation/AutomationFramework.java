package nl.gingerbeard.automation;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

import nl.gingerbeard.automation.autocontrol.AutoControl;
import nl.gingerbeard.automation.autocontrol.AutoControlToDomoticz;
import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.devices.CompositeDevice;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.domoticz.api.DomoticzApi;
import nl.gingerbeard.automation.domoticz.api.DomoticzException;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.IState;

public class AutomationFramework implements IAutomationFrameworkInterface {

	private final IEvents events;
	private final IDeviceRegistry deviceRegistry;
	private final AutoControlToDomoticz autoControlToDomoticz;
	private final IState state;
	private ILogger log;
	private DomoticzApi domoticz;
	private Map<Class<? extends Room>, Room> rooms = new HashMap<>();

	public AutomationFramework(final IEvents events, final IDeviceRegistry deviceRegistry, final IState state,
			final AutoControlToDomoticz autoControlToDomoticz, ILogger log, DomoticzApi domoticz) {
		this.events = events;
		this.deviceRegistry = deviceRegistry;
		this.state = state;
		this.autoControlToDomoticz = autoControlToDomoticz;
		this.log = log;
		this.domoticz = domoticz;
	}

	private void addRoom(final Class<? extends Room> roomClass) {
		Preconditions.checkArgument(roomClass != null, "Please provide a non-null room");
		final Room room = createRoom(roomClass);
		logAddRoom(room);

		room.getDevices().stream().forEach((device) -> addDevice(device));
		room.getAutoControls().stream().forEach((autoControl) -> {
			addAutoControl(autoControl);
		});
		events.subscribe(room);

		rooms.put(roomClass, room);
	}

	private void addAutoControl(AutoControl autoControl) {
		autoControl.init(autoControlToDomoticz, state, log);
		autoControl.getDevices().forEach((device) -> addDevice(device));
		events.subscribe(autoControl);
	}

	private <T extends Room> void logAddRoom(T room) {
		log.debug(String.format("Adding room %s with %d devices and %d autocontrols containing %d devices in total", //
				room.getClass().getSimpleName(), //
				room.getDevices().size(), //
				room.getAutoControls().size(), //
				room.getAutoControls().stream().mapToInt((autoControl) -> autoControl.getDevices().size()).sum() //
		));
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
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
			final RuntimeException rte = new RuntimeException("Is the room and its default constructor public?");
			rte.initCause(e);
			throw rte;
		}
	}

	private void addDevice(final IDevice<?> device) {
		if (device instanceof CompositeDevice) {
			for (final Device<?> childDevice : getDevices(device)) {
				addDevice(childDevice);
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

	// TODO: responsibility does not feel right. Not an external interface as it
	// shall be controlled via state receiver
	@Override
	public void deviceChanged(final Device<?> changedDevice) {
		events.trigger(changedDevice);
	}
	
	/////////////////////////////////
	
	@Override
	public void start(Class<? extends Room> room) throws InterruptedException {
		addRoom(room);
		roomsAdded();
	}
	
	@Override
	public void start(Collection<Class<? extends Room>> rooms) throws InterruptedException {
		for (Class<? extends Room> room : rooms) {
			addRoom(room);
		}
		roomsAdded();
	}

	public void roomsAdded() throws InterruptedException {
		try {
			domoticz.syncFullState();
		} catch (DomoticzException e) {
			log.warning(e,
					"Could not sync full state at startup, continuing without initial state. This may result in misbehaving rules.");
		}
		for (Room room : rooms.values()) {
			room.init(state);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Room> T getRoom(Class<T> roomType) {
		Room room = rooms.get(roomType);
		if (room != null && room.getClass().isAssignableFrom(roomType)) {
			return (T) room;
		}
		return null;
	}

}
