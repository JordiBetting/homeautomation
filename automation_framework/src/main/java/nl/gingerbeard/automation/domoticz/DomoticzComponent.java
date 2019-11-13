package nl.gingerbeard.automation.domoticz;

import java.io.IOException;
import java.util.Optional;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.domoticz.clients.TimeOfDayClient;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.receiver.IDomoticzEventReceiver;
import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
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

	@Requires
	public DomoticzConfiguration domoticzConfig;

	private Domoticz domoticzInstance;

	@Provides
	public DomoticzThreadHandler threadHandler;

	@Activate
	public void registerReceiver() throws IOException {
		threadHandler = createThreadHandler();
		final TimeOfDayClient todClient = new TimeOfDayClient(domoticzConfig);
		domoticzInstance = new Domoticz(logger, threadHandler, todClient);
		domoticzReceiver.setEventListener(domoticzInstance);
	}

	private DomoticzThreadHandler createThreadHandler() {
		final DomoticzThreadHandler threadHandler = new DomoticzThreadHandler(logger, deviceRegistry);
		threadHandler.setAlarmListener(alarmListener);
		threadHandler.setDeviceListener(deviceListener);
		threadHandler.setTimeListener(timeListener);
		return threadHandler;
	}

	@Deactivate
	public void unregisterReceiver() {
		domoticzReceiver.setEventListener(null);
		domoticzInstance = null;
		threadHandler = null;
	}
}
