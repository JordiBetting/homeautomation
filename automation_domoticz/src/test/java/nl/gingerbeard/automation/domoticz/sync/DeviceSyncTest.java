package nl.gingerbeard.automation.domoticz.sync;

import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.domoticz.clients.GetDeviceClient;
import nl.gingerbeard.automation.domoticz.clients.json.DeviceJSON;
import nl.gingerbeard.automation.domoticz.clients.json.DeviceJSON.DeviceResultJSON;
import nl.gingerbeard.automation.logging.LogLevel;
import nl.gingerbeard.automation.logging.TestLogger;

public class DeviceSyncTest {

	private TestLogger log;
	
	@Mock
	GetDeviceClient client;
	
	@Mock
	IDeviceRegistry registry;
	
	//Sut
	DeviceSync sync;
	
	@BeforeEach
	public void init() {
		log = new TestLogger();
		MockitoAnnotations.initMocks(this);
		sync = new DeviceSync(client, registry, log);
	}
	
	@Test
	public void syncs_goodWeather() throws IOException {
		when(client.getDeviceDetails(1)).thenReturn(createDevice());
		
		sync.syncDevice(1);
		
		verify(registry, times(1)).updateDevice(1, "On");
	}
	
	@Test
	public void multipleResults_firstUsed() throws IOException {
		when(client.getDeviceDetails(1)).thenReturn(createDeviceWithMultipleResults());
		
		sync.syncDevice(1);
		
		verify(registry, times(1)).updateDevice(1, "On");
	}
	
	@Test
	public void multipleResults_warningLogged() throws IOException {
		when(client.getDeviceDetails(1)).thenReturn(createDeviceWithMultipleResults());
		
		sync.syncDevice(1);
		
		log.assertContains(LogLevel.WARNING, "Received multiple details for device with idx 1, taking first entry.");
	}
	
	@Test
	public void noResults_registryNotCalled() throws IOException {
		when(client.getDeviceDetails(1)).thenReturn(createDeviceWithNoResults());
		
		sync.syncDevice(1);
		
		verifyNoMoreInteractions(registry);
	}

	@Test
	public void noResults_warningLogged() throws IOException {
		when(client.getDeviceDetails(1)).thenReturn(createDeviceWithNoResults());
		
		sync.syncDevice(1);
		
		log.assertContains(LogLevel.WARNING, "No details received for device with idx 1. Could not set initial state. DomoticzReplyStatus: OK");
	}
	
	@Test
	public void requestFailed() throws IOException {
		when(client.getDeviceDetails(1)).thenReturn(createFailedResult());
		
		sync.syncDevice(1);
		
		log.assertContains(LogLevel.WARNING, "No details received for device with idx 1. Could not set initial state. DomoticzReplyStatus: Failed");
	}
	
	private DeviceJSON createFailedResult() {
		DeviceJSON json = new DeviceJSON();
		json.status = "Failed";
		return json;
	}

	private DeviceJSON createDevice() {
		DeviceResultJSON result = new DeviceResultJSON();
		result.status = "On";
		
		DeviceJSON json = new DeviceJSON();
		json.status = "OK";
		json.result = new DeviceResultJSON[] { result };
		return json;
	}
	
	private DeviceJSON createDeviceWithMultipleResults() {
		DeviceResultJSON result1 = new DeviceResultJSON();
		result1.status = "On";
		
		DeviceResultJSON result2 = new DeviceResultJSON();
		result2.status = "Off";
		
		DeviceJSON json = new DeviceJSON();
		json.status = "OK";
		json.result = new DeviceResultJSON[] { result1, result2 };
		return json;
	}
	
	private DeviceJSON createDeviceWithNoResults() {
		DeviceJSON json = new DeviceJSON();
		json.status = "OK"; 
		json.result = new DeviceResultJSON[] { };
		return json;
	}
}
