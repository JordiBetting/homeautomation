package nl.gingerbeard.automation;

import java.util.Collection;

import nl.gingerbeard.automation.configuration.ConfigurationServerSettings;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.ILogOutput;

public interface IAutomationFrameworkInterface {

	void deviceChanged(Device<?> changedDevice);

	public static AutomationFrameworkContainer createFrameworkContainer(final DomoticzConfiguration domoticzConfig,
			final ConfigurationServerSettings configSettings) {
		return new AutomationFrameworkContainer(domoticzConfig, configSettings);
	}

	public static AutomationFrameworkContainer createFrameworkContainer(final DomoticzConfiguration domoticzConfig,
			final ILogOutput logOutput, final ConfigurationServerSettings configSettings) {
		return new AutomationFrameworkContainer(domoticzConfig, logOutput, configSettings);
	}

	void start(Class<? extends Room> room) throws InterruptedException;
	
	void start(Collection<Class<? extends Room>> rooms) throws InterruptedException;

	<T extends Room> T getRoom(Class<T> roomType);

}
