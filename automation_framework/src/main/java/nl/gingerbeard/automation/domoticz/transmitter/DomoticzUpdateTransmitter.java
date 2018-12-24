package nl.gingerbeard.automation.domoticz.transmitter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
		executeRequest(url);
	}

	private void executeRequest(final URL url) throws IOException, ProtocolException {
		HttpURLConnection con = null;
		try {
			con = createConnection(url);
			final int responseCode = con.getResponseCode();
			validateResponseCode(url, con, responseCode);
			validateOutput(con);
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}

	private void validateResponseCode(final URL url, final HttpURLConnection con, final int responseCode) throws IOException {
		if (responseCode != HttpURLConnection.HTTP_OK) {
			throw new IOException(url.toString() + " " + con.getResponseMessage()); // TODO: add response body;
		}
	}

	private HttpURLConnection createConnection(final URL url) throws IOException, ProtocolException {
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		return con;
	}

	private void validateOutput(final HttpURLConnection con) throws IOException {
		try {
			validateOutputThrows(con);
		} catch (final ParseException e) {
			throw new IOException("Unable to parse JSON from domoticz", e);
		}
	}

	private void validateOutputThrows(final HttpURLConnection con) throws IOException, ParseException {
		final JSONObject content = getJsonContent(con);
		validateContent(content);
	}

	private void validateContent(final JSONObject content) throws IOException {
		if (!"OK".equals(content.get("status"))) {
			throw new IOException("Failed setting value in domotics: " + content);
		}
	}

	private JSONObject getJsonContent(final HttpURLConnection con) throws IOException, ParseException {
		final JSONParser parser = new JSONParser();
		final InputStreamReader in = new InputStreamReader(con.getInputStream());
		return (JSONObject) parser.parse(in);
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
