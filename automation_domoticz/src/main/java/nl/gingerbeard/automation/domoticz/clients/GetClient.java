package nl.gingerbeard.automation.domoticz.clients;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Optional;

import com.google.common.base.Charsets;
import com.google.gson.Gson;

import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;

public abstract class GetClient {

	private URL url;
	protected static final Gson gson = new Gson();
	private final Optional<String> authorizationHeader;

	public GetClient(DomoticzConfiguration config, String url) throws IOException {
		this.url = createUrl(config, url);
		this.authorizationHeader = getAuthorizationHeader(config);

	}

	private Optional<String> getAuthorizationHeader(DomoticzConfiguration config) {
		String auth = null;
		if (config.getCredentialsEncoded().isPresent()) {
			auth = "Basic " + config.getCredentialsEncoded().get();
		}
		return Optional.ofNullable(auth);
	}

	private URL createUrl(DomoticzConfiguration config, String path) throws IOException {
		return new URL(config.getBaseURL().toString() + path);
	}

	protected final InputStreamReader executeRequest() throws IOException, ProtocolException {
		final HttpURLConnection con = createConnection(url);
		validateResponseCode(con);
		return new InputStreamReader(con.getInputStream(), Charsets.UTF_8);
	}

	private void validateResponseCode(final HttpURLConnection con) throws IOException {
		final int httpCode = con.getResponseCode();
		if (httpCode != 200) {
			throw new IOException("responsecode expected 200, but was: " + httpCode);
		}
	}

	private HttpURLConnection createConnection(final URL url) throws IOException, ProtocolException {
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setUseCaches(false);
		con.setRequestMethod("GET");
		authorizationHeader.ifPresent((header) -> con.addRequestProperty("Authorization", header));
		con.connect();
		return con;
	}

}
