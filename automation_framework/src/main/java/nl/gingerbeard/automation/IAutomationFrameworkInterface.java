package nl.gingerbeard.automation;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.ILogOutput;

public interface IAutomationFrameworkInterface {

	void addRoom(Room room);

	void deviceChanged(Device<?> changedDevice);

	void addRooms(Room... rooms);

	public static AutomationFrameworkContainer createFrameworkContainer(final DomoticzConfiguration domoticzConfig) {
		return new AutomationFrameworkContainer(domoticzConfig);
	}

	public static AutomationFrameworkContainer createFrameworkContainer(final DomoticzConfiguration domoticzConfig, final ILogOutput logOutput) {
		return new AutomationFrameworkContainer(domoticzConfig, logOutput);
	}

}
