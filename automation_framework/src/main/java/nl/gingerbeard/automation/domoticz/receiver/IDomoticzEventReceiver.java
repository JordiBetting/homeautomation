package nl.gingerbeard.automation.domoticz.receiver;

import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiverServer.EventReceived;

public interface IDomoticzEventReceiver {

	void setEventListener(EventReceived listener);

	int getListeningPort();

	void stop();

}