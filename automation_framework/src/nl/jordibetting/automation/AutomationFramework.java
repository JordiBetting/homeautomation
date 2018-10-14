package nl.jordibetting.automation;

import nl.jordibetting.automation.controlloop.Controlloop;
import nl.jordibetting.automation.domoticz.Domoticz;
import nl.jordibetting.automation.event.Events;
import nl.jordibetting.automation.event.SynchronousEvents;
import nl.jordibetting.automation.state.State;

public class AutomationFramework {

	private final State state;
	private final Domoticz domoticz;
	private final Events events;
	private final Controlloop controlLoop;
	
	public AutomationFramework(State state, Domoticz domoticz, Events events, Controlloop controlloop) {
		this.state = state;
		this.domoticz = domoticz;
		this.events = events;
		this.controlLoop = controlloop;
		
	}

	public static AutomationFramework create() {
		State state = new State();
		Domoticz domoticz = new Domoticz();
		Events events = new SynchronousEvents();
		Controlloop controlloop = new Controlloop(events);
		
		return new AutomationFramework(state, domoticz, events, controlloop);
	}

	public void addRoom(Room room) {
//		eventHandler.register(room);
	}
	
}
