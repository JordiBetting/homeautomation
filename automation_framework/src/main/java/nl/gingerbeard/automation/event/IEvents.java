package nl.gingerbeard.automation.event;

import java.util.List;

public interface IEvents {

	void subscribe(Object subscriber);

	EventResult trigger(Object event);

	void disable(String subscriberSimpleClassName);

	void enable(String subscriberSimpleClassName);

	List<String> getSubscribers();

	boolean isEnabled(String room);

}