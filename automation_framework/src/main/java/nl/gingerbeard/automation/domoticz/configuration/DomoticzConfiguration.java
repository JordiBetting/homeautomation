package nl.gingerbeard.automation.domoticz.configuration;

import java.net.URL;

public final class DomoticzConfiguration {

	private int listenPort;
	private final URL baseURL;
	private int connectTimeoutMS = DEFAULT_CONNECT_TIMEOUT_MS;
	private boolean synchronousEventHandling = false;
	private int maxInitWait_s = 60 * 15; // TODO: Use Duration i.s.o. int_s
	private int initInterval_s = 5;

	public static final int DEFAULT_CONNECT_TIMEOUT_MS = 3000;

	public DomoticzConfiguration(final int listenPort, final URL baseURL) {
		this.listenPort = listenPort;
		this.baseURL = baseURL;
	}

	/**
	 * Returns port provided in constuctor. If 0 was provided in constructor, this
	 * method returns the actualy listen port.
	 *
	 * @return
	 */
	public int getListenPort() {
		return listenPort;
	}

	/**
	 * Retrieve the Domoticz baseUrl as set in constructor.
	 * 
	 * @return The baseUrl used for creation of all api calls.
	 */
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

	/**
	 * Return the timeout in milliseconds used for connections to the Domoticz API.
	 * 
	 * @return
	 */
	public int getConnectTimeoutMS() {
		return connectTimeoutMS;
	}

	/**
	 * Sets timeout for connection in milliseconds. Default value is specified in
	 * {@link #DEFAULT_CONNECT_TIMEOUT_MS}
	 *
	 * @param connectTimeoutMS Timeout in milliseconds, 0 for disable timeout.
	 */
	public void setConnectTimeoutMS(final int connectTimeoutMS) {
		this.connectTimeoutMS = connectTimeoutMS;
	}

	/**
	 * When this synchronous has been enabled (by calling this method), the REST
	 * call of the events received will be blocked until all reactions on event have
	 * been handled. If set, the HTTP code is based on the successful handling of the
	 * event. If set to false, event is handled asynchronously from the received
	 * event.
	 */
	public void setEventHandlingSynchronous() {
		this.synchronousEventHandling = true;
	}

	public boolean isSynchronousEventHandling() {
		return synchronousEventHandling;
	}

	public int getMaxInitWait_s() {
		return maxInitWait_s;
	}

	/**
	 * Sets the maximum wait time for Domoticz to be online during initialization.
	 * 
	 * @param maxInitWait_s
	 */
	public void setMaxInitWait_s(int maxInitWait_s) {
		this.maxInitWait_s = maxInitWait_s;
	}

	public int getInitInterval_s() {
		return initInterval_s;
	}

	/**
	 * Sets the interval of retries connecting to Domoticz during initialization.
	 * 
	 * @param initInterval_s The interval in seconds.
	 */
	public void setInitInterval_s(int initInterval_s) {
		this.initInterval_s = initInterval_s;
	}

}