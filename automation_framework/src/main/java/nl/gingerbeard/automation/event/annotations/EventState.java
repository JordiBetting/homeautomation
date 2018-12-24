package nl.gingerbeard.automation.event.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.HomeAway;
import nl.gingerbeard.automation.state.TimeOfDay;

@Retention(RUNTIME)
@Target(TYPE)
public @interface EventState {
	TimeOfDay timeOfDay() default TimeOfDay.ALLDAY;

	HomeAway homeAway() default HomeAway.ALWAYS;

	AlarmState alarmState() default AlarmState.ALWAYS;
}
