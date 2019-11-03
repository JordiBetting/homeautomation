package nl.gingerbeard.automation.onkyo;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;

import nl.gingerbeard.automation.devices.OnkyoReceiver;
import nl.gingerbeard.automation.devices.OnkyoReceiver.OnkyoSubdevice;
import nl.gingerbeard.automation.devices.OnkyoZoneMain;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public class OnkyoTransmitter implements IOnkyoTransmitter {

	private Map<String, OnkyoDriver> drivers = Maps.newHashMap();

	@Override
	public void transmit(NextState<?> newState) {
		if (isOnkyoNextState(newState)) {
			String host = getHost(newState);
			OnkyoDriver driver = getOrCreateDriver(host);
			OnOffState nextState = (OnOffState) newState.get();
			try {
				updateState(newState, driver, nextState);
			} catch (IOException | InterruptedException e) {
				// TODO handle error
			}
		}
	}

	private void updateState(NextState<?> newState, OnkyoDriver driver, OnOffState nextState)
			throws IOException, InterruptedException {
		if (isMain(newState)) {
			updateMainState(driver, nextState);
		} else { 
			updateZone2State(driver, nextState);
		}
	}

	private void updateZone2State(OnkyoDriver driver, OnOffState nextState) throws IOException, InterruptedException {
		if (nextState == OnOffState.ON) {
			driver.setZone2On();
		} else {
			driver.setZone2Off();
		}
	}

	private void updateMainState(OnkyoDriver driver, OnOffState nextState) throws IOException, InterruptedException {
		if (nextState == OnOffState.ON) {
			driver.setMainOn();
		} else {
			driver.setMainOff();
		}
	}

	private boolean isMain(NextState<?> newState) {
		return OnkyoZoneMain.class.isAssignableFrom(newState.getDevice().getClass());
	}

	private OnkyoDriver getOrCreateDriver(String host) {
		OnkyoDriver driver = drivers.get(host);
		if (driver == null) {
			driver = createOnkyoDriver(host);
			drivers.put(host, driver);
		}
		return driver;
	}

	// for testing override
	protected OnkyoDriver createOnkyoDriver(String host) {
		return new OnkyoDriver(host);
	}

	private String getHost(NextState<?> newState) {
		Optional<OnkyoReceiver> receiver = ((OnkyoSubdevice)newState.getDevice()).getParent();
		if (receiver.isPresent()) {
			return receiver.get().getHost();
		}
		throw new IllegalArgumentException("Cannot process Onkyo subdevice without have its parent set. Have you created the NextState with OnkyoReceiver.create... ?");
	}

	private boolean isOnkyoNextState(NextState<?> newState) {
		return OnkyoSubdevice.class.isAssignableFrom(newState.getDevice().getClass());
	}

}
