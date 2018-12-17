package nl.gingerbeard.automation.service;

import java.lang.reflect.Field;

public class ServiceInstance {
	private final String name;
	private final Class<?> clazz;
	private final int producePriority;
	private Object instance;
	private final int componentPriority;

	public ServiceInstance(final int componentPriority, final Field field) {
		name = field.getName();
		clazz = field.getType();
		final nl.gingerbeard.automation.service.annotation.Provides produces = field.getAnnotation(nl.gingerbeard.automation.service.annotation.Provides.class);
		producePriority = produces.priority();
		this.componentPriority = componentPriority;
	}

	ServiceInstance(final String name, final Class<?> clazz, final int producePriority, final int componentPriority) {
		this.name = name;
		this.clazz = clazz;
		this.producePriority = producePriority;
		this.componentPriority = componentPriority;
	}

	public String getName() {
		return name;
	}

	public Object getService() {
		return instance;
	}

	public void setService(final Object instance) {
		this.instance = instance;
	}

	public int getProducePriority() {
		return producePriority;
	}

	public boolean isMatching(final Class<?> clazz) {
		return this.clazz == clazz;
	}

	public boolean isMatching(final Class<?> clazz, final int priority) {
		if (this.clazz != clazz) {
			return false;
		}
		return producePriority > priority;
	}

	public int getComponentPriority() {
		return componentPriority;
	}
}
