package nl.gingerbeard.automation.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

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

	public static final class ReflectionException extends RuntimeException {

		private static final long serialVersionUID = 2955666324295948758L;

		public ReflectionException(final String message, final Throwable cause) {
			super(message, cause);
		}

	}

	public static <T> Set<T> createInstancesBySubtype(final String packageName, final Class<T> type) {
		final Reflections refl = new Reflections(packageName);
		final Set<Class<? extends T>> subtypes = refl.getSubTypesOf(type);
		final Set<T> instances = new HashSet<>();
		for (final Class<? extends T> subtype : subtypes) {
			try {
				instances.add(subtype.getConstructor().newInstance());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new ReflectionException("Cannot instantiate " + type.getName(), e);
			}
		}
		return instances;
	}
}
