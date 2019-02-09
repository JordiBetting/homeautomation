package nl.gingerbeard.automation.domoticz.receiver;

import java.io.IOException;

import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;

public final class DomoticzEventReceiverComponent {

	@Provides
	public IDomoticzEventReceiver receiver;

	@Requires
	public DomoticzConfiguration config;

	@Requires
	public ILogger log;

	private DomoticzEventReceiverServer instance;

	@Activate
	public void createReceiver() throws IOException {
		receiver = instance = new DomoticzEventReceiverServer(config, log);
	}

	@Deactivate
	public void stopReceiver() {
		instance.stop();
		receiver = instance = null;
	}

}
