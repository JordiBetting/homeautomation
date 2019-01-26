package nl.gingerbeard.domotica;

import java.net.MalformedURLException;
import java.net.URL;

import nl.gingerbeard.automation.AutomationFrameworkContainer;
import nl.gingerbeard.automation.IAutomationFrameworkInterface;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.domotica.rooms.Gang;

public class MainClass {

	static AutomationFrameworkContainer container;

	public static void main(final String[] args) throws InterruptedException, MalformedURLException {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Stopping");
				container.stop();
			}
		});
		final DomoticzConfiguration config = new DomoticzConfiguration(8081, new URL("http://home.gingerbread.nl:8080"));
		container = IAutomationFrameworkInterface.createFrameworkContainer(config);
		container.start();
		System.out.println("Running");
		container.getAutomationFramework().addRoom(new Gang());
		System.out.println("Configured");
		while (true) {
			Thread.sleep(Long.MAX_VALUE);
		}
	}
}
