package nl.gingerbeard.automation.controlloop;

import nl.gingerbeard.automation.domoticz.DomoticzComponent.IDomoticz;
import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;
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
	public IDomoticzUpdateTransmitter transmitter;

	@Requires
	public ILogger log;

	@Requires
	public IState state;
	
	@Requires
	public IOnkyoTransmitter onkyoTransmitter;
	
	@Requires
	public IDomoticz domoticz;

	@Activate
	public void provideListener() {
		final Controlloop controlloop = new Controlloop(events, transmitter, state, log, onkyoTransmitter);
		domoticz.setAlarmListener(controlloop);
		domoticz.setTimeListener(controlloop);
		domoticz.setDeviceListener(controlloop);
		
		try {
			controlloop.retrieveInitialState(domoticz.getClients());
		} catch (Exception e) {
			log.warning(e, "Could not retrieve initial state, skipping");
		} // TODO: Retry mechanism?
	}

}
