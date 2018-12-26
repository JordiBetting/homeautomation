package nl.gingerbeard.automation;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.helpers.TestWebServer;
import nl.gingerbeard.automation.service.Container;

public class IntegrationTest {

	private static TestWebServer webserver;
	private static int port;
	private static DomoticzConfiguration config;
	private static Container container;

	// TODO: use local webserver, trigger transmitter, ensure room updates actuator based on sensor value.

	@BeforeEach
	public static void start() throws MalformedURLException {
		webserver = new TestWebServer();
		config = new DomoticzConfiguration(0, new URL("http://localhost:" + webserver.getListeningPort()));
		container = AutomationFrameworkInterface.createFrameworkContainer();
		container.register(DomoticzConfiguration.class, config);
	}

	@AfterEach
	public static void stop() {
		container.shutDown();
		container = null;

		webserver.stop();
		webserver = null;
	}

	@Test
	public void actuatorUpdatedBySensorUpdate() {
	}

}
