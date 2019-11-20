package nl.gingerbeard.automation;

import java.util.Collection;

import nl.gingerbeard.automation.components.AutomationFrameworkComponent;
import nl.gingerbeard.automation.components.ConfigurationServerComponent;
import nl.gingerbeard.automation.components.EventsComponent;
import nl.gingerbeard.automation.components.LoggingComponent;
import nl.gingerbeard.automation.components.OnkyoTransmitterComponent;
import nl.gingerbeard.automation.components.StateComponent;
import nl.gingerbeard.automation.configuration.ConfigurationServerSettings;
import nl.gingerbeard.automation.controlloop.ControlloopComponent;
import nl.gingerbeard.automation.deviceregistry.DeviceRegistryComponent;
import nl.gingerbeard.automation.domoticz.DomoticzComponent;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.ILogOutput;
import nl.gingerbeard.automation.service.Container;

public final class AutomationFrameworkContainer {

	private final Container container = new Container();

	public AutomationFrameworkContainer(final DomoticzConfiguration domoticzConfig, final ConfigurationServerSettings configSettings) {
		createBasics(domoticzConfig, configSettings);
	}

	public AutomationFrameworkContainer(final DomoticzConfiguration domoticzConfig, final ILogOutput logOutput, final ConfigurationServerSettings configSettings) {
		createBasics(domoticzConfig, configSettings);
		container.register(ILogOutput.class, logOutput, 1);
	}

	private void createBasics(final DomoticzConfiguration domoticzConfig, final ConfigurationServerSettings configSettings) {
		container.register(StateComponent.class);
		container.register(EventsComponent.class);
		container.register(DomoticzComponent.class);
		container.register(ControlloopComponent.class);
		container.register(AutomationFrameworkComponent.class);
		container.register(LoggingComponent.class);
		container.register(DomoticzConfiguration.class, domoticzConfig, 1);
		container.register(DeviceRegistryComponent.class);
		container.register(ConfigurationServerComponent.class);
		container.register(ConfigurationServerSettings.class, configSettings, 1);
		container.register(OnkyoTransmitterComponent.class);
	}

	public IAutomationFrameworkInterface getAutomationFramework() {
		return container.getService(IAutomationFrameworkInterface.class).get();
	}
	
	public void start(Class<? extends Room> room) throws InterruptedException {
		container.start();
		getAutomationFramework().start(room);
	}

	@SafeVarargs
	public final void start(Class<? extends Room> ... rooms) throws InterruptedException {
		container.start(); 
		getAutomationFramework().start(rooms);
	}
	
	public void start(Collection<Class<? extends Room>> rooms) throws InterruptedException {
		container.start();
		getAutomationFramework().start(rooms);
	}


	public void stop() {
		container.shutDown();
	}

	public Container getRuntime() {
		return container;
	}
}
