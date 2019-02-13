package nl.gingerbeard.automation.domoticz.hil;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.ThermostatModeDevice;
import nl.gingerbeard.automation.devices.ThermostatSetpointDevice;
import nl.gingerbeard.automation.domoticz.transmitter.DomoticzUpdateTransmitter;
import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;
import nl.gingerbeard.automation.logging.TestLogger;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.Temperature.Unit;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;

@Disabled
public class HeatingHilTest extends DomoticzHILIntegrationTest {
	@Test
	public void setHeatingSetpointLivingRoom() throws IOException {
		final IDomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig, new TestLogger());
		final ThermostatSetpointDevice device = new ThermostatSetpointDevice(471);

		transmitter.transmitDeviceUpdate(new NextState<>(device, new Temperature(22, Unit.CELSIUS)));
	}

	@Test
	public void setHeatingModeLivingRoom() throws IOException {
		final IDomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig, new TestLogger());
		final ThermostatModeDevice device = new ThermostatModeDevice(469);

		transmitter.transmitDeviceUpdate(new NextState<>(device, ThermostatMode.FULL_HEAT));
	}
}