package nl.gingerbeard.automation;

import nl.gingerbeard.automation.controlloop.ControlloopComponent;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.DomoticzComponent;
import nl.gingerbeard.automation.domoticz.DomoticzEventReceiverComponent;
import nl.gingerbeard.automation.event.EventsCompoent;
import nl.gingerbeard.automation.service.Container;
import nl.gingerbeard.automation.state.StateComponent;

public interface AutomationFrameworkInterface {

	void addRoom(Room room);

	void deviceChanged(Device<?> changedDevice);

	public static Container createFrameworkContainer() {
		final Container container = new Container();
		container.register(StateComponent.class);
		container.register(EventsCompoent.class);
		container.register(DomoticzComponent.class);
		container.register(DomoticzEventReceiverComponent.class);
		container.register(ControlloopComponent.class);
		container.register(AutomationFrameworkComponent.class);
		return container;
	}
}
