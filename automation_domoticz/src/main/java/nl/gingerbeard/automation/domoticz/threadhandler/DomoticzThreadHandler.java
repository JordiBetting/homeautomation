package nl.gingerbeard.automation.domoticz.threadhandler;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.api.DomoticzException;
import nl.gingerbeard.automation.domoticz.api.IDomoticzAlarmChanged;
import nl.gingerbeard.automation.domoticz.api.IDomoticzDeviceStatusChanged;
import nl.gingerbeard.automation.domoticz.api.IDomoticzTimeOfDayChanged;
import nl.gingerbeard.automation.domoticz.clients.TimeOfDayClient;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.sync.SyncAll;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.TimeOfDay;
import nl.gingerbeard.automation.state.TimeOfDayValues;

public class DomoticzThreadHandler {
	private Optional<IDomoticzDeviceStatusChanged> deviceListener = Optional.empty();
	private Optional<IDomoticzTimeOfDayChanged> timeListener = Optional.empty();
	private Optional<IDomoticzAlarmChanged> alarmListener = Optional.empty();

	private final IDeviceRegistry deviceRegistry;
	private final ThreadPoolExecutor executor;
	private final ILogger logger;
	private final IState state;
	private final DomoticzConfiguration config;
	private SyncAll syncAll;

	public DomoticzThreadHandler(final DomoticzConfiguration config, final IDeviceRegistry deviceRegistry, IState state,
			final ILogger logger) throws IOException {
		this(config, deviceRegistry, state, logger, new TimeOfDayClient(config, logger),
				new SyncAll(config, state, deviceRegistry, logger));
	}

	DomoticzThreadHandler(final DomoticzConfiguration config, final IDeviceRegistry deviceRegistry, IState state,
			final ILogger logger, TimeOfDayClient todClient, SyncAll syncAll) {
		this.config = config;
		this.deviceRegistry = deviceRegistry;
		this.state = state;
		this.logger = logger;
		this.syncAll = syncAll;

		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
	}

	public void setDeviceListener(final IDomoticzDeviceStatusChanged deviceListener) {
		this.deviceListener = Optional.of(deviceListener);
	}

	public void setTimeListener(final IDomoticzTimeOfDayChanged timeListener) {
		this.timeListener = Optional.of(timeListener);
	}

	public void setAlarmListener(IDomoticzAlarmChanged alarmListener) {
		this.alarmListener = Optional.of(alarmListener);
	}

	private AlarmState getAlarmStateByString(final String alarmStateString) {
		final String ucState = alarmStateString.toUpperCase(Locale.US);
		return AlarmState.valueOf(ucState);
	}

	public void alarmChanged(final String newState) throws InterruptedException, DomoticzException {
		final AlarmState newAlarmState = getAlarmStateByString(newState);
		execute(() -> {
			if (state.getAlarmState() != newAlarmState) {
				state.setAlarmState(newAlarmState);
				alarmListener.ifPresent((listener) -> listener.alarmChanged(newAlarmState));
			}
		});
	}

	static class Container<T> {
		private Optional<T> item = Optional.empty();

		void set(T item) {
			this.item = Optional.of(item);
		}

		boolean isPresent() {
			return item.isPresent();
		}

		T get() {
			return item.orElse(null);
		}
	}

	static interface Task {
		void execute() throws DomoticzException;
	}

	void execute(final Task command) throws InterruptedException, DomoticzException {
		execute(command, config.isSynchronousEventHandling());
	}
	
	void execute(final Task command, boolean isSynchronous) throws InterruptedException, DomoticzException {
		final CountDownLatch sync = new CountDownLatch(1);

		Container<DomoticzException> thrown = new Container<>();

		executor.execute(() -> {
			try {
				command.execute();
			} catch (DomoticzException e) {
				thrown.set(e);
				if (!isSynchronous) {
					logger.exception(e, "Failed to execute command");
				}
			} finally {
				sync.countDown();
			}
		});

		if (isSynchronous) {
			sync.await();
			if (thrown.isPresent()) {
				if (thrown.get().getCause() instanceof InterruptedException) {
					throw (InterruptedException) thrown.get().getCause();
				} else {
					throw thrown.get();
				}
			}
		}
	}
	
	private void executeSynchronous(final Task command) throws InterruptedException, DomoticzException {
		execute(command, true);
	}

	public void deviceChanged(final int idx, final String newState) throws InterruptedException, DomoticzException {
		Preconditions.checkArgument(newState != null);
		execute(() -> {
			Optional<?> oldState = deviceRegistry.getDeviceState(idx);
			final Optional<Device<?>> device = deviceRegistry.updateDevice(idx, newState);
			if (device.isPresent()) {
				if (oldState.orElse(null) != device.get().getState()) {
					// TODO consistent logging
					logger.debug("Device with idx " + idx + " changed state into: " + newState);
					final Device<?> changedDevice = device.get();
					deviceListener.ifPresent((listener) -> listener.statusChanged(changedDevice));
				}
			}
		});
	}

	public void timeChanged(final TimeOfDayValues timeOfDayValues) throws InterruptedException, DomoticzException {
		// TODO: timeOfDayValues may contain valuable information for rules. Propagate
		// into state and events.
		Preconditions.checkArgument(timeOfDayValues != null);
		final TimeOfDay newTimeOfDay = toTimeOfDay(timeOfDayValues);
		execute(() -> {
			if (state.getTimeOfDay() != newTimeOfDay) {
				state.setTimeOfDay(newTimeOfDay);
				timeListener.ifPresent((listener) -> listener.timeChanged(timeOfDayValues));
			}
		});
	}

	private TimeOfDay toTimeOfDay(final TimeOfDayValues timeOfDayValues) {
		final TimeOfDay newTimeOfDay = timeOfDayValues.isDayTime() ? TimeOfDay.DAYTIME : TimeOfDay.NIGHTTIME;
		return newTimeOfDay;
	}

	public boolean handlesDevice(final int idx) {
		return deviceListener.isPresent() && deviceRegistry.devicePresent(idx);
	}

	public boolean handlesTime() {
		return timeListener.isPresent();
	}

	public boolean handlesAlarm() {
		return alarmListener.isPresent();
	}

	public void stop(final long timeout, final TimeUnit unit) throws InterruptedException {
		executor.shutdown();
		executor.awaitTermination(timeout, unit);
	}

	public void syncFull() throws DomoticzException, InterruptedException {
		executeSynchronous(() -> {
			try {
				syncAll.syncAll();
			} catch (IOException e) {
				throw new DomoticzException(e);
			}
		});
	}


}
