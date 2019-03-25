package nl.gingerbeard.automation.domoticz.configuration;

import java.net.URL;

public final class DomoticzConfiguration {

	private int listenPort;
	private final URL baseURL;
	private int connectTimeoutMS = DEFAULT_CONNECT_TIMEOUT_MS;

	public static final int DEFAULT_CONNECT_TIMEOUT_MS = 3000;

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

	public int getConnectTimeoutMS() {
		return connectTimeoutMS;
	}

	/**
	 * Sets timeout for connection in milliseconds. Default value is specified in {@link #DEFAULT_CONNECT_TIMEOUT_MS}
	 *
	 * @param connectTimeoutMS
	 *            Timeout in milliseconds, 0 for disable timeout.
	 */
	public void setConnectTimeoutMS(final int connectTimeoutMS) {
		this.connectTimeoutMS = connectTimeoutMS;
	}

}
