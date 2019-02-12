package nl.gingerbeard.automation.domoticz.hil;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.BeforeAll;

import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;

public abstract class DomoticzHILIntegrationTest {

	protected static DomoticzConfiguration domoticzConfig;

	@BeforeAll
	public static void initConfig() throws MalformedURLException {
		domoticzConfig = new DomoticzConfiguration(0, new URL("http://192.168.2.204:8080"));
	}

}
