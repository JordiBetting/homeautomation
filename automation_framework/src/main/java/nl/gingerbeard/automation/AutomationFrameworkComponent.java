package nl.gingerbeard.automation;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
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

	@Requires
	public IDeviceRegistry deviceRegistry;

	@Activate
	public void createFramework() {
		framework = new AutomationFramework(events, deviceRegistry);
	}

	@Deactivate
	public void removeFramework() {
		framework = null;
	}

}
