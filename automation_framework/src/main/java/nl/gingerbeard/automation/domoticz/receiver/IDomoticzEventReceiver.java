package nl.gingerbeard.automation.domoticz.receiver;

import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiver.EventReceived;

public interface IDomoticzEventReceiver {

	void setEventListener(EventReceived listener);

	int getListeningPort();

}