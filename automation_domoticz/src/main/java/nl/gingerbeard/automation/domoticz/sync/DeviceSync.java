package nl.gingerbeard.automation.domoticz.sync;

import java.io.IOException;
import java.util.Optional;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.domoticz.clients.GetDeviceClient;
import nl.gingerbeard.automation.domoticz.clients.json.DeviceJSON;
import nl.gingerbeard.automation.domoticz.clients.json.DeviceJSON.DeviceResultJSON;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.ILogger;

public class DeviceSync {

	private IDeviceRegistry deviceRegistry;
	private ILogger logger;
	private GetDeviceClient client;

	public DeviceSync(DomoticzConfiguration config, IDeviceRegistry deviceRegistry, ILogger logger) throws IOException {
		this(new GetDeviceClient(config, logger), deviceRegistry, logger);
	}

	public DeviceSync(GetDeviceClient client, IDeviceRegistry deviceRegistry, ILogger logger) {
		this.client = client;
		this.deviceRegistry = deviceRegistry;
		this.logger = logger;
	}

	public void syncDevice(int idx) throws IOException {
		Optional<DeviceResultJSON> details = getDeviceDetails(idx);
		if (details.isPresent()) {
			if (details.get().status != null && !details.get().status.equals("")) {
				deviceRegistry.updateDevice(idx, details.get().status);
			} else {
				logger.warning(
						String.format("No status reported for device with idx %d. Could not set initial state.", idx));
			}
		}
	}

	private Optional<DeviceResultJSON> getDeviceDetails(int idx) throws IOException {
		DeviceJSON deviceState = client.getDeviceDetails(idx);
		Optional<DeviceResultJSON> details = getDeviceDetails(idx, deviceState);
		return details;
	}

	private Optional<DeviceResultJSON> getDeviceDetails(int idx, DeviceJSON deviceState) {
		Optional<DeviceResultJSON> details = Optional.empty();
		DeviceResultJSON[] result = deviceState.result;
		if (result == null || result.length == 0 || result[0] == null) {
			logger.warning(
					String.format("No details received for device with idx %d. Could not set initial state. DomoticzReplyStatus: %s", idx, deviceState.status));
		} else {
			if (result.length > 1) {
				logger.warning(
						String.format("Received multiple details for device with idx %d, taking first entry.", idx));
			}
			details = Optional.ofNullable(result[0]);
		}
		return details;
	}
}
