package nl.gingerbeard.automation.domoticz.transmitter;

import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;

public final class DomoticzUpdateTransmitterComponent {

	@Provides
	public IDomoticzUpdateTransmitter transmitter;

	@Requires
	public DomoticzConfiguration configuration;

	@Requires
	public ILogger log;

	@Activate
	public void createTransmitter() {
		transmitter = new DomoticzUpdateTransmitter(configuration, log);
	}

	@Deactivate
	public void removeTransmitter() {
		transmitter = null;
	}

}
