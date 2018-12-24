package nl.gingerbeard.automation;

import nl.gingerbeard.automation.controlloop.Controlloop;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.Domoticz;
import nl.gingerbeard.automation.domoticz.DomoticzEventReceiverComponent;
import nl.gingerbeard.automation.event.Events;
import nl.gingerbeard.automation.event.EventsCompoent;
import nl.gingerbeard.automation.service.Container;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;
import nl.gingerbeard.automation.state.StateComponent;

public class AutomationFramework {

	@Requires
	public Events events;

	@Provides
	public AutomationFramework framework;

	@Activate
	public void createFramework() {
		framework = this;
	}

	public void addRoom(final Room room) {
		events.subscribe(room);
	}

	public void deviceChanged(final Device<?> changedDevice) {
		events.trigger(changedDevice);
	}

	public static Container createFrameworkContainer() {
		final Container container = new Container();
		container.register(StateComponent.class);
		container.register(EventsCompoent.class);
		container.register(Domoticz.class);
		container.register(DomoticzEventReceiverComponent.class);
		container.register(Controlloop.class);
		container.register(AutomationFramework.class);
		return container;
	}

}
