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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import nl.gingerbeard.automation.devices.OnOffDevice;
import nl.gingerbeard.automation.devices.StateDevice;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.OnOffState;

public class DomoticzTest {

	@Test
	public void updateDevice_deviceUpdated() {
		final Domoticz domoticz = new Domoticz();

		final OnOffDevice device = new Switch(1);
		device.updateState(OnOffState.OFF.name());
		domoticz.addDevice(device);

		assertEquals(OnOffState.OFF, device.getState());
		domoticz.deviceChanged(1, "on");
		assertEquals(OnOffState.ON, device.getState());
	}

	@Test
	public void updateNotExistingDevice_noException() {
		final Domoticz domoticz = new Domoticz();

		domoticz.deviceChanged(1, "does not exist");
	}

	@Test
	public void addDevice_twice_fails() {
		final Domoticz domoticz = new Domoticz();
		final Switch switch1 = new Switch(1);
		boolean result = domoticz.addDevice(switch1);
		assertTrue(result);

		result = domoticz.addDevice(switch1);

		assertFalse(result);
	}

	@Test
	public void addDevices_sameidx_fails() {
		final Domoticz domoticz = new Domoticz();
		final Switch switch1 = new Switch(1);
		final Switch switch2 = new Switch(1);
		boolean result = domoticz.addDevice(switch1);
		assertTrue(result);

		result = domoticz.addDevice(switch2);

		assertFalse(result);
	}

	@Test
	public void noListener_deviceChanged_noException() {
		final Domoticz domoticz = new Domoticz();
		domoticz.addDevice(new Switch(1));

		final boolean result = domoticz.deviceChanged(1, "on");

		assertTrue(result);
	}

	@Test
	public void update_invalidNewState_returnsFalse() {
		final Domoticz domoticz = new Domoticz();
		domoticz.addDevice(new Switch(1));

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
		final Domoticz domoticz = new Domoticz(Optional.of(listener), Optional.empty(), (t, level, message) -> {
		});
		domoticz.addDevice(new Switch(1));

		final boolean result = domoticz.deviceChanged(1, "on");

		assertTrue(result);
		assertEquals(1, listener.receivedDeviceUpdates.size());
	}

	@Test
	public void update_timelistenerCalled() {
		final IDomoticzTimeOfDayChanged listener = mock(IDomoticzTimeOfDayChanged.class);
		final Domoticz domoticz = new Domoticz(Optional.empty(), Optional.of(listener), mock(ILogger.class));

		domoticz.timeChanged(1, 2, 3);

		Mockito.verify(listener, times(1)).timeChanged(any());
		verifyNoMoreInteractions(listener);
	}

	@Test
	public void update_timelistener_returnsTrue() {
		final IDomoticzTimeOfDayChanged listener = mock(IDomoticzTimeOfDayChanged.class);
		final Domoticz domoticz = new Domoticz(Optional.empty(), Optional.of(listener), mock(ILogger.class));

		when(listener.timeChanged(any())).thenReturn(true);

		final boolean result = domoticz.timeChanged(1, 2, 3);

		assertTrue(result);
	}

	@Test
	public void update_timelistener_returnsFalse() {
		final IDomoticzTimeOfDayChanged listener = mock(IDomoticzTimeOfDayChanged.class);
		final Domoticz domoticz = new Domoticz(Optional.empty(), Optional.of(listener), mock(ILogger.class));

		when(listener.timeChanged(any())).thenReturn(false);

		final boolean result = domoticz.timeChanged(1, 2, 3);

		assertFalse(result);
	}

	@Test
	public void update_timeListener_noListener_noException() {
		final Domoticz domoticz = new Domoticz(Optional.empty(), Optional.empty(), mock(ILogger.class));

		final boolean result = domoticz.timeChanged(1, 2, 3);

		assertFalse(result);
	}
}
