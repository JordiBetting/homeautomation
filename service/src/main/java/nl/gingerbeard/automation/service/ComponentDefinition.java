package nl.gingerbeard.automation.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;
import nl.gingerbeard.automation.service.exception.ComponentException;

public final class ComponentDefinition {
	private static enum State {
		NEW, //
		RESOLVED, //
		ACTIVE, //
		;
	}

	private State state = State.NEW;
	private final Class<?> componentClass;
	private final Object componentInstance;
	private final int componentPriority;

	ComponentDefinition(final int componentPriority, final Class<?> clazz) {
		componentClass = clazz;
		componentInstance = createInstance(clazz);
		this.componentPriority = componentPriority;
	}

	private Object createInstance(final Class<?> clazz) {
		try {
			return clazz.getConstructor().newInstance();
		} catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new ComponentException(
					"No default constructor found for class " + clazz.getName() + ". Ensure that constructor and class are public, static (in case of nested class) and contain no parameters.", e);
		}
	}

	Object getComponent() {
		return componentInstance;
	}

	void resolve(final ServiceRegistry serviceRegistry) {
		if (!isAllFieldsResolved(serviceRegistry)) {
			return;
		}
		registerProducedServices(serviceRegistry);
		state = State.RESOLVED;
	}

	private boolean isAllFieldsResolved(final ServiceRegistry serviceRegistry) {
		for (final Field field : componentClass.getDeclaredFields()) {
			if (isRequiresField(field) && !isRequiresResolved(field, serviceRegistry)) {
				return false;
			}
		}
		return true;
	}

	public List<String> getUnResolvedFieldNames(final ServiceRegistry serviceRegistry) {
		final List<String> unresolvedFields = Lists.newArrayList();
		for (final Field field : componentClass.getDeclaredFields()) {
			if (isRequiresField(field) && !isRequiresResolved(field, serviceRegistry)) {
				unresolvedFields.add(field.getName());
			}
		}
		return unresolvedFields;
	}

	private boolean isRequiresResolved(final Field field, final ServiceRegistry serviceRegistry) {
		final Class<?> serviceClass = field.getType();
		if (isCollection(serviceClass)) {
			return true;
		}
		if (isOptional(serviceClass)) {
			try {
				if (field.get(componentInstance) == null) {
					field.set(componentInstance, Optional.empty());
				}
			} catch (final IllegalAccessException e) {
				return false;
			}
			return true;
		}
		return serviceRegistry.hasService(this, serviceClass);
	}

	private boolean isCollection(final Class<?> serviceClass) {
		return serviceClass == Many.class;
	}

	private boolean isOptional(final Class<?> serviceClass) {
		return serviceClass == Optional.class;
	}

	private Class<?> getGenericTypeParameter(final Field field) {
		return (Class<?>) ((java.lang.reflect.ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
	}

	private void registerProducedServices(final ServiceRegistry registry) {
		for (final Field field : componentClass.getDeclaredFields()) {
			if (isProducedService(field)) {
				registry.registerService(this, new ServiceInstance(getComponentPriority(), field));
			}
		}
	}

	private boolean isProducedService(final Field field) {
		return field.isAnnotationPresent(Provides.class);
	}

	boolean isResolved() {
		return state != State.NEW;
	}

	boolean isActive() {
		return state == State.ACTIVE;
	}

	void activate(final ServiceRegistry registry) {
		if (isResolved()) {
			try {
				linkRequiredServices(registry);
				invokeAnnotatedMethods(Activate.class);
				activateProducedServices(registry);
				state = State.ACTIVE;
			} catch (final ServiceRegistry.InactiveServiceException e) {
			}
		}
	}

	private void linkRequiredServices(final ServiceRegistry registry) {
		for (final Field field : componentClass.getDeclaredFields()) {
			if (isRequiresField(field)) {
				linkRequiredField(field, registry);
			}
		}
	}

	private boolean isRequiresField(final Field field) {
		return field.isAnnotationPresent(Requires.class);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void linkRequiredField(final Field field, final ServiceRegistry registry) {
		final Class<?> serviceClass = field.getType();
		if (isCollection(serviceClass)) {
			final Collection<?> services = registry.getServices(getGenericTypeParameter(field), this);
			injectService(field, new ManyServices(services));

		} else if (isOptional(serviceClass)) {
			final Class<?> genericTypeParameter = getGenericTypeParameter(field);
			final Optional<Object> service = registry.getService(genericTypeParameter, this);
			injectOptionalService(field, service);
		} else {
			injectService(field, registry.getService(serviceClass, this));
		}
	}

	private void injectOptionalService(final Field field, final Optional<Object> service) {
		try {
			field.set(componentInstance, service);
		} catch (final IllegalAccessException e) {
			throw new ComponentException("Service " + field.getName() + " of " + this + " cannot be set", e);
		}
	}

	private void injectService(final Field field, final Object service) {
		injectService(field, Optional.ofNullable(service));
	}

	private void injectService(final Field field, final Optional<Object> service) {
		try {
			field.set(componentInstance, service.orElse(null));
		} catch (final IllegalAccessException e) {
			throw new ComponentException("Service " + field.getName() + " of " + this + " cannot be set", e);
		}
	}

	private void activateProducedServices(final ServiceRegistry registry) {
		for (final Field field : componentClass.getDeclaredFields()) {
			if (isProducedService(field)) {
				final Object service = getServiceFromField(field);
				registry.activateService(this, field.getName(), service);
			}
		}
	}

	public void deactivate() {
		invokeAnnotatedMethods(Deactivate.class);
		state = State.RESOLVED;
	}

	private void invokeAnnotatedMethods(final Class<? extends Annotation> annotation) {
		for (final Method method : componentClass.getDeclaredMethods()) {
			if (method.isAnnotationPresent(annotation)) {
				invokeMethod(method);
			}
		}
	}

	private void invokeMethod(final Method method) {
		try {
			method.invoke(componentInstance, new Object[0]);
		} catch (final IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new ComponentException("Method " + method.getName() + " of " + this + " failed", e);
		}
	}

	private Object getServiceFromField(final Field field) {
		try {
			final Object result = field.get(componentInstance);
			if (result == null) {
				throw new ComponentException("Service " + field.getName() + " of " + this + " is null");
			}
			return result;
		} catch (final IllegalArgumentException | IllegalAccessException e) {
			throw new ComponentException("Service " + field.getName() + " of " + this + " cannot be read", e);
		}
	}

	@Override
	public String toString() {
		return componentClass.getName();
	}

	public int getComponentPriority() {
		return componentPriority;
	}

	private static final class ManyServices<T> implements Many<T> {
		private final Iterable<T> elements;

		public ManyServices(final Iterable<T> elements) {
			this.elements = elements;
		}

		@Override
		public Iterator<T> iterator() {
			return this.elements.iterator();
		}
	}
}
