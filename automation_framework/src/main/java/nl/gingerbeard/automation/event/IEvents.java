package nl.gingerbeard.automation.event;

public interface IEvents {

	void subscribe(Object subscriber);

	EventResult trigger(Object event);

}