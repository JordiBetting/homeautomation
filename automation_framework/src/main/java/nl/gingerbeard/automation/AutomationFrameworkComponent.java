package nl.gingerbeard.automation;

import nl.gingerbeard.automation.deviceregistry.DeviceRegistry;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;

public class AutomationFrameworkComponent {

	@Requires
	public IEvents events;

	@Provides
	public IAutomationFrameworkInterface framework;

	@Provides
	public DeviceRegistry deviceRegistry;

	@Activate
	public void createFramework() {
		deviceRegistry = new DeviceRegistry();
		framework = new AutomationFramework(events, deviceRegistry);
	}

	@Deactivate
	public void removeFramework() {
		framework = null;
	}

}
