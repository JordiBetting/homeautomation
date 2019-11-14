package nl.gingerbeard.automation.domoticz;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.TimeOfDayValues;

public class DomoticzThreadHandler {
	private Optional<IDomoticzDeviceStatusChanged> deviceListener = Optional.empty();
	private Optional<IDomoticzTimeOfDayChanged> timeListener = Optional.empty();
	private Optional<IDomoticzAlarmChanged> alarmListener = Optional.empty();
	private final IDeviceRegistry deviceRegistry;
	private final ThreadPoolExecutor executor;
	private final ILogger logger;
	private volatile boolean isSynchronous = false;

	public DomoticzThreadHandler(final ILogger logger, final IDeviceRegistry deviceRegistry) {
		this.logger = logger;
		this.deviceRegistry = deviceRegistry;

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

	public void alarmChanged(final String newState) throws InterruptedException {
		final AlarmState alarm = getAlarmStateByString(newState);
		execute(() -> {
			alarmListener.ifPresent((listener) -> listener.alarmChanged(alarm));
		});
	}

	private void execute(final Runnable command) throws InterruptedException {
		final CountDownLatch sync = new CountDownLatch(1);

		executor.execute(() -> {
			command.run();
			sync.countDown();
		});

		if (isSynchronous) {
			sync.await();
		}
	}

	public void deviceChanged(final int idx, final String newState) throws InterruptedException {
		Preconditions.checkArgument(newState != null);
		execute(() -> {
			final Optional<Device<?>> device = deviceRegistry.updateDevice(idx, newState);
			if (device.isPresent()) {
				logger.debug("Device with idx " + idx + " changed state into: " + newState);
				final Device<?> changedDevice = device.get();
				deviceListener.ifPresent((listener) -> listener.statusChanged(changedDevice));
			}
		});
	}

	public void timeChanged(final TimeOfDayValues timeOfDayValues) throws InterruptedException {
		Preconditions.checkArgument(timeOfDayValues != null);
		execute(() -> {
			timeListener.ifPresent((listener) -> listener.timeChanged(timeOfDayValues));
		});
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

	/**
	 * By default, this class handles all events asynchronuos, by calling this method, all will be handled synchronously
	 */
	public void setSynchronous() {
		isSynchronous = true;
	}

	public void stop(final long timeout, final TimeUnit unit) throws InterruptedException {
		executor.shutdown();
		executor.awaitTermination(timeout, unit);
	}

}
