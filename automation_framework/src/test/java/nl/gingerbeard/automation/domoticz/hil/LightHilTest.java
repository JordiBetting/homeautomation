package nl.gingerbeard.automation.domoticz.hil;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.DimmeableLight;
import nl.gingerbeard.automation.domoticz.api.DomoticzException;
import nl.gingerbeard.automation.domoticz.transmitter.DomoticzUpdateTransmitter;
import nl.gingerbeard.automation.logging.TestLogger;
import nl.gingerbeard.automation.state.Level;
import nl.gingerbeard.automation.state.NextState;

@Disabled
public class LightHilTest extends DomoticzHILIntegrationTest {
	@Test
	public void light_bank_on() throws IOException, DomoticzException {
		final DomoticzUpdateTransmitter transmitter = new DomoticzUpdateTransmitter(domoticzConfig, new TestLogger());
		final DimmeableLight device = new DimmeableLight(274);

		transmitter.transmitDeviceUpdate(new NextState<>(device, new Level(10)));
	}
}
