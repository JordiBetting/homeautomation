package nl.gingerbeard.automation.domoticz;

public final class DomoticzEventReceiverConfiguration {

	private final int port;

	public DomoticzEventReceiverConfiguration(final int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

}
