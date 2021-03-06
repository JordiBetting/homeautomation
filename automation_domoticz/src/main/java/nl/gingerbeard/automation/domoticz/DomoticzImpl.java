package nl.gingerbeard.automation.domoticz;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.domoticz.api.DomoticzApi;
import nl.gingerbeard.automation.domoticz.api.DomoticzException;
import nl.gingerbeard.automation.domoticz.api.IDomoticzAlarmChanged;
import nl.gingerbeard.automation.domoticz.api.IDomoticzDeviceStatusChanged;
import nl.gingerbeard.automation.domoticz.api.IDomoticzTimeOfDayChanged;
import nl.gingerbeard.automation.domoticz.clients.TimeOfDayClient;
import nl.gingerbeard.automation.domoticz.clients.UpdateTransmitterClient;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration.DomoticzInitBehaviorConfig;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiverServer;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiverServer.EventReceived;
import nl.gingerbeard.automation.domoticz.threadhandler.DomoticzThreadHandler;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.TimeOfDayValues;
import nl.gingerbeard.automation.util.RetryUtil;
import nl.gingerbeard.automation.util.RetryUtil.RetryTask;

public class DomoticzImpl implements DomoticzApi, EventReceived {

	private final DomoticzThreadHandler threadHandler;
	private final UpdateTransmitterClient transmitter;
	private final DomoticzEventReceiverServer receiver;
	private final ILogger log;
	private final TimeOfDayClient todClient;
	private DomoticzConfiguration config;

	public DomoticzImpl(DomoticzConfiguration config, IDeviceRegistry deviceRegistry, IState state, ILogger log)
			throws IOException {
		this(new UpdateTransmitterClient(config, log), //
				new DomoticzEventReceiverServer(config, log), //
				new DomoticzThreadHandler(config, deviceRegistry, state, log), //
				new TimeOfDayClient(config, log), //
				config, log);
	}

	DomoticzImpl(UpdateTransmitterClient transmitter, DomoticzEventReceiverServer receiver,
			DomoticzThreadHandler threadHandler, TimeOfDayClient todClient, DomoticzConfiguration config, ILogger log) {
		this.threadHandler = threadHandler;
		this.transmitter = transmitter;
		this.receiver = receiver;
		this.config = config;
		this.log = log;
		this.todClient = todClient;

		receiver.setEventListener(this);
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
	public void syncFullState() throws DomoticzException {
		if (config.isInitEnabled()) {
			try {
				executeTaskWithRetries(() -> threadHandler.syncFull(), config.getInitConfig().get());
			} catch (InterruptedException e) {
				throw new DomoticzException("Interrupted while retrying syncFullState", e);
			}
		}
	}

	void executeTaskWithRetries(RetryTask task, DomoticzInitBehaviorConfig config)
			throws InterruptedException, DomoticzException {
		int interval_s = config.getInitInterval_s();
		int nrTries = Math.max(1, config.getMaxInitWait_s() / Math.max(1, interval_s));
		Optional<Throwable> e = RetryUtil.retry(task, nrTries, Duration.ofSeconds(interval_s));
		if (e.isPresent()) {
			throw new DomoticzException("Failed to sync full state with Domoticz", e.get());
		}
	}

	@Override
	public <T> void transmitDeviceUpdate(NextState<T> nextState) throws DomoticzException {
		transmitter.transmitDeviceUpdate(nextState);
	}

	public void stop() throws InterruptedException {
		// TODO: make configurable
		threadHandler.stop(15, TimeUnit.SECONDS);
		receiver.stop();
	}

	@Override
	public boolean deviceChanged(int idx, String newState) throws DomoticzException {
		boolean success = false;
		if (threadHandler.handlesDevice(idx)) {
			try {
				threadHandler.deviceChanged(idx, newState);
				success = true;
			} catch (final InterruptedException e) {
				log.warning(e, "Interrupted while updating device");
				throw new DomoticzException(e);
			}
		} else {
			// logger.debug("Received update for unknown device with idx: " + idx);
		}
		return success;
	}

	@Override
	public boolean timeChanged(int curtime, int sunrise, int sunset) throws DomoticzException {
		boolean success = false;
		if (threadHandler.handlesTime()) {
			try {
				// TODO: Should todClient be used here?
				final TimeOfDayValues timeOfDayValues = todClient.createTimeOfDayValues();
				threadHandler.timeChanged(timeOfDayValues);
				success = true;
			} catch (final IOException | InterruptedException e) {
				log.warning(e, "Failed retrieving time of day values");
				throw new DomoticzException(e);
			}
		}
		return success;
	}

	@Override
	public boolean alarmChanged(String alarmState) throws DomoticzException {
		boolean success = false;
		if (threadHandler.handlesAlarm()) {
			try {
				threadHandler.alarmChanged(alarmState);
				success = true;
			} catch (final InterruptedException e) {
				log.warning(e, "Interrupted while changing alarm state");
				throw new DomoticzException(e);
			}
		}
		return success;
	}

}
