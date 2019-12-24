package nl.gingerbeard.automation.domoticz.clients;

import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.JsonSyntaxException;

import nl.gingerbeard.automation.domoticz.api.DomoticzException;
import nl.gingerbeard.automation.domoticz.clients.json.StatusJSON;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.DomoticzUrls;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.NextState;

public class UpdateTransmitterClient extends GetClient {

	private DomoticzUrls urlCreator;

	public UpdateTransmitterClient(DomoticzConfiguration config, final ILogger log) throws IOException {
		super(config, log, "");
		urlCreator = new DomoticzUrls(config);
	}

	public <T> void transmitDeviceUpdate(final NextState<T> nextState) throws DomoticzException {
		try {
			setUrl(urlCreator.getUrl(nextState));
			InputStreamReader responseBodyReader = executeRequest();
			validateOutput(responseBodyReader);
		} catch (IOException e) {
			throw new DomoticzException(e.getMessage(), e);
		}
	}

	private void validateOutput(InputStreamReader responseBodyReader) throws DomoticzException {
		try {
			StatusJSON status = gson.fromJson(responseBodyReader, StatusJSON.class);
			if (!"OK".equals(status.status)) {
				throw new DomoticzException("Failed setting value in domoticz: " + status.status);
			}
		} catch (JsonSyntaxException e) {
			throw new DomoticzException("Unable to parse JSON from domoticz", e);
		}

	}

}
