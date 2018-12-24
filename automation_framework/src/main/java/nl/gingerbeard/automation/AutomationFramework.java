package nl.gingerbeard.automation;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.event.Events;

public class AutomationFramework implements AutomationFrameworkInterface {

	private final Events events;

	public AutomationFramework(final Events events) {
		this.events = events;
	}

	@Override
	public void addRoom(final Room room) {
		events.subscribe(room);
	}

	// TODO: responsibility does not feel right. Not an external interface as it shall be controlled via state receiver
	@Override
	public void deviceChanged(final Device<?> changedDevice) {
		events.trigger(changedDevice);
	}
}
