package nl.gingerbeard.automation.domoticz;

import java.io.IOException;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.domoticz.clients.AlarmStateClient;
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

	// TODO: Is this design clean enough? Quite some requires, while you would
	// expect central component (e.g. Controller) to have that. Is this component
	// needed at all?

	// TODO: IDomoticzEventReceiver shall be a @Provide. GetListeningPort needs
	// refactoring
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

	@Provides
	public DomoticzThreadHandler threadHandler;

	@Provides
	public IDomoticz domoticz;

	private Domoticz domoticzInstance;

	@Activate
	public void registerReceiver() throws IOException {
		threadHandler = createThreadHandler();
		final TimeOfDayClient todClient = new TimeOfDayClient(domoticzConfig);
		final AlarmStateClient alarmClient = new AlarmStateClient(domoticzConfig);
		domoticzInstance = new Domoticz(logger, threadHandler, todClient, alarmClient);
		domoticzReceiver.setEventListener(domoticzInstance);
		domoticz = new DomoticzImpl(threadHandler, domoticzInstance);
		// TODO: it's getting messy. This needs to be redesigned
	}

	private DomoticzThreadHandler createThreadHandler() {
		final DomoticzThreadHandler threadHandler = new DomoticzThreadHandler(logger, deviceRegistry);
		return threadHandler;
	}

	@Deactivate
	public void unregisterReceiver() {
		domoticzReceiver.setEventListener(null);
		domoticzInstance = null;
		threadHandler = null;
		domoticz = null;
	}

	public static interface IDomoticz {
		void setAlarmListener(IDomoticzAlarmChanged alarmListener);

		void setDeviceListener(IDomoticzDeviceStatusChanged deviceListener);

		void setTimeListener(IDomoticzTimeOfDayChanged timeListener);

		IDomoticzClient getClients();
	}

	public static class DomoticzImpl implements IDomoticz {

		private DomoticzThreadHandler threadHandler;;
		private IDomoticzClient clients;

		public DomoticzImpl(DomoticzThreadHandler threadHandler, IDomoticzClient clients) {
			this.threadHandler = threadHandler;
			this.clients = clients;
		}

		@Override
		public void setAlarmListener(IDomoticzAlarmChanged alarmListener) {
			threadHandler.setAlarmListener(alarmListener);
		}

		@Override
		public void setDeviceListener(IDomoticzDeviceStatusChanged deviceListener) {
			threadHandler.setDeviceListener(deviceListener);
		}

		@Override
		public void setTimeListener(IDomoticzTimeOfDayChanged timeListener) {
			threadHandler.setTimeListener(timeListener);
		}

		@Override
		public IDomoticzClient getClients() {
			return clients;
		}

	}
}
