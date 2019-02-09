package nl.gingerbeard.automation.domoticz;

import java.util.Optional;

import nl.gingerbeard.automation.domoticz.receiver.IDomoticzEventReceiver;
import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;

public final class DomoticzComponent {
	@Requires
	public Optional<IDomoticzDeviceStatusChanged> deviceListener;

	@Requires
	public Optional<IDomoticzTimeOfDayChanged> timeListener;

	@Requires
	public IDomoticzEventReceiver domoticzReceiver;

	@Requires
	public IDomoticzUpdateTransmitter domoticzTransmitter;

	@Requires
	public ILogger logger;

	@Provides
	public IDomoticz domoticz;

	private Domoticz domoticzInstance;

	@Activate
	public void registerReceiver() {
		domoticz = domoticzInstance = new Domoticz(deviceListener, timeListener, logger);
		domoticzReceiver.setEventListener(domoticzInstance);
	}

	@Deactivate
	public void unregisterReceiver() {
		domoticzReceiver.setEventListener(null);
		domoticz = domoticzInstance = null;
	}
}
