package nl.gingerbeard.automation.deviceregistry;

import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;

public class DeviceRegistryComponent {

	@Provides
	public IDeviceRegistry deviceRegistry;

	@Activate
	public void createRegistry() {
		deviceRegistry = new DeviceRegistry();
	}

	@Deactivate
	public void removeRegistry() {
		deviceRegistry = null;
	}

}
