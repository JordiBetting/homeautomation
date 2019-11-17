package nl.gingerbeard.automation.domoticz.clients;

import java.io.IOException;
import java.io.InputStreamReader;

import nl.gingerbeard.automation.domoticz.clients.json.GetSecStatus;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.state.AlarmState;

public class AlarmStateClient extends GetClient {

	public AlarmStateClient(DomoticzConfiguration config) throws IOException {
		super(config, "/json.htm?type=command&param=getsecstatus");
	}

	public AlarmState getAlarmState() throws IOException {
		InputStreamReader responseBodyReader = executeRequest();
		GetSecStatus secStatus = gson.fromJson(responseBodyReader, GetSecStatus.class);
		return secStatus.getAlarmState();
	}

}
