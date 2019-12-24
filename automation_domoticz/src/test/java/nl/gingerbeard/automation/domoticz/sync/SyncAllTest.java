package nl.gingerbeard.automation.domoticz.sync;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;

public class SyncAllTest {

	@Mock
	IDeviceRegistry deviceRegistry;

	@Mock
	AlarmSync alarmSync;

	@Mock
	DeviceSync deviceSync;

	@Mock
	TimeSync timeSync;

	private SyncAll sync;
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		
		sync = new SyncAll(deviceRegistry, alarmSync, deviceSync, timeSync);
	}
	
	@Test
	public void syncAll_callsTime() throws IOException {
		sync.syncAll();
		
		verify(timeSync, times(1)).syncTime();
	}
	
	@Test
	public void syncAll_callsAlarm() throws IOException {
		sync.syncAll();
		
		verify(alarmSync, times(1)).syncAlarm();
	}
	
	@Test
	public void syncAll_singleDevice_callsDevice() throws IOException {
		when(deviceRegistry.getAllIdx()).thenReturn(Sets.newHashSet(42));
		
		sync.syncAll();
		
		verify(deviceSync, times(1)).syncDevice(42);
	}
	
	@Test
	public void syncAll_multipleDevices_callsDevice() throws IOException {
		when(deviceRegistry.getAllIdx()).thenReturn(Sets.newHashSet(42, 666));
		
		sync.syncAll();
		
		verify(deviceSync, times(1)).syncDevice(42);
		verify(deviceSync, times(1)).syncDevice(666);
		verifyNoMoreInteractions(deviceSync);
	}
	
}
