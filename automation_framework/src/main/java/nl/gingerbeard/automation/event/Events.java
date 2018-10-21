package nl.gingerbeard.automation.event;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.state.State;

public interface Events {

	void subscribe(Object subscriber);

	EventResult trigger(Device event);

	EventResult trigger(State event);

}