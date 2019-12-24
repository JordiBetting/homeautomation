package nl.gingerbeard.automation.domoticz;

import java.io.IOException;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.domoticz.api.DomoticzApi;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;
import nl.gingerbeard.automation.state.IState;

public class DomoticzComponent {

	@Requires
	public DomoticzConfiguration config;

	@Requires
	public ILogger log;

	@Requires
	public IDeviceRegistry deviceRegistry;

	@Requires
	public IState state;

	@Provides
	public DomoticzApi api;

	@Activate
	public void create() throws IOException {
		api = new DomoticzImpl(config, deviceRegistry, state, log);
	}

	@Deactivate
	public void destroy() throws InterruptedException {
		api.stop();
		api = null;
	}

}
