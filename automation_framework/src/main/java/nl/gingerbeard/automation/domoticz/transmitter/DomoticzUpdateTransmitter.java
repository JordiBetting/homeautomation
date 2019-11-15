package nl.gingerbeard.automation.domoticz.transmitter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import nl.gingerbeard.automation.domoticz.api.DomoticzException;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.DomoticzUrls;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.NextState;

public class DomoticzUpdateTransmitter {

	private final DomoticzUrls urlCreator;
	private final ILogger log;
	private final int timeoutMS;

	public DomoticzUpdateTransmitter(final DomoticzConfiguration configuration, final ILogger log) {
		this.log = log;
		urlCreator = new DomoticzUrls(configuration);
		timeoutMS = configuration.getConnectTimeoutMS();
	}

	public <T> void transmitDeviceUpdate(final NextState<T> nextState) throws DomoticzException {
		try {
			final URL url = urlCreator.getUrl(nextState);
			executeRequest(url);
		} catch(IOException e) {
			throw new DomoticzException(e.getMessage(), e); 
		}
	}

	private void executeRequest(final URL url) throws IOException, ProtocolException {
		HttpURLConnection con = null;
		try {
			// log.debug("Creating request: " + url.toString());
			con = createConnection(url);
			final int responseCode = con.getResponseCode();
			log.debug("Domoticz call " + responseCode + " on " + url.toString());
			validateResponseCode(url, con, responseCode);
			validateOutput(con);
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}

	// TODO: Split off into clients package
	
	private void validateResponseCode(final URL url, final HttpURLConnection con, final int responseCode) throws IOException {
		if (responseCode != HttpURLConnection.HTTP_OK) {
			String body = readErrorBody(con);
			if (body.length() > 0) {
				body = System.lineSeparator() + body;
			}
			throw new IOException(url.toString() + " " + con.getResponseMessage() + body);
		}
	}

	private String readErrorBody(final HttpURLConnection con) throws IOException {
		if (con.getErrorStream() != null) {
			try (InputStreamReader reader = new InputStreamReader(con.getErrorStream(), Charset.defaultCharset())) {
				return CharStreams.toString(reader);
			}
		}
		return "";
	}

	private HttpURLConnection createConnection(final URL url) throws IOException, ProtocolException {
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setConnectTimeout(timeoutMS);
		con.setReadTimeout(timeoutMS);
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
