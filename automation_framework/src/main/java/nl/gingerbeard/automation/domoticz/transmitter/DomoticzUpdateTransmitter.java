package nl.gingerbeard.automation.domoticz.transmitter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;

public final class DomoticzUpdateTransmitter implements IDomoticzUpdateTransmitter {

	private final DomoticzConfiguration configuration;

	public DomoticzUpdateTransmitter(final DomoticzConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void transmitDeviceUpdate(final Device<?> device) throws IOException {
		final URL url = new URL(configuration.getBaseURL(), createDeviceSpecificUrlPart(device));
		System.out.println(url.toString());
		executeRequest(url);
	}

	private void executeRequest(final URL url) throws IOException, ProtocolException {
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		final int responseCode = con.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK) {
			// TODO: additionally parse returning JSON. Domoticz returns 200 with error in case of failures.
			throw new IOException(url.toString() + " " + con.getResponseMessage()); // TODO: add response body;
		}
	}

	private String createDeviceSpecificUrlPart(final Device<?> device) {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("json.htm?type=command&param=");
		stringBuilder.append(device.getDomoticzParam());
		stringBuilder.append("&idx=");
		stringBuilder.append(device.getIdx());
		stringBuilder.append("&switchcmd=");
		stringBuilder.append(device.getDomoticzSwitchCmd());
		final String deviceSpecificUrlPart = stringBuilder.toString();
		return deviceSpecificUrlPart;
	}

}
