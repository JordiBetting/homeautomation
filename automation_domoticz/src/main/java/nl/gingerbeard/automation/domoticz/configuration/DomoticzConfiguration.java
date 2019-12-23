package nl.gingerbeard.automation.domoticz.configuration;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Optional;

public class DomoticzConfiguration { // TODO: Refactor into general settings

	private int listenPort;
	private final URL baseURL;
	private int connectTimeoutMS = DEFAULT_CONNECT_TIMEOUT_MS;
	private boolean synchronousEventHandling = false;
	private Optional<DomoticzInitBehaviorConfig> initBehavior = Optional.of(new DomoticzInitBehaviorConfig());
	private Optional<String> credentials = Optional.empty();

	public static final int DEFAULT_CONNECT_TIMEOUT_MS = 3000;

	public DomoticzConfiguration(final int listenPort, final URL baseURL) {
		this.listenPort = listenPort;
		this.baseURL = baseURL;
	}

	public static class DomoticzInitBehaviorConfig {
		private final int maxInitWait_s; // TODO: Use Duration i.s.o. int_s
		private final int initInterval_s;

		/**
		 * Configuration for initialization behavior retry mechanism.
		 * 
		 * @param maxInitWait_s  Sets the maximum wait time for Domoticz to be online
		 *                       during initialization.
		 * @param initInterval_s ets the interval of retries connecting to Domoticz
		 *                       during initialization..
		 */
		public DomoticzInitBehaviorConfig(int maxInitWait_s, int initInterval_s) {
			this.maxInitWait_s = maxInitWait_s;
			this.initInterval_s = initInterval_s;
		}

		public DomoticzInitBehaviorConfig() {
			this(60 * 15, 5);
		}

		public int getMaxInitWait_s() {
			return maxInitWait_s;
		}

		public int getInitInterval_s() {
			return initInterval_s;
		}

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
	 * been handled. If set, the HTTP code is based on the successful handling of
	 * the event. If set to false, event is handled asynchronously from the received
	 * event.
	 */
	public void setEventHandlingSynchronous() {
		this.synchronousEventHandling = true;
	}

	public boolean isSynchronousEventHandling() {
		return synchronousEventHandling;
	}

	public void setInitConfiguration(DomoticzInitBehaviorConfig config) {
		this.initBehavior = Optional.of(config);
	}

	public void disableInit() {
		this.initBehavior = Optional.empty();
	}

	public boolean isInitEnabled() {
		return initBehavior.isPresent();
	}

	public Optional<DomoticzInitBehaviorConfig> getInitConfig() {
		return this.initBehavior;
	}

	public void setCredentials(String username, String password) {
		this.credentials = Optional.of(encode(username, password));
	}

	private String encode(String username, String password) {
		String credString = String.format("%s:%s", username, password);
		byte[] encoded = Base64.getEncoder().encode(credString.getBytes(Charset.defaultCharset()));
		return new String(encoded, Charset.defaultCharset());
	}

	public Optional<String> getCredentialsEncoded() {
		return credentials;
	}

}