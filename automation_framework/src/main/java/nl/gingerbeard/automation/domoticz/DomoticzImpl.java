package nl.gingerbeard.automation.domoticz;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.domoticz.api.DomoticzApi;
import nl.gingerbeard.automation.domoticz.api.DomoticzException;
import nl.gingerbeard.automation.domoticz.api.IDomoticzAlarmChanged;
import nl.gingerbeard.automation.domoticz.api.IDomoticzDeviceStatusChanged;
import nl.gingerbeard.automation.domoticz.api.IDomoticzTimeOfDayChanged;
import nl.gingerbeard.automation.domoticz.clients.TimeOfDayClient;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiverServer;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiverServer.EventReceived;
import nl.gingerbeard.automation.domoticz.threadhandler.DomoticzThreadHandler;
import nl.gingerbeard.automation.domoticz.transmitter.DomoticzUpdateTransmitter;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.TimeOfDayValues;

public class DomoticzImpl implements DomoticzApi, EventReceived {

	private final DomoticzThreadHandler threadHandler;
	private final DomoticzUpdateTransmitter transmitter;
	private final DomoticzEventReceiverServer receiver;
	private final ILogger log;
	private DomoticzConfiguration config;

	public DomoticzImpl(DomoticzConfiguration config, IDeviceRegistry deviceRegistry, IState state, ILogger log)
			throws IOException {
		this.config = config;
		this.log = log;
		threadHandler = new DomoticzThreadHandler(config, deviceRegistry, state, log);
		transmitter = new DomoticzUpdateTransmitter(config, log);
		receiver = new DomoticzEventReceiverServer(config, log);
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
	public void syncFullState() throws DomoticzException, InterruptedException {
		threadHandler.syncFull();
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
				final TimeOfDayValues timeOfDayValues = (new TimeOfDayClient(config)).createTimeOfDayValues();
				threadHandler.timeChanged(timeOfDayValues);
				success = true;
			} catch (final IOException | InterruptedException e) {
				log.warning(e, "Failed retrieving time of day values");
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
			}
		}
		return success;
	}

}
