package nl.gingerbeard.automation;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;

public interface IAutomationFrameworkInterface {

	void addRoom(Room room); // TODO: consider adding Class rather then instance

	void deviceChanged(Device<?> changedDevice);

	public static AutomationFrameworkContainer createFrameworkContainer(final DomoticzConfiguration domoticzConfig) {
		return new AutomationFrameworkContainer(domoticzConfig);
	}
}
