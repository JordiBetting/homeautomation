package nl.gingerbeard.automation.onkyo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.OnkyoReceiver;
import nl.gingerbeard.automation.devices.OnkyoZone2;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public class OnkyoTransmitterTest {

	private OnkyoReceiver receiver;
	private OnkyoDriver driver;
	private OnkyoTransmitter transmitter;

	@BeforeEach
	public void createMocks() {
		receiver = new OnkyoReceiver("1.2.3.4");
		transmitter = spy(new OnkyoTransmitter());
		driver = mock(OnkyoDriver.class);
		when(transmitter.createOnkyoDriver(anyString())).thenReturn(driver);
	}
	
	@Test
	public void transmit_MainOn() throws IOException, InterruptedException {
		transmitter.transmit(receiver.createNextStateMain(OnOffState.ON));
		
		verify(driver, times(1)).setMainOn();
		verifyNoMoreInteractions(driver);
	}
	
	@Test
	public void transmit_MainOff() throws IOException, InterruptedException {
		transmitter.transmit(receiver.createNextStateMain(OnOffState.OFF));
		
		verify(driver, times(1)).setMainOff();
		verifyNoMoreInteractions(driver);
	}
	
	@Test
	public void transmit_Zone2Off() throws IOException, InterruptedException {
		transmitter.transmit(receiver.createNextStateZone2(OnOffState.OFF));
		
		verify(driver, times(1)).setZone2Off();
		verifyNoMoreInteractions(driver);
	}
	
	@Test
	public void transmit_Zone2On() throws IOException, InterruptedException {
		transmitter.transmit(receiver.createNextStateZone2(OnOffState.ON));
		
		verify(driver, times(1)).setZone2On();
		verifyNoMoreInteractions(driver);
	}
	
	
	@Test
	public void transmit_nonOnkyoNextState_noException() {
		NextState<?> newState = new NextState<>(new Switch(1), OnOffState.OFF);
		transmitter.transmit(newState);
	}
	
	@Test
	public void transmit_subdeviceWithoutParent_throwsException() {
		NextState<?> newState = new NextState<>(new OnkyoZone2(), OnOffState.OFF);
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transmitter.transmit(newState));
		assertEquals("Onkyo subdevice should have parent set to Receiver", exception.getMessage());
	}
	
	@Test
	public void driversCached() {
		transmitter.transmit(receiver.createNextStateZone2(OnOffState.ON));
		transmitter.transmit(receiver.createNextStateZone2(OnOffState.ON));
		
		verify(transmitter, times(1)).createOnkyoDriver(anyString());
		
	}
}
