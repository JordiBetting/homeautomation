package nl.gingerbeard.automation.domoticz;

import java.util.Optional;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.domoticz.receiver.IDomoticzEventReceiver;
import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Requires;

public final class DomoticzComponent {

	// TODO: Is this design clean enough? Quite some requires, while you would expect central component (e.g. Controller) to have that. Is this component needed at all?

	@Requires
	public Optional<IDomoticzDeviceStatusChanged> deviceListener;

	@Requires
	public Optional<IDomoticzTimeOfDayChanged> timeListener;

	@Requires
	public Optional<IDomoticzAlarmChanged> alarmListener;

	// TODO: IDomoticzEventReceiver shall be a @Provide. GetListeningPort needs refactoring
	@Requires
	public IDomoticzEventReceiver domoticzReceiver;

	@Requires
	public IDomoticzUpdateTransmitter domoticzTransmitter;

	@Requires
	public ILogger logger;

	@Requires
	public IDeviceRegistry deviceRegistry;

	private Domoticz domoticzInstance;

	@Activate
	public void registerReceiver() {
		domoticzInstance = new Domoticz(deviceListener, timeListener, alarmListener, logger, deviceRegistry);
		domoticzReceiver.setEventListener(domoticzInstance);
	}

	@Deactivate
	public void unregisterReceiver() {
		domoticzReceiver.setEventListener(null);
		domoticzInstance = null;
	}
}
