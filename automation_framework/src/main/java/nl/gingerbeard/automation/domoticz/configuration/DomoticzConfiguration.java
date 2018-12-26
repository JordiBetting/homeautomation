package nl.gingerbeard.automation.domoticz.configuration;

import java.net.URL;

public final class DomoticzConfiguration {

	private int listenPort;
	private final URL baseURL;

	public DomoticzConfiguration(final int listenPort, final URL baseURL) {
		this.listenPort = listenPort;
		this.baseURL = baseURL;
	}

	/**
	 * Returns port provided in constuctor. If 0 was provided in constructor, this method returns the actualy listen port.
	 * 
	 * @return
	 */
	public int getListenPort() {
		return listenPort;
	}

	public URL getBaseURL() {
		return baseURL;
	}

	/**
	 * Used internally.
	 * 
	 * @param listenPort
	 */
	public void updateListenPort(final int listenPort) {
		this.listenPort = listenPort;
	}

}
