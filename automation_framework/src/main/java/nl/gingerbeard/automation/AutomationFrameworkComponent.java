package nl.gingerbeard.automation;

import nl.gingerbeard.automation.event.Events;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;

public class AutomationFrameworkComponent {

	@Requires
	public Events events;

	@Provides
	public AutomationFramework framework;

	@Activate
	public void createFramework() {
		framework = new AutomationFramework(events);
	}

	@Deactivate
	public void removeFramework() {
		framework = null;
	}

}
