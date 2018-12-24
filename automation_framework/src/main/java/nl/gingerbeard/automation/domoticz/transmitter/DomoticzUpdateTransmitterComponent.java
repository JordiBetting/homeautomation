package nl.gingerbeard.automation.domoticz.transmitter;

import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;

public final class DomoticzUpdateTransmitterComponent {

	@Provides
	public IDomoticzUpdateTransmitter transmitter;

	@Requires
	public DomoticzConfiguration configuration;

	@Activate
	public void createTransmitter() {
		transmitter = new DomoticzUpdateTransmitter(configuration);
	}

	@Deactivate
	public void removeTransmitter() {
		transmitter = null;
	}

}
