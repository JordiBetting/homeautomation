package nl.gingerbeard.automation.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Provides {
	public static final int DEFAULT_PRIO = 10;

	int priority() default DEFAULT_PRIO;
}
