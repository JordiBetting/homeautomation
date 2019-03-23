package nl.gingerbeard.automation.domoticz;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.TimeOfDayValues;

public class DomoticzThreadHandlerTest {

	private static final TimeOfDayValues TIMEOFDAY_EXAMPLE = new TimeOfDayValues(5, 1, 2, 4, 5);

	private ILogger logger;
	private IDeviceRegistry registry;
	private DomoticzThreadHandler handler;

	@BeforeEach
	public void create() {
		logger = mock(ILogger.class);
		registry = mock(IDeviceRegistry.class);
		handler = new DomoticzThreadHandler(logger, registry);
	}

	@AfterEach
	public void shutdown() throws InterruptedException {
		handler.stop(3, TimeUnit.SECONDS);
	}

	@Test
	public void sync_time_nolistener_noexception() throws InterruptedException {
		handler.setSynchronous();

		handler.timeChanged(TIMEOFDAY_EXAMPLE);
	}

	@Test
	public void sync_time_listener_listenerCalled() throws InterruptedException {
		handler.setSynchronous();
		final IDomoticzTimeOfDayChanged listener = mock(IDomoticzTimeOfDayChanged.class);

		handler.setTimeListener(Optional.of(listener));
		handler.timeChanged(TIMEOFDAY_EXAMPLE);

		verify(listener, times(1)).timeChanged(TIMEOFDAY_EXAMPLE);
		verifyNoMoreInteractions(listener);
	}

	@Test
	public void sync_time_invalidInput_exceptionThrown() {
		handler.setSynchronous();

		assertThrows(IllegalArgumentException.class, () -> handler.timeChanged(null));
	}

	@Test
	public void sync_alarm_nolistener_noException() throws InterruptedException {
		handler.setSynchronous();

		handler.alarmChanged("disarmed");
	}

	@Test
	public void sync_alarm_invalidValue_exceptionThrown() {
		handler.setSynchronous();

		assertThrows(IllegalArgumentException.class, () -> handler.alarmChanged("doesNotExist"));
	}

	@Test
	public void sync_alarm_listener_listenerCalled() throws InterruptedException {
		handler.setSynchronous();
		final IDomoticzAlarmChanged listener = mock(IDomoticzAlarmChanged.class);

		handler.setAlarmListener(Optional.of(listener));
		handler.alarmChanged("disarmed");

		verify(listener, times(1)).alarmChanged(AlarmState.DISARMED);
		verifyNoMoreInteractions(listener);
	}

	@Test
	public void sync_device_nolistener_noException() throws InterruptedException {
		handler.setSynchronous();

		handler.deviceChanged(1, "On");
	}

	@Test
	public void sync_device_nolistener_deviceUpdated() throws InterruptedException {
		handler.setSynchronous();

		handler.deviceChanged(1, "on");

		verify(registry, times(1)).updateDevice(1, "on");
		verifyNoMoreInteractions(registry);
	}

	@Test
	public void sync_device_invalidInput_throwsException() {
		handler.setSynchronous();

		assertThrows(IllegalArgumentException.class, () -> handler.deviceChanged(1, null));
	}

	@Test
	public void sync_device_listener_listenerCalled() throws InterruptedException {
		handler.setSynchronous();
		final IDomoticzDeviceStatusChanged listener = mock(IDomoticzDeviceStatusChanged.class);
		final Device<?> device = new Switch(1);
		when(registry.updateDevice(1, "on")).thenReturn(Optional.of(device));
		handler.setDeviceListener(Optional.of(listener));

		handler.deviceChanged(1, "on");

		verify(listener, times(1)).statusChanged(device);
		verifyNoMoreInteractions(listener);
	}

	// Mockito doesn't want me to do this kind of stuff
	private static class MultiListener implements IDomoticzTimeOfDayChanged, IDomoticzAlarmChanged {
		final AtomicInteger counter = new AtomicInteger();
		final AtomicInteger max = new AtomicInteger();
		final AtomicInteger total = new AtomicInteger();
		private final CountDownLatch finishLatch;

		public MultiListener(final CountDownLatch finishLatch) {
			this.finishLatch = finishLatch;
		}

		@Override
		public void timeChanged(final TimeOfDayValues time) {
			count();
		}

		@Override
		public void alarmChanged(final AlarmState newState) {
			count();
		}

		private void count() {
			try {
				final int val = counter.incrementAndGet();
				total.incrementAndGet();
				Thread.sleep(500);
				synchronized (this) {
					if (val > max.get()) {
						max.set(val);
					}
				}
				counter.getAndDecrement();
				finishLatch.countDown();
			} catch (final InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

	}

	@Test
	public void async() throws Throwable {

		final AtomicReference<AssertionError> failure = new AtomicReference<>();

		final CountDownLatch threadsAliveLatch = new CountDownLatch(2);
		final CountDownLatch startLatch = new CountDownLatch(1);
		final CountDownLatch finishLatch = new CountDownLatch(2);
		final MultiListener listener = new MultiListener(finishLatch);

		handler.setTimeListener(Optional.of(listener));
		handler.setAlarmListener(Optional.of(listener));

		// final AtomicInteger
		final Thread first = new Thread(() -> {
			try {
				assertDoesNotThrow(() -> {
					threadsAliveLatch.countDown();
					startLatch.await();

					handler.timeChanged(TIMEOFDAY_EXAMPLE);
				});
			} catch (final AssertionError t) {
				failure.set(t);
			}
		});

		final Thread second = new Thread(() -> {
			try {
				assertDoesNotThrow(() -> {
					threadsAliveLatch.countDown();
					startLatch.await();
					handler.alarmChanged("disarmed");
				});
			} catch (final AssertionError t) {
				failure.set(t);
			}
		});

		// ensure all started at the same time.
		first.start();
		second.start();
		threadsAliveLatch.await(1, TimeUnit.MINUTES);
		startLatch.countDown();
		// wait until all done
		finishLatch.await(1, TimeUnit.MINUTES);

		if (failure.get() != null) {
			throw failure.get();
		}

		// both executed
		assertEquals(2, listener.total.get());
		// max at the same time: 1, so nothing in parallel
		assertEquals(1, listener.max.get());
	}

	@Test
	public void submitAfterStop() {
		handler.setSynchronous();

		assertDoesNotThrow(() -> handler.stop(1, TimeUnit.SECONDS));

		assertThrows(RejectedExecutionException.class, () -> handler.timeChanged(TIMEOFDAY_EXAMPLE));
	}

	@Test
	public void handlesTime_noListener_returnsFalse() {
		assertFalse(handler.handlesTime());
	}

	@Test
	public void handlesTime_listenerPresent_returnsTrue() {
		handler.setTimeListener(Optional.of(mock(IDomoticzTimeOfDayChanged.class)));
		assertTrue(handler.handlesTime());
	}

	@Test
	public void handlesAlarm_noListener_returnsFalse() {
		assertFalse(handler.handlesAlarm());
	}

	@Test
	public void handlesAlarm_listenerPresent_returnsTrue() {
		handler.setAlarmListener(Optional.of(mock(IDomoticzAlarmChanged.class)));
		assertTrue(handler.handlesAlarm());
	}

	@Test
	public void handlesDevice_noListener_returnsFalse() {
		assertFalse(handler.handlesDevice(42));
	}

	@Test
	public void handlesDevice_unknownDevice_returnsFalse() {
		handler.setDeviceListener(Optional.of(mock(IDomoticzDeviceStatusChanged.class)));

		assertFalse(handler.handlesDevice(42));
	}

	@Test
	public void handlesDevice_deviceKnown_returnsTrue() {
		handler.setDeviceListener(Optional.of(mock(IDomoticzDeviceStatusChanged.class)));
		when(registry.devicePresent(42)).thenReturn(true);

		assertTrue(handler.handlesDevice(42));
	}

}