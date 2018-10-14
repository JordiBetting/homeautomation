package nl.jordibetting.automation.event;

public interface Events {

	void subscribe(Object subscriber);

	EventResult trigger(Object event);

}