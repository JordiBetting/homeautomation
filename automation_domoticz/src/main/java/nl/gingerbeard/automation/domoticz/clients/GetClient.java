package nl.gingerbeard.automation.domoticz.clients;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;

import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.ILogger;

public abstract class GetClient {

	private URL baseUrl;
	private URL url;
	protected static final Gson gson = new Gson();
	private final Optional<String> authorizationHeader;
	private int timeoutMS;
	private ILogger log;

	public GetClient(DomoticzConfiguration config, final ILogger log, String path) throws IOException {
		this.log = log;
		this.baseUrl = config.getBaseURL();
		this.url = createUrl(path);
		this.authorizationHeader = getAuthorizationHeader(config);
		timeoutMS = config.getConnectTimeoutMS();
	}

	private Optional<String> getAuthorizationHeader(DomoticzConfiguration config) {
		String auth = null;
		if (config.getCredentialsEncoded().isPresent()) {
			auth = "Basic " + config.getCredentialsEncoded().get();
		}
		return Optional.ofNullable(auth);
	}
	
	protected void setUrl(URL url) {
		this.url = url;
	}
	
	protected void setUrl(String path) throws IOException {
		this.url = createUrl(path);
	}

	private URL createUrl(String path) throws IOException {
		return new URL(baseUrl.toString() + path);
	}
	

	protected final InputStreamReader executeRequest() throws IOException, ProtocolException {
		final HttpURLConnection con = createConnection(url);
		validateResponseCode(con);
		return new InputStreamReader(con.getInputStream(), Charsets.UTF_8);
	}

	private void validateResponseCode(final HttpURLConnection con) throws IOException {
		int responseCode = con.getResponseCode();
		log.debug("Domoticz call " + responseCode + " on " + con.getURL().toString());
		
		if (responseCode != HttpURLConnection.HTTP_OK) {
			String body = readErrorBody(con);
			if (body.length() > 0) {
				body = System.lineSeparator() + body;
			}
			throw new IOException("responsecode expected 200, but was: " + responseCode + System.lineSeparator() + url.toString() + " " + con.getResponseMessage() + body);
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
		con.setUseCaches(false);		
		con.setConnectTimeout(timeoutMS);
		con.setReadTimeout(timeoutMS);
		con.setRequestMethod("GET");
		authorizationHeader.ifPresent((header) -> con.addRequestProperty("Authorization", header));
		con.connect();
		return con;
	}

}
