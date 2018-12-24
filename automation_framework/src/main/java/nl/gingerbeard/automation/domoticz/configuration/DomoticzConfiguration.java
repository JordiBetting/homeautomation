package nl.gingerbeard.automation.domoticz.configuration;

import java.net.URL;

public final class DomoticzConfiguration {

	private final int listenPort;
	private final URL baseURL;

	public DomoticzConfiguration(final int listenPort, final URL baseURL) {
		this.listenPort = listenPort;
		this.baseURL = baseURL;
	}

	public int getListenPort() {
		return listenPort;
	}

	public URL getBaseURL() {
		return baseURL;
	}

}
