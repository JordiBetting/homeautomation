package nl.gingerbeard.automation.domoticz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import nl.gingerbeard.automation.deviceregistry.DeviceRegistry;
import nl.gingerbeard.automation.devices.OnOffDevice;
import nl.gingerbeard.automation.devices.StateDevice;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.OnOffState;

public class DomoticzTest {

	private DeviceRegistry registry;
	private Domoticz domoticz;

	@BeforeEach
	public void initDomoticz() {
		registry = new DeviceRegistry();
		domoticz = new Domoticz(registry);
	}

	@Test
	public void updateDevice_deviceUpdated() {
		final OnOffDevice device = new Switch(1);
		device.setState(OnOffState.OFF);
		registry.addDevice(device);

		domoticz.deviceChanged(1, "on");

		assertEquals(OnOffState.ON, device.getState());
	}

	@Test
	public void updateNotExistingDevice_noException() {
		domoticz.deviceChanged(1, "does not exist");
	}

	@Test
	public void noListener_deviceChanged_noException() {
		registry.addDevice(new Switch(1));

		final boolean result = domoticz.deviceChanged(1, "on");

		assertTrue(result);
	}

	@Test
	public void update_invalidNewState_returnsFalse() {
		registry.addDevice(new Switch(1));

		final boolean result = domoticz.deviceChanged(1, "does not exist");

		assertFalse(result);
	}

	private static class TestListener implements IDomoticzDeviceStatusChanged {

		private final List<StateDevice<?>> receivedDeviceUpdates = new ArrayList<>();

		@Override
		public void statusChanged(final StateDevice<?> device) {
			receivedDeviceUpdates.add(device);
		}
	}

	@Test
	public void update_devicelistenerCalled() {
		final TestListener listener = new TestListener();
		final Domoticz domoticz = new Domoticz(Optional.of(listener), Optional.empty(), Optional.empty(), (t, level, message) -> {
		}, registry);
		registry.addDevice(new Switch(1));

		final boolean result = domoticz.deviceChanged(1, "on");

		assertTrue(result);
		assertEquals(1, listener.receivedDeviceUpdates.size());
	}

	@Test
	public void update_timelistenerCalled() {
		final IDomoticzTimeOfDayChanged listener = mock(IDomoticzTimeOfDayChanged.class);
		final Domoticz domoticz = new Domoticz(Optional.empty(), Optional.of(listener), Optional.empty(), mock(ILogger.class), registry);

		domoticz.timeChanged(1, 2, 3);

		Mockito.verify(listener, times(1)).timeChanged(any());
		verifyNoMoreInteractions(listener);
	}

	@Test
	public void update_timelistener_returnsTrue() {
		final IDomoticzTimeOfDayChanged listener = mock(IDomoticzTimeOfDayChanged.class);
		final Domoticz domoticz = new Domoticz(Optional.empty(), Optional.of(listener), Optional.empty(), mock(ILogger.class), registry);

		when(listener.timeChanged(any())).thenReturn(true);

		final boolean result = domoticz.timeChanged(1, 2, 3);

		assertTrue(result);
	}

	@Test
	public void update_timelistener_returnsFalse() {
		final IDomoticzTimeOfDayChanged listener = mock(IDomoticzTimeOfDayChanged.class);
		final Domoticz domoticz = new Domoticz(Optional.empty(), Optional.of(listener), Optional.empty(), mock(ILogger.class), registry);

		when(listener.timeChanged(any())).thenReturn(false);

		final boolean result = domoticz.timeChanged(1, 2, 3);

		assertFalse(result);
	}

	@Test
	public void update_timeListener_noListener_noException() {
		final Domoticz domoticz = new Domoticz(Optional.empty(), Optional.empty(), Optional.empty(), mock(ILogger.class), registry);

		final boolean result = domoticz.timeChanged(1, 2, 3);

		assertFalse(result);
	}

	@Test
	public void update_alarm_away_ListenerCalled() {
		final IDomoticzAlarmChanged listener = mock(IDomoticzAlarmChanged.class);
		final Domoticz domoticz = new Domoticz(Optional.empty(), Optional.empty(), Optional.of(listener), mock(ILogger.class), registry);

		domoticz.alarmChanged("arm_away");

		Mockito.verify(listener, times(1)).alarmChanged(AlarmState.ARM_AWAY);
		verifyNoMoreInteractions(listener);
	}

	@Test
	public void update_alarm_home_ListenerCalled() {
		final IDomoticzAlarmChanged listener = mock(IDomoticzAlarmChanged.class);
		final Domoticz domoticz = new Domoticz(Optional.empty(), Optional.empty(), Optional.of(listener), mock(ILogger.class), registry);

		domoticz.alarmChanged("arm_home");

		Mockito.verify(listener, times(1)).alarmChanged(AlarmState.ARM_HOME);
		verifyNoMoreInteractions(listener);
	}

	@Test
	public void update_alarm_disarm_ListenerCalled() {
		final IDomoticzAlarmChanged listener = mock(IDomoticzAlarmChanged.class);
		final Domoticz domoticz = new Domoticz(Optional.empty(), Optional.empty(), Optional.of(listener), mock(ILogger.class), registry);

		domoticz.alarmChanged("disarmed");

		Mockito.verify(listener, times(1)).alarmChanged(AlarmState.DISARMED);
		verifyNoMoreInteractions(listener);
	}

	@Test
	public void update_alarmlistenre_returnsTrue() {
		final IDomoticzAlarmChanged listener = mock(IDomoticzAlarmChanged.class);
		when(listener.alarmChanged(any())).thenReturn(true);
		final Domoticz domoticz = new Domoticz(Optional.empty(), Optional.empty(), Optional.of(listener), mock(ILogger.class), registry);

		final boolean result = domoticz.alarmChanged("disarmed");

		assertTrue(result);
	}

	@Test
	public void update_alarmlistenre_returnsFalse() {
		final IDomoticzAlarmChanged listener = mock(IDomoticzAlarmChanged.class);
		when(listener.alarmChanged(any())).thenReturn(false);
		final Domoticz domoticz = new Domoticz(Optional.empty(), Optional.empty(), Optional.of(listener), mock(ILogger.class), registry);

		final boolean result = domoticz.alarmChanged("disarmed");

		assertFalse(result);
	}

	@Test
	public void update_alarm_notlistener_noException() {
		final Domoticz domoticz = new Domoticz(Optional.empty(), Optional.empty(), Optional.empty(), mock(ILogger.class), registry);

		final boolean result = domoticz.alarmChanged("disarmed");

		assertFalse(result);
	}

}
