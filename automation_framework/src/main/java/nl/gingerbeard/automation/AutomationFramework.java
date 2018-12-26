package nl.gingerbeard.automation;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.IDomoticz;
import nl.gingerbeard.automation.event.IEvents;

public class AutomationFramework implements IAutomationFrameworkInterface {

	private final IEvents events;
	private final IDomoticz domoticzEvents;

	public AutomationFramework(final IEvents events, final IDomoticz domoticzEvents) {
		this.events = events;
		this.domoticzEvents = domoticzEvents;
	}

	@Override
	public void addRoom(final Room room) {
		events.subscribe(room);
		for (final Device<?> device : room.getDevices()) {
			domoticzEvents.addDevice(device);
		}
	}

	// TODO: responsibility does not feel right. Not an external interface as it shall be controlled via state receiver
	@Override
	public void deviceChanged(final Device<?> changedDevice) {
		events.trigger(changedDevice);
	}

	// TODO: Add synchronization method to ensure devices are not updated while processing an update, etc. Consider grouping events that come in right after each other.

}
