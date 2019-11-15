package nl.gingerbeard.automation.domoticz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import nl.gingerbeard.automation.deviceregistry.DeviceRegistry;
import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.devices.OnOffDevice;
import nl.gingerbeard.automation.devices.StateDevice;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.domoticz.api.DomoticzException;
import nl.gingerbeard.automation.domoticz.api.IDomoticzAlarmChanged;
import nl.gingerbeard.automation.domoticz.api.IDomoticzDeviceStatusChanged;
import nl.gingerbeard.automation.domoticz.api.IDomoticzTimeOfDayChanged;
import nl.gingerbeard.automation.domoticz.clients.AlarmStateClient;
import nl.gingerbeard.automation.domoticz.clients.TimeOfDayClient;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.logging.LogLevel;
import nl.gingerbeard.automation.logging.TestLogger;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.OnOffState;
import nl.gingerbeard.automation.state.TimeOfDayValues;

public class DomoticzTest {
//
//	private IDeviceRegistry registry;
//	private Domoticz domoticz;
//	private IState state;
//	private ILogger logger;
//	private DomoticzConfiguration config;
//
//	@BeforeEach
//	public void initDomoticz() {
//		registry = new DeviceRegistry();
//		logger = mock(ILogger.class);
//		state = mock(IState.class);
//		config = mock(DomoticzConfiguration.class);
//		final DomoticzThreadHandler domoticzThreadHandler = new DomoticzThreadHandler(config, registry, state, logger);
//		domoticz = new Domoticz(logger, domoticzThreadHandler, mock(TimeOfDayClient.class), mock(AlarmStateClient.class));
//	}
//
//	@Test
//	public void updateDevice_NoHandler_deviceNotUpdated() {
//		final OnOffDevice device = new Switch(1);
//		device.setState(OnOffState.OFF);
//		registry.addDevice(device);
//
//		domoticz.deviceChanged(1, "on");
//
//		assertEquals(OnOffState.OFF, device.getState());
//	}
//
//	@Test
//	public void updateNotExistingDevice_noException() {
//		domoticz.deviceChanged(1, "does not exist");
//	}
//
//	@Test
//	public void noListener_deviceChanged_noException() {
//		registry.addDevice(new Switch(1));
//
//		final boolean result = domoticz.deviceChanged(1, "on");
//
//		assertFalse(result);
//	}
//
//	@Test
//	public void update_invalidNewState_returnsFalse() {
//		registry.addDevice(new Switch(1));
//
//		final boolean result = domoticz.deviceChanged(1, "does not exist");
//
//		assertFalse(result);
//	}
//
//	private static class TestListener implements IDomoticzDeviceStatusChanged {
//
//		private final List<StateDevice<?>> receivedDeviceUpdates = new ArrayList<>();
//
//		@Override
//		public void statusChanged(final StateDevice<?> device) {
//			receivedDeviceUpdates.add(device);
//		}
//	}
//
//	@Test
//	public void update_devicelistenerCalled() {
//		final TestListener listener = new TestListener();
//		final ILogger log = mock(ILogger.class);
//		final DomoticzThreadHandler threadHandler = new DomoticzThreadHandler(createSyncConfig(), registry, state, logger);
//		final Domoticz domoticz = new Domoticz(log, threadHandler, null, null);
//		threadHandler.setDeviceListener(listener);
//
//		registry.addDevice(new Switch(1));
//
//		final boolean result = domoticz.deviceChanged(1, "on");
//
//		assertTrue(result);
//		assertEquals(1, listener.receivedDeviceUpdates.size());
//	}
//
//	private DomoticzConfiguration createSyncConfig() {
//		DomoticzConfiguration config = new DomoticzConfiguration(0, null);
//		config.setEventHandlingSynchronous();
//		return config;
//	}
//
//	@Test
//	public void update_timelistenerCalled() throws IOException {
//		final IDomoticzTimeOfDayChanged listener = mock(IDomoticzTimeOfDayChanged.class);
//		final ILogger log = mock(ILogger.class);
//		final DomoticzThreadHandler threadHandler = new DomoticzThreadHandler(createSyncConfig(), registry, state, logger);
//		final Domoticz domoticz = new Domoticz(log, threadHandler, createTimeOfDayMock(), null);
//		threadHandler.setTimeListener(listener);
//
//		domoticz.timeChanged(1, 2, 3);
//
//		Mockito.verify(listener, times(1)).timeChanged(any());
//		verifyNoMoreInteractions(listener);
//	}
//
//	private TimeOfDayClient createTimeOfDayMock() throws IOException {
//		final TimeOfDayClient tod = mock(TimeOfDayClient.class);
//		when(tod.createTimeOfDayValues()).thenReturn(new TimeOfDayValues(1, 2, 3, 4, 5));
//		return tod;
//	}
//
//	@Test
//	public void update_timeListener_noListener_noException() throws IOException {
//		final ILogger log = mock(ILogger.class);
//		final DomoticzThreadHandler threadHandler = new DomoticzThreadHandler(config, registry, state, logger);
//		final Domoticz domoticz = new Domoticz(log, threadHandler, createTimeOfDayMock(), null);
//
//		final boolean result = domoticz.timeChanged(1, 2, 3);
//
//		assertFalse(result);
//	}
//
//	@Test
//	public void update_alarm_away_ListenerCalled() {
//		final IDomoticzAlarmChanged listener = mock(IDomoticzAlarmChanged.class);
//		final ILogger log = mock(ILogger.class);
//		final DomoticzThreadHandler threadHandler = new DomoticzThreadHandler(createSyncConfig(), registry, state, logger);
//		final Domoticz domoticz = new Domoticz(log, threadHandler, null, null);
//		threadHandler.setAlarmListener(listener);
//
//		domoticz.alarmChanged("arm_away");
//
//		Mockito.verify(listener, times(1)).alarmChanged(AlarmState.ARM_AWAY);
//		verifyNoMoreInteractions(listener);
//	}
//
//	@Test
//	public void update_alarm_home_ListenerCalled() {
//		final IDomoticzAlarmChanged listener = mock(IDomoticzAlarmChanged.class);
//		final ILogger log = mock(ILogger.class);
//		final DomoticzThreadHandler threadHandler = new DomoticzThreadHandler(createSyncConfig(), registry, state, logger);
//		final Domoticz domoticz = new Domoticz(log, threadHandler, null, null);
//		threadHandler.setAlarmListener(listener);
//
//		domoticz.alarmChanged("arm_home");
//
//		Mockito.verify(listener, times(1)).alarmChanged(AlarmState.ARM_HOME);
//		verifyNoMoreInteractions(listener);
//	}
//
//	@Test
//	public void update_alarm_disarm_ListenerCalled() {
//		final IDomoticzAlarmChanged listener = mock(IDomoticzAlarmChanged.class);
//		final ILogger log = mock(ILogger.class);
//		final DomoticzThreadHandler threadHandler = new DomoticzThreadHandler(createSyncConfig(), registry, state, logger);
//		final Domoticz domoticz = new Domoticz(log, threadHandler, null, null);
//		threadHandler.setAlarmListener(listener);
//
//		domoticz.alarmChanged("disarmed");
//
//		Mockito.verify(listener, times(1)).alarmChanged(AlarmState.DISARMED);
//		verifyNoMoreInteractions(listener);
//	}
//
//	@Test
//	public void update_alarm_notlistener_noException() {
//		final ILogger log = mock(ILogger.class);
//		final DomoticzThreadHandler threadHandler = new DomoticzThreadHandler(config, registry, state, logger);
//		final Domoticz domoticz = new Domoticz(log, threadHandler, null, null);
//
//		final boolean result = domoticz.alarmChanged("disarmed");
//
//		assertFalse(result);
//	}
//
//	@Test
//	public void getCivilTwilightFails_warningLogged() throws IOException {
//		final TestLogger logger = new TestLogger();
//		final TimeOfDayClient todClient = mock(TimeOfDayClient.class);
//		when(todClient.createTimeOfDayValues()).thenThrow(new IOException("testing exception"));
//		final DomoticzThreadHandler threadHandler = new DomoticzThreadHandler(createSyncConfig(), registry, state, logger);
//		threadHandler.setTimeListener(mock(IDomoticzTimeOfDayChanged.class));
//		final Domoticz domoticz = new Domoticz(logger, threadHandler, todClient, null);
//
//		domoticz.timeChanged(1, 2, 3);
//
//		logger.assertContains(LogLevel.WARNING, "Failed retrieving time of day values");
//	}
//
//	@Test
//	public void alarmChanged_interrupted_warningLogged() throws InterruptedException, DomoticzException {
//		final TestLogger logger = new TestLogger();
//		final TimeOfDayClient todClient = mock(TimeOfDayClient.class);
//		final DomoticzThreadHandler threadHandler = mock(DomoticzThreadHandler.class);
//		doThrow(InterruptedException.class).when(threadHandler).alarmChanged(any());
//		when(threadHandler.handlesAlarm()).thenReturn(true);
//		final Domoticz domoticz = new Domoticz(logger, threadHandler, todClient, null);
//
//		domoticz.alarmChanged("disarmed");
//
//		logger.assertContains(LogLevel.WARNING, "Interrupted while changing alarm state");
//	}
//
//	@Test
//	public void timeChanged_interrupted_warningLogged() throws InterruptedException, DomoticzException {
//		final TestLogger logger = new TestLogger();
//		final TimeOfDayClient todClient = mock(TimeOfDayClient.class);
//		final DomoticzThreadHandler threadHandler = mock(DomoticzThreadHandler.class);
//		doThrow(InterruptedException.class).when(threadHandler).timeChanged(any());
//		when(threadHandler.handlesTime()).thenReturn(true);
//		final Domoticz domoticz = new Domoticz(logger, threadHandler, todClient, null);
//
//		domoticz.timeChanged(1, 2, 3);
//
//		logger.assertContains(LogLevel.WARNING, "Failed retrieving time of day values");
//	}
//
//	@Test
//	public void deviceChanged_interrupted_warningLogged() throws InterruptedException, DomoticzException {
//		final TestLogger logger = new TestLogger();
//		final TimeOfDayClient todClient = mock(TimeOfDayClient.class);
//		final DomoticzThreadHandler threadHandler = mock(DomoticzThreadHandler.class);
//		doThrow(InterruptedException.class).when(threadHandler).deviceChanged(anyInt(), any());
//		when(threadHandler.handlesDevice(anyInt())).thenReturn(true);
//		final Domoticz domoticz = new Domoticz(logger, threadHandler, todClient, null);
//
//		domoticz.deviceChanged(1, "on");
//
//		logger.assertContains(LogLevel.WARNING, "Interrupted while updating device");
//	}

}
