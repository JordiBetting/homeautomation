package nl.gingerbeard.automation.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ReflectionUtil {
	private ReflectionUtil() {
		// avoid instantiation
	}

	public static List<Method> getMethodsAnnotatedWith(final Class<?> inputType, final Class<? extends Annotation> annotation, final int parameterCount) {
		final List<Method> methods = new ArrayList<>();
		Class<?> type = inputType;
		while (type != Object.class) { // walk-up to superclasses until reaching Object
			final List<Method> allMethods = Arrays.asList(type.getDeclaredMethods());
			for (final Method method : allMethods) {
				if (methodSignatureMatches(annotation, method, parameterCount)) {
					methods.add(method);
				}
			}
			// move to the upper class in the hierarchy in search for more methods
			type = type.getSuperclass();
		}
		return methods;
	}

	private static boolean methodSignatureMatches(final Class<? extends Annotation> annotation, final Method method, final int parameterCount) {
		if (method.isAnnotationPresent(annotation)) {
			if (method.getParameterCount() != parameterCount) {
				throw new IllegalArgumentException("Expected subscribe with exectly " + parameterCount + " parameter, method: " + method.getName());
			}
			return true;
		}
		return false;
	}
}
