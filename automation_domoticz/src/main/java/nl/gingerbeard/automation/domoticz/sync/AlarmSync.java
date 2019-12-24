package nl.gingerbeard.automation.domoticz.sync;

import java.io.IOException;

import nl.gingerbeard.automation.domoticz.clients.AlarmStateClient;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.IState;

public class AlarmSync {
	
	private IState state;
	private AlarmStateClient client;

	public AlarmSync(IState state, DomoticzConfiguration config, ILogger log) throws IOException {
		this(state, new AlarmStateClient(config, log));
	}
	
	public AlarmSync(IState state, AlarmStateClient client) {
		this.state = state;
		this.client = client;
	}
	
	public void syncAlarm() throws IOException {
		state.setAlarmState(client.getAlarmState());
	}
}
