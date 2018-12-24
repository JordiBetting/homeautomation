package nl.gingerbeard.automation.domoticz;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.DimmeableLight;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.transmitter.DomoticzUpdateTransmitter;
import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;

@Disabled
public class IntegrationTest {

	private static DomoticzConfiguration domoticzConfig;

	@BeforeAll
	public static void initConfig() throws MalformedURLException {
		domoticzConfig = new DomoticzConfiguration(0, new URL("http://192.168.2.204:8080"));
	}

	@Test
	public void light_bank_on() throws IOException {
		final IDomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig);
		final DimmeableLight device = new DimmeableLight(274);

		device.updateState("10");
		transmitter.transmitDeviceUpdate(device);
	}

}
