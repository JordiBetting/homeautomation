package nl.gingerbeard.automation.controlloop;

import nl.gingerbeard.automation.domoticz.api.DomoticzApi;
import nl.gingerbeard.automation.domoticz.api.DomoticzException;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.onkyo.IOnkyoTransmitter;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Requires;
import nl.gingerbeard.automation.state.IState;

public class ControlloopComponent {

	@Requires
	public IEvents events;

	@Requires
	public ILogger log;

	@Requires
	public IState state;
	
	@Requires
	public IOnkyoTransmitter onkyoTransmitter;
	
	@Requires
	public DomoticzApi domoticz;
	
	@Requires
	public DomoticzConfiguration domoticzConfig;

	@Activate
	public void create() throws InterruptedException {
		final Controlloop controlloop = new Controlloop(events, domoticz, state, log, onkyoTransmitter);
		domoticz.setAlarmListener(controlloop);
		domoticz.setTimeListener(controlloop);
		domoticz.setDeviceListener(controlloop);
		try {
			domoticz.syncFullState();
		} catch (DomoticzException e) {
			log.warning(e, "Cloud not sync full state at startup, continuing without initial state. This may result in misbehaving rules.");
		}
	}

}
