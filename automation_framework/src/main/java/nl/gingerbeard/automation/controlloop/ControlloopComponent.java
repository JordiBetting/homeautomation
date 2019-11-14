package nl.gingerbeard.automation.controlloop;

import java.time.Duration;
import java.util.Optional;

import nl.gingerbeard.automation.domoticz.IDomoticz;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.onkyo.IOnkyoTransmitter;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Requires;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.util.RetryUtil;

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
	
	@Requires
	public DomoticzConfiguration domoticzConfig;

	@Activate
	public void provideListener() throws InterruptedException {
		final Controlloop controlloop = new Controlloop(events, transmitter, state, log, onkyoTransmitter);
		domoticz.setAlarmListener(controlloop);
		domoticz.setTimeListener(controlloop);
		domoticz.setDeviceListener(controlloop);
		
		getInitialState(controlloop); 
	}

	private void getInitialState(final Controlloop controlloop) throws InterruptedException {
		int interval_s = domoticzConfig.getInitInterval_s();
		int nrTries = interval_s == 0 ? 1 : Math.max(1, domoticzConfig.getMaxInitWait_s()/interval_s);
		
		Optional<Throwable> e = RetryUtil.retry(() -> controlloop.retrieveInitialState(domoticz.getClients()), nrTries, Duration.ofSeconds(interval_s));
		if (e.isPresent()) {
			log.warning(e.get(), "Could not retrieve initial state, skipping");
		}
	}

}
