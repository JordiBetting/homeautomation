package nl.gingerbeard.automation.domoticz;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.DimmeableLight;
import nl.gingerbeard.automation.devices.ThermostatModeDevice;
import nl.gingerbeard.automation.devices.ThermostatSetpointDevice;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.transmitter.DomoticzUpdateTransmitter;
import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;
import nl.gingerbeard.automation.logging.TestLogger;
import nl.gingerbeard.automation.state.Level;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.Temperature.Unit;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;

@Disabled
public class DomoticzHILIntegrationTest {

	private static DomoticzConfiguration domoticzConfig;

	@BeforeAll
	public static void initConfig() throws MalformedURLException {
		domoticzConfig = new DomoticzConfiguration(0, new URL("http://192.168.2.204:8080"));
	}

	@Test
	public void light_bank_on() throws IOException {
		final IDomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig, new TestLogger());
		final DimmeableLight device = new DimmeableLight(274);

		transmitter.transmitDeviceUpdate(new NextState<>(device, new Level(10)));
	}

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
