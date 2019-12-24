package nl.gingerbeard.automation.domoticz.clients;

import java.io.IOException;
import java.io.InputStreamReader;

import nl.gingerbeard.automation.domoticz.clients.json.GetSecStatusJSON;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.AlarmState;

public class AlarmStateClient extends GetClient {

	public AlarmStateClient(DomoticzConfiguration config, final ILogger log) throws IOException {
		super(config, log, "/json.htm?type=command&param=getsecstatus");
	}

	public AlarmState getAlarmState() throws IOException {
		InputStreamReader responseBodyReader = executeRequest();
		GetSecStatusJSON secStatus = gson.fromJson(responseBodyReader, GetSecStatusJSON.class);
		return secStatus.getAlarmState();
	}

}
