package nl.gingerbeard.automation.domoticz.clients;

import java.io.IOException;
import java.io.InputStreamReader;

import nl.gingerbeard.automation.domoticz.clients.json.DeviceJSON;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.ILogger;

public class GetDeviceClient extends GetClient {

	public GetDeviceClient(DomoticzConfiguration config, ILogger log) throws IOException {
		super(config, log, "");
	}
	
	public DeviceJSON getDeviceDetails(int idx) throws IOException {
		super.setUrl("/json.htm?type=devices&rid="+idx);
		InputStreamReader result = executeRequest();
		return gson.fromJson(result, DeviceJSON.class);
	}
	

}
