package nl.gingerbeard.automation.components;

import java.io.IOException;
import java.util.List;

import nl.gingerbeard.automation.configuration.ConfigurationServer;
import nl.gingerbeard.automation.configuration.ConfigurationServerSettings;
import nl.gingerbeard.automation.configuration.IConfigurationProvider;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Requires;

public class ConfigurationServerComponent {

	@Requires
	public ConfigurationServerSettings settings;

	@Requires
	public IEvents events;

	private ConfigurationServer server;
	private Provider provider;

	// TODO: It now becomes really important to restructure the controller as Domoticz owns threading of the system
	// and this just bypasses the controller. Not a clean design.

	@Activate
	public void createServer() throws IOException {
		provider = new Provider();
		server = new ConfigurationServer(settings, provider);
	}

	@Deactivate
	public void stopServer() {
		server.stop();
		server = null;
	}

	private class Provider implements IConfigurationProvider {

		@Override
		public void disable(final String room) {
			events.disable(room);
		}

		@Override
		public void enable(final String room) {
			events.enable(room);
		}

		@Override
		public List<String> getRooms() {
			return events.getSubscribers();
		}

	}

}
