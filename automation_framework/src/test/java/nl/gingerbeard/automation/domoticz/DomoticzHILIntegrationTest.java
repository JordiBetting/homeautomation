package nl.gingerbeard.automation.domoticz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
import nl.gingerbeard.automation.state.Level;
import nl.gingerbeard.automation.state.NextState;

@Disabled
public class DomoticzHILIntegrationTest {

	private static DomoticzConfiguration domoticzConfig;

	@BeforeAll
	public static void initConfig() throws MalformedURLException {
		domoticzConfig = new DomoticzConfiguration(0, new URL("http://192.168.2.204:8080"));
	}

	@Test
	public void light_bank_on() throws IOException {
		final IDomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig);
		final DimmeableLight device = new DimmeableLight(274);

		transmitter.transmitDeviceUpdate(new NextState<>(device, new Level(10)));
	}

	private static class ErrorDimmeableLight extends DimmeableLight {

		public ErrorDimmeableLight(final int idx) {
			super(idx);
		}

	}

	@Test
	public void domoticz_returnError() throws IOException {
		final IDomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig);
		final ErrorDimmeableLight device = new ErrorDimmeableLight(274);

		try {
			transmitter.transmitDeviceUpdate(new NextState<>(device, new Level(10)));
			fail("Expected exception");
		} catch (final IOException e) {
			assertEquals("Failed setting value in domotics: {\"message\":\"Error sending switch command, check device\\/hardware !\",\"title\":\"SwitchLight\",\"status\":\"ERROR\"}", e.getMessage());
		}
	}

}
