package nl.gingerbeard.automation.domoticz.api;

import nl.gingerbeard.automation.deviceregistry.DeviceRegistry;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.State;

/**
 * Interface for communication with Domoticz.
 * 
 * The goal of this interface is to provide all domoticz communication.
 * It receives events from Domoticz (alarm, time, devices). It is capable of sending commands to Domoticz (e.g. turn light on).
 *
 * Clients can subscribe to events using this interface. Event are all fired from the same Thread (which is not the main thread), thus are always sequential.
 * When events are received, the administration ({@link State} or {@link DeviceRegistry}) is updated just before the event is sent. 
 *
 */
public interface DomoticzApi {

	/**
	 * Register a listener for changes in alarm state.
	 * 
	 * @param alarmListener The listener to set. Provide null for unregister.
	 */
	void setAlarmListener(IDomoticzAlarmChanged alarmListener);

	/**
	 * Register a listener for changes in device state.
	 * @param deviceListener The listener to set. Provide null for unregister.
	 */
	void setDeviceListener(IDomoticzDeviceStatusChanged deviceListener);

	/**
	 * Register a listener for time changes.
	 * @param timeListener The listener to set. Provide null for unregister.
	 */
	void setTimeListener(IDomoticzTimeOfDayChanged timeListener);

	/**
	 * Initialize the system, receiving current state for alarm, time and all devices. Result in stored in {@link State} and {@link DeviceRegistry}.
	 * The initialization is retried in case it fails. See {@link DomoticzConfiguration} for configuration options.
	 * Calling this method will not result in triggering of any listeners.
	 * Skipped in case no init behavior is configured in {@link DomoticzConfiguration}
	 * @throws DomoticzException Thrown on communication failure.
	 * @throws InterruptedException Thrown when thread is interrupted during this (potentially long running) method.
	 */
	void syncFullState() throws InterruptedException, DomoticzException;
	
	/**
	 * Sets a device to a new state in Domoticz.
	 * @param newState The NextState describing the device and desired state.
	 * @throws DomoticzException Thrown on communication failure.
	 */
	<T> void transmitDeviceUpdate(NextState<T> newState) throws DomoticzException;

	/**
	 * Stops receiver and all internal threads. Other methods will no longer be functional.
	 * @throws InterruptedException Thrown when one of the threads is interrupted during stopping.
	 */
	void stop() throws InterruptedException;
}
