package nl.gingerbeard.automation.configuration;

public final class ConfigurationServerSettings {

	private int listenPort;

	public ConfigurationServerSettings(final int listenPort) {
		this.listenPort = listenPort;
	}

	public int getListenPort() {
		return listenPort;
	}

	void setListenPort(final int listenPort) {
		this.listenPort = listenPort;
	}

}
