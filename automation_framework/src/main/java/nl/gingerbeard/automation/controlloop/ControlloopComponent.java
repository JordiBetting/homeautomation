package nl.gingerbeard.automation.controlloop;

import nl.gingerbeard.automation.domoticz.IDomoticzDeviceStatusChanged;
import nl.gingerbeard.automation.domoticz.IDomoticzTimeOfDayChanged;
import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;
import nl.gingerbeard.automation.state.State;

public class ControlloopComponent {

	@Requires
	public IEvents events;

	@Requires
	public IDomoticzUpdateTransmitter transmitter;

	@Requires
	public ILogger log;

	@Requires
	public State state;

	@Provides
	public IDomoticzDeviceStatusChanged devicelistener;

	@Provides
	public IDomoticzTimeOfDayChanged timeListener;

	@Activate
	public void provideListener() {
		final Controlloop controlloop = new Controlloop(events, transmitter, state, log);
		devicelistener = controlloop;
		timeListener = controlloop;
	}

	@Deactivate
	public void removeListener() {
		devicelistener = null;
		timeListener = null;
	}
}
