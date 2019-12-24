package nl.gingerbeard.automation.domoticz.sync;

import java.io.IOException;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.IState;

public class SyncAll {

	private AlarmSync alarmSync;
	private DeviceSync deviceSync;
	private TimeSync timeSync;
	private IDeviceRegistry deviceRegistry;

	public SyncAll(DomoticzConfiguration config, IState state, IDeviceRegistry deviceRegistry, ILogger log)
			throws IOException {
		this(deviceRegistry, new AlarmSync(state, config, log), new DeviceSync(config, deviceRegistry, log),
				new TimeSync(state, config, log));
	}

	public SyncAll(IDeviceRegistry deviceRegistry, AlarmSync alarmSync, DeviceSync deviceSync, TimeSync timeSync) {
		this.deviceRegistry = deviceRegistry;
		this.alarmSync = alarmSync;
		this.deviceSync = deviceSync;
		this.timeSync = timeSync;
	}

	public void syncAll() throws IOException {
		timeSync.syncTime();
		alarmSync.syncAlarm();
		syncDevices();
//		syncScenes();
	}

	private void syncDevices() throws IOException {
		for (int idx : deviceRegistry.getAllIdx()) {
			deviceSync.syncDevice(idx);
		}
	}

}
