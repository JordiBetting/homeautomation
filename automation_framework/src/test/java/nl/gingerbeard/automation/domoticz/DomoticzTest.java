package nl.gingerbeard.automation.domoticz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.OnOffDevice;
import nl.gingerbeard.automation.devices.StateDevice;
import nl.gingerbeard.automation.devices.Switch;
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
	public void update_listenerCalled() {
		final TestListener listener = new TestListener();
		final Domoticz domoticz = new Domoticz(Optional.of(listener), (t, level, message) -> {
		});
		domoticz.addDevice(new Switch(1));

		final boolean result = domoticz.deviceChanged(1, "on");

		assertTrue(result);
		assertEquals(1, listener.receivedDeviceUpdates.size());
	}
}
