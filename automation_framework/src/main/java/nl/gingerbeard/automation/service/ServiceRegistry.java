package nl.gingerbeard.automation.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class ServiceRegistry {
	private final Map<ComponentDefinition, List<ServiceInstance>> componentServices;

	public ServiceRegistry() {
		componentServices = com.google.common.collect.Maps.newHashMap();
	}

	public void registerService(final ComponentDefinition component, final ServiceInstance service) {
		if (component == null) {
			throw new NullPointerException("key for component registration can not be null");
		}
		final List<ServiceInstance> services = getServices(component);
		services.add(service);
	}

	private List<ServiceInstance> getServices(final ComponentDefinition component) {
		if (!componentServices.containsKey(component)) {
			componentServices.put(component, com.google.common.collect.Lists.newArrayList());
		}
		final List<ServiceInstance> services = componentServices.get(component);
		return services;
	}

	public boolean hasService(final ComponentDefinition currentComponent, final Class<?> serviceClass) {
		final Optional<ServiceInstance> instance = findBestService(currentComponent, serviceClass);
		return instance.isPresent();
	}

	public Optional<Object> getService(final Class<?> serviceClass) {
		return getService(serviceClass, null);
	}

	public Optional<Object> getService(final Class<?> serviceClass, final ComponentDefinition currentComponent) {
		Optional<Object> service = Optional.empty();
		final Optional<ServiceInstance> instance = findBestService(currentComponent, serviceClass);
		if (instance.isPresent()) {
			service = Optional.ofNullable(instance.get().getService());
			if (!service.isPresent()) {
				throw new InactiveServiceException(instance.get());
			}
		}
		return service;
	}

	private Optional<ServiceInstance> findBestService(final ComponentDefinition currentComponent, final Class<?> serviceClass) {
		Optional<ServiceInstance> selected = Optional.empty();
		int producePriority = Integer.MIN_VALUE;
		int componentPriority = Integer.MIN_VALUE;
		for (final ServiceInstance instance : filterServices(currentComponent, serviceClass)) {
			if (instance.getComponentPriority() > componentPriority) {
				componentPriority = instance.getComponentPriority();
				producePriority = instance.getProducePriority();
				selected = Optional.of(instance);
			} else if (instance.getComponentPriority() == componentPriority && instance.getProducePriority() > producePriority) {
				selected = Optional.of(instance);
				producePriority = instance.getProducePriority();
			}
		}
		return selected;
	}

	public <T> Collection<T> getServices(final Class<T> serviceClass) {
		return getServices(serviceClass, null);
	}

	public <T> Collection<T> getServices(final Class<T> serviceClass, final ComponentDefinition skipComponent) {
		final List<T> services = new ArrayList<>();
		for (final ServiceInstance instance : filterServices(skipComponent, serviceClass)) {
			services.add(getService(instance));
		}
		return services;
	}

	private <T> T getService(final ServiceInstance instance) {
		final T service = (T) instance.getService();
		if (service == null) {
			throw new InactiveServiceException(instance);
		}
		return service;
	}

	private Collection<ServiceInstance> filterServices(final ComponentDefinition currentComponent, final Class<?> serviceClass) {
		final List<ServiceInstance> instances = new ArrayList<>();
		for (final Entry<ComponentDefinition, List<ServiceInstance>> entry : componentServices.entrySet()) {
			final ComponentDefinition key = entry.getKey();
			if (key != currentComponent && (currentComponent == null || key.getComponentPriority() <= currentComponent.getComponentPriority())) {
				instances.addAll(filterMatchingInstances(entry.getValue(), serviceClass));
			}
		}
		return instances;
	}

	private List<ServiceInstance> filterMatchingInstances(final List<ServiceInstance> candidates, final Class<?> serviceClass) {
		final List<ServiceInstance> instances = com.google.common.collect.Lists.newArrayList();
		for (final ServiceInstance instance : candidates) {
			if (instance.isMatching(serviceClass)) {
				instances.add(instance);
			}
		}
		return instances;
	}

	public void activateService(final ComponentDefinition component, final String fieldName, final Object service) {
		final List<ServiceInstance> services = componentServices.get(component);
		final ServiceInstance instance = findServiceInstance(services, fieldName);
		instance.setService(service);
	}

	private ServiceInstance findServiceInstance(final java.util.Collection<ServiceInstance> services, final String fieldName) {
		for (final ServiceInstance instance : services) {
			if (instance.getName().equals(fieldName)) {
				return instance;
			}
		}
		return null;
	}

	public static class InactiveServiceException extends RuntimeException {
		public InactiveServiceException(final ServiceInstance service) {
			super();
		}
	}
}
