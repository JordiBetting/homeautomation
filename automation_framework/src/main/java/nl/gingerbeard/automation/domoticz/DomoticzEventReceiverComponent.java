package nl.gingerbeard.automation.domoticz;

import java.io.IOException;

import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;

public class DomoticzEventReceiverComponent {

	@Provides
	public IDomoticzEventReceiver receiver;

	@Requires
	public DomoticzEventReceiverConfiguration config;

	private DomoticzEventReceiver instance;

	@Activate
	public void createReceiver() throws IOException {
		receiver = instance = new DomoticzEventReceiver(config.getPort());
	}

	@Deactivate
	public void stopReceiver() {
		instance.stop();
		receiver = instance = null;
	}

}
