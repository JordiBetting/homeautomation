package nl.gingerbeard.automation.components;

import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.onkyo.IOnkyoTransmitter;
import nl.gingerbeard.automation.onkyo.OnkyoTransmitter;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;

public class OnkyoTransmitterComponent {

	@Requires
	public ILogger logger;
	
	@Provides
	public IOnkyoTransmitter transmitter;
	
	public OnkyoTransmitter instance;
	
	@Activate
	public void createTransmitter() {
		transmitter = instance = new OnkyoTransmitter(logger);
	}
	
	@Deactivate 
	public void cleanup() {
		transmitter = instance = null;
	}

}
