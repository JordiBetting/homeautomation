package nl.gingerbeard.automation.domoticz.transmitter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.base.Charsets;

import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.DomoticzUrls;
import nl.gingerbeard.automation.state.NextState;

public final class DomoticzUpdateTransmitter implements IDomoticzUpdateTransmitter {

	private final DomoticzUrls urlCreator;

	public DomoticzUpdateTransmitter(final DomoticzConfiguration configuration) {
		urlCreator = new DomoticzUrls(configuration);
	}

	@Override
	public <T> void transmitDeviceUpdate(final NextState<T> nextState) throws IOException {
		final URL url = urlCreator.getUrl(nextState);
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
		con.connect();
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
		final InputStreamReader in = new InputStreamReader(con.getInputStream(), Charsets.UTF_8);
		return (JSONObject) parser.parse(in);
	}

}
