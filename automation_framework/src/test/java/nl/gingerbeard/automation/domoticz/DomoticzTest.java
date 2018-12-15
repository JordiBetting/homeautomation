package nl.gingerbeard.automation.domoticz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.OnOffDevice;
import nl.gingerbeard.automation.devices.OnOffDevice.OnOff;
import nl.gingerbeard.automation.devices.Switch;

public class DomoticzTest {

	@Test
	public void updateDevice_deviceUpdated() {
		final Domoticz domoticz = new Domoticz((e) -> {
		});

		final OnOffDevice device = new Switch(1, Optional.empty());
		device.updateState(OnOff.OFF.name());
		domoticz.addDevice(device);

		assertEquals(OnOff.OFF, device.getState());
		domoticz.deviceChanged(1, "on");
		assertEquals(OnOff.ON, device.getState());
	}

	@Test
	public void updateNotExistingDevice_noException() {
		final Domoticz domoticz = new Domoticz((e) -> {
		});

		domoticz.deviceChanged(1, "does not exist");
	}

	@Test
	public void addDevice_twice_fails() {
		final Domoticz domoticz = new Domoticz((e) -> {
		});

		final Switch switch1 = new Switch(1, Optional.empty());
		boolean result = domoticz.addDevice(switch1);
		assertTrue(result);
		result = domoticz.addDevice(switch1);
		assertFalse(result);
	}

	@Test
	public void addDevices_sameidx_fails() {
		final Domoticz domoticz = new Domoticz((e) -> {
		});

		final Switch switch1 = new Switch(1, Optional.empty());
		final Switch switch2 = new Switch(1, Optional.empty());
		boolean result = domoticz.addDevice(switch1);
		assertTrue(result);
		result = domoticz.addDevice(switch2);
		assertFalse(result);
	}
}
