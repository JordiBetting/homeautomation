package nl.gingerbeard.automation;

import nl.gingerbeard.automation.controlloop.ControlloopComponent;
import nl.gingerbeard.automation.domoticz.DomoticzComponent;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiverComponent;
import nl.gingerbeard.automation.domoticz.transmitter.DomoticzUpdateTransmitterComponent;
import nl.gingerbeard.automation.event.EventsComponent;
import nl.gingerbeard.automation.service.Container;
import nl.gingerbeard.automation.state.StateComponent;

public final class AutomationFrameworkContainer {

	private final Container container = new Container();

	public AutomationFrameworkContainer(final DomoticzConfiguration domoticzConfig) {
		container.register(StateComponent.class);
		container.register(EventsComponent.class);
		container.register(DomoticzComponent.class);
		container.register(DomoticzEventReceiverComponent.class);
		container.register(DomoticzUpdateTransmitterComponent.class);
		container.register(ControlloopComponent.class);
		container.register(AutomationFrameworkComponent.class);
		container.register(DomoticzConfiguration.class, domoticzConfig, 1);
	}

	public IAutomationFrameworkInterface getAutomationFramework() {
		return container.getService(IAutomationFrameworkInterface.class).get();
	}

	public void start() {
		container.start();
	}

	public void stop() {
		container.shutDown();
	}

	public Container getRuntime() {
		return container;
	}
}
