package nl.gingerbeard.automation;

import nl.gingerbeard.automation.controlloop.ControlloopComponent;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.DomoticzComponent;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiverComponent;
import nl.gingerbeard.automation.domoticz.transmitter.DomoticzUpdateTransmitterComponent;
import nl.gingerbeard.automation.event.EventsComponent;
import nl.gingerbeard.automation.service.Container;
import nl.gingerbeard.automation.state.StateComponent;

public interface IAutomationFrameworkInterface {

	void addRoom(Room room); // TODO: consider adding Class rather then instance

	void deviceChanged(Device<?> changedDevice);

	public static Container createFrameworkContainer() {
		final Container container = new Container();
		container.register(StateComponent.class);
		container.register(EventsComponent.class);
		container.register(DomoticzComponent.class);
		container.register(DomoticzEventReceiverComponent.class);
		container.register(DomoticzUpdateTransmitterComponent.class);
		container.register(ControlloopComponent.class);
		container.register(AutomationFrameworkComponent.class);
		return container;
	}
}