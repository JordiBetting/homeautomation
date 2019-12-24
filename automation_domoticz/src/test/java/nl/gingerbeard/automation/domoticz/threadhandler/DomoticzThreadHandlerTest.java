package nl.gingerbeard.automation.domoticz.threadhandler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.domoticz.api.DomoticzException;
import nl.gingerbeard.automation.domoticz.api.IDomoticzAlarmChanged;
import nl.gingerbeard.automation.domoticz.api.IDomoticzDeviceStatusChanged;
import nl.gingerbeard.automation.domoticz.api.IDomoticzTimeOfDayChanged;
import nl.gingerbeard.automation.domoticz.clients.AlarmStateClient;
import nl.gingerbeard.automation.domoticz.clients.GetDeviceClient;
import nl.gingerbeard.automation.domoticz.clients.TimeOfDayClient;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.sync.AlarmSync;
import nl.gingerbeard.automation.domoticz.sync.DeviceSync;
import nl.gingerbeard.automation.domoticz.sync.DeviceSyncTest;
import nl.gingerbeard.automation.domoticz.sync.SyncAll;
import nl.gingerbeard.automation.domoticz.sync.TimeSync;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.OnOffState;
import nl.gingerbeard.automation.state.State;
import nl.gingerbeard.automation.state.TimeOfDayValues;

public class DomoticzThreadHandlerTest {

	private static final TimeOfDayValues TIMEOFDAY_EXAMPLE = new TimeOfDayValues(125, 50, 200, 190, 135);

	private ILogger logger;
	private IDeviceRegistry registry;
	private DomoticzThreadHandler handler;

	private DomoticzConfiguration config;

	private TimeOfDayClient todClient;

	private AlarmStateClient alarmClient;
	
	private SyncAll syncAll;
	
	private IState state;

	private GetDeviceClient deviceClient;

	public void create(boolean synchronous) throws IOException {
		createMocks(synchronous);
		handler = new DomoticzThreadHandler(config, registry, state, logger, todClient, syncAll);
	}

	private void createMocks(boolean synchronous) throws IOException {
		config = new DomoticzConfiguration(0, null);
		if (synchronous) {
			config.setEventHandlingSynchronous();
		}
		state = new State();
		logger = mock(ILogger.class);
		registry = mock(IDeviceRegistry.class);
		todClient = mock(TimeOfDayClient.class);
		alarmClient = mock(AlarmStateClient.class);
		deviceClient = mock(GetDeviceClient.class);
		syncAll = new SyncAll(registry, //
				new AlarmSync(state, alarmClient),
				new DeviceSync(deviceClient, registry, logger), //
				new TimeSync(state, todClient));
	}

	@AfterEach
	public void shutdown() throws IOException, InterruptedException {
		create(false);

		handler.stop(3, TimeUnit.SECONDS);
	}

	@Test
	public void sync_time_nolistener_noexception() throws IOException, InterruptedException, DomoticzException {
		create(false);

		handler.timeChanged(TIMEOFDAY_EXAMPLE);
	}

	@Test
	public void sync_time_listener_listenerCalled() throws IOException, InterruptedException, DomoticzException {
		create(true);
		final IDomoticzTimeOfDayChanged listener = mock(IDomoticzTimeOfDayChanged.class);

		handler.setTimeListener(listener);
		handler.timeChanged(TIMEOFDAY_EXAMPLE);

		verify(listener, times(1)).timeChanged(TIMEOFDAY_EXAMPLE);
		verifyNoMoreInteractions(listener);
	}

	@Test
	public void sync_time_invalidInput_exceptionThrown() throws IOException {
		create(false);

		assertThrows(IllegalArgumentException.class, () -> handler.timeChanged(null));
	}

	@Test
	public void sync_alarm_nolistener_noException() throws IOException, InterruptedException, DomoticzException {
		create(false);

		handler.alarmChanged("disarmed");
	}

	@Test
	public void sync_alarm_invalidValue_exceptionThrown() throws IOException {
		create(false);

		assertThrows(IllegalArgumentException.class, () -> handler.alarmChanged("doesNotExist"));
	}

	@Test
	public void sync_alarm_listener_listenerCalled() throws IOException, InterruptedException, DomoticzException {
		create(true);
		final IDomoticzAlarmChanged listener = mock(IDomoticzAlarmChanged.class);

		handler.setAlarmListener(listener);
		handler.alarmChanged("arm_away");

		verify(listener, times(1)).alarmChanged(AlarmState.ARM_AWAY);
		verifyNoMoreInteractions(listener);
	}

	@Test
	public void sync_device_nolistener_noException() throws IOException, InterruptedException, DomoticzException {
		create(false);

		handler.deviceChanged(1, "On");
	}

	@Test
	public void sync_device_nolistener_deviceUpdated() throws IOException, InterruptedException, DomoticzException {
		create(true);

		handler.deviceChanged(1, "on");

		verify(registry, times(1)).getDeviceState(1);
		verify(registry, times(1)).updateDevice(1, "on");
		verifyNoMoreInteractions(registry);
	}

	@Test
	public void sync_device_invalidInput_throwsException() throws IOException {
		create(false);

		assertThrows(IllegalArgumentException.class, () -> handler.deviceChanged(1, null));
	}

	@Test
	public void sync_device_listener_listenerCalled() throws IOException, InterruptedException, DomoticzException {
		create(true);
		final IDomoticzDeviceStatusChanged listener = mock(IDomoticzDeviceStatusChanged.class);
		final Switch device = new Switch(1);
		when(registry.updateDevice(1, "on")).thenReturn(Optional.of(device));
		when(registry.updateDevice(1, "on")).thenAnswer((args) -> {
			device.setState(OnOffState.ON);
			return Optional.of(device);
		});

		handler.setDeviceListener(listener);

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
		create(false);
		final AtomicReference<AssertionError> failure = new AtomicReference<>();

		final CountDownLatch threadsAliveLatch = new CountDownLatch(2);
		final CountDownLatch startLatch = new CountDownLatch(1);
		final CountDownLatch finishLatch = new CountDownLatch(2);
		final MultiListener listener = new MultiListener(finishLatch);

		handler.setTimeListener(listener);
		handler.setAlarmListener(listener);

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
					handler.alarmChanged("arm_away");
				});
			} catch (final AssertionError t) {
				failure.set(t);
			}
		});

		// ensure all started at the same time.
		first.start();
		second.start();
		assertTrue(threadsAliveLatch.await(1, TimeUnit.MINUTES));
		startLatch.countDown();
		// wait until all done
		assertTrue(finishLatch.await(1, TimeUnit.MINUTES));

		if (failure.get() != null) {
			throw failure.get();
		}

		// both executed
		assertEquals(2, listener.total.get());
		// max at the same time: 1, so nothing in parallel
		assertEquals(1, listener.max.get());
	}

	@Test
	public void submitAfterStop() throws IOException {
		create(false);

		assertDoesNotThrow(() -> handler.stop(1, TimeUnit.SECONDS));

		assertThrows(RejectedExecutionException.class, () -> handler.timeChanged(TIMEOFDAY_EXAMPLE));
	}

	@Test
	public void handlesTime_noListener_returnsFalse() throws IOException {
		create(false);
		assertFalse(handler.handlesTime());
	}

	@Test
	public void handlesTime_listenerPresent_returnsTrue() throws IOException {
		create(false);
		handler.setTimeListener(mock(IDomoticzTimeOfDayChanged.class));
		assertTrue(handler.handlesTime());
	}

	@Test
	public void handlesAlarm_noListener_returnsFalse() throws IOException {
		create(false);
		assertFalse(handler.handlesAlarm());
	}

	@Test
	public void handlesAlarm_listenerPresent_returnsTrue() throws IOException {
		create(false);
		handler.setAlarmListener(mock(IDomoticzAlarmChanged.class));
		assertTrue(handler.handlesAlarm());
	}

	@Test
	public void handlesDevice_noListener_returnsFalse() throws IOException {
		create(false);
		assertFalse(handler.handlesDevice(42));
	}

	@Test
	public void handlesDevice_unknownDevice_returnsFalse() throws IOException {
		create(false);
		handler.setDeviceListener(mock(IDomoticzDeviceStatusChanged.class));

		assertFalse(handler.handlesDevice(42));
	}

	@Test
	public void handlesDevice_deviceKnown_returnsTrue() throws IOException {
		create(false);
		handler.setDeviceListener(mock(IDomoticzDeviceStatusChanged.class));
		when(registry.devicePresent(42)).thenReturn(true);

		assertTrue(handler.handlesDevice(42));
	}

	@Test
	public void syncFull_retrievesAlarmAndTime() throws IOException, DomoticzException, InterruptedException {
		create(true);
		when(todClient.createTimeOfDayValues()).thenReturn(new TimeOfDayValues(1, 2, 3, 4, 5));
		when(alarmClient.getAlarmState()).thenReturn(AlarmState.DISARMED);

		handler.syncFull();

		verify(todClient, times(1)).createTimeOfDayValues();
		verify(alarmClient, times(1)).getAlarmState();
	}



	@Test
	public void deviceUpdated_noOldState_works() throws IOException, InterruptedException, DomoticzException {
		create(true);
		IDomoticzDeviceStatusChanged deviceListener = mock(IDomoticzDeviceStatusChanged.class);
		handler.setDeviceListener(deviceListener);

		when(registry.devicePresent(1)).thenReturn(true);
		when(registry.updateDevice(1, "on")).thenAnswer((args) -> {
			Switch s = new Switch(1);
			s.setState(OnOffState.ON);
			return Optional.of(s);
		});

		handler.deviceChanged(1, "on");

		verify(deviceListener, times(1)).statusChanged(any());
	}

	@Test
	public void deviceUpdated_sameState_noUpdate() throws IOException, InterruptedException, DomoticzException {
		create(true);
		IDomoticzDeviceStatusChanged deviceListener = mock(IDomoticzDeviceStatusChanged.class);
		handler.setDeviceListener(deviceListener);

		when(registry.devicePresent(1)).thenReturn(true);
		when(registry.updateDevice(1, "on")).thenAnswer((args) -> {
			Switch s = new Switch(1);
			s.setState(OnOffState.ON);
			return Optional.of(s);
		});
		when(registry.getDeviceState(1)).thenAnswer((args) -> {
			return Optional.of(OnOffState.ON);
		});

		handler.deviceChanged(1, "on");

		verify(deviceListener, times(0)).statusChanged(any());
	}

	@Test
	public void interruptedTask() throws IOException, InterruptedException, DomoticzException {
		create(true);

		assertThrows(InterruptedException.class, () -> handler.execute(() -> {
			throw new DomoticzException(new InterruptedException());
		}));
	}


	@Test
	public void execute_async_failed_exceptionLogged() throws IOException, InterruptedException, DomoticzException {
		create(false);
		
		final CountDownLatch latch = new CountDownLatch(1);
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				latch.countDown();
				return null;
			}}).when(logger).exception(any(), any());
		
		final DomoticzException exc = new DomoticzException("Test exception");
		handler.execute(() -> { throw exc; });
		
		latch.await(1, TimeUnit.MINUTES);
		verify(logger, times(1)).exception(exc, "Failed to execute command");
	}
	
	@Test
	public void execute_sync_throwsDomoticzException_caught() throws IOException {
		create(true);
		
		DomoticzException e = assertThrows(DomoticzException.class, () -> handler.execute(() -> {throw new DomoticzException("TEST");}));
		assertEquals("TEST", e.getMessage());
	}
	
}
