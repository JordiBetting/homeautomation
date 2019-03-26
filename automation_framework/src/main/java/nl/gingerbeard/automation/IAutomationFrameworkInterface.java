package nl.gingerbeard.automation;

import nl.gingerbeard.automation.configuration.ConfigurationServerSettings;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.ILogOutput;

public interface IAutomationFrameworkInterface {

	<T extends Room> T addRoom(Class<T> room);

	void deviceChanged(Device<?> changedDevice);

	public static AutomationFrameworkContainer createFrameworkContainer(final DomoticzConfiguration domoticzConfig, final ConfigurationServerSettings configSettings) {
		return new AutomationFrameworkContainer(domoticzConfig, configSettings);
	}

	public static AutomationFrameworkContainer createFrameworkContainer(final DomoticzConfiguration domoticzConfig, final ILogOutput logOutput, final ConfigurationServerSettings configSettings) {
		return new AutomationFrameworkContainer(domoticzConfig, logOutput, configSettings);
	}

}
