package nl.gingerbeard.automation.domoticz;

import nl.gingerbeard.automation.domoticz.DomoticzEventReceiver.EventReceived;

public interface IDomoticzEventReceiver {

	void setEventListener(EventReceived listener);

	int getListeningPort();

}