package nl.gingerbeard.automation.domoticz;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
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

	// TODO: test handlesTime,alarm,device

	@Test
	public void async() {
		// TODO
		// 2 parallel calls to handler
		// stop-go (countdownlatch) to release both at the same time
		// test that these are executed in sequence (HOW?)
	}
}
