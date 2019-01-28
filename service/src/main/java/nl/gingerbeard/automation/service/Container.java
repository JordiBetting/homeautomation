package nl.gingerbeard.automation.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import nl.gingerbeard.automation.service.exception.ComponentException;
import nl.gingerbeard.automation.service.exception.UnresolvedDependencyException;

public final class Container {
	private static final Logger LOG = Logger.getLogger(Container.class.getName());
	private static final ComponentDefinition CONTEXT_COMPONENT = new ComponentDefinition(42, Container.class);

	public Container() {
		components = Sets.newHashSet();
		toActivate = Sets.newHashSet();
		activationSequence = Lists.newArrayList();
		registry = new ServiceRegistry();
		isStarted = false;
	}

	public void register(final Class<?>... componentClasses) {
		register(42, componentClasses);
	}

	private final Set<ComponentDefinition> components;
	private final Set<ComponentDefinition> toActivate;

	private void registerComponents(final int componentPriority, final Class<?>... componentClasses) {
		for (final Class<?> clazz : componentClasses) {
			registerComponent(componentPriority, clazz);
		}
	}

	private void registerComponent(final int componentPriority, final Class<?> clazz) {
		final ComponentDefinition component = new ComponentDefinition(componentPriority, clazz);
		components.add(component);
	}

	private void resolveComponents() {
		boolean isDone = false;
		while (!isDone) {
			final int activationCount = toActivate.size();
			attemptToResolveComponents();
			isDone = toActivate.size() == activationCount;
		}
	}

	private void attemptToResolveComponents() {
		for (final ComponentDefinition def : components) {
			resolveComponent(def);
		}
	}

	private void resolveComponent(final ComponentDefinition def) {
		if (!def.isResolved()) {
			def.resolve(registry);
			if (def.isResolved()) {
				toActivate.add(def);
			}
		}
	}

	public void start() {
		resolveComponents();
		activateComponents();
		if (components.size() != activationSequence.size()) {
			components.removeAll(activationSequence);
			throw new UnresolvedDependencyException(components, registry);
		}
		isStarted = true;
	}

	private void activateComponents() {
		boolean isDone = false;
		while (!isDone) {
			final int activationCount = toActivate.size();
			attemptToActivateComponents();
			isDone = toActivate.size() == activationCount;
		}
	}

	private void attemptToActivateComponents() {
		for (final ComponentDefinition def : Lists.newArrayList(toActivate)) {
			def.activate(registry);
			if (def.isActive()) {
				LOG.fine("Component " + def + " is activated.");
				toActivate.remove(def);
				activationSequence.add(def);
			}
		}
	}

	private void validateContainerStarted() {
		if (!isStarted) {
			throw new ComponentException("Cannot perform requested operation when Container is not started.");
		}
	}

	public void shutDown() {
		isStarted = false;
		final ArrayList<ComponentDefinition> wasActive = Lists.newArrayList(activationSequence);
		while (!activationSequence.isEmpty()) {
			removeLastActivatedComponent().deactivate();
		}
		toActivate.addAll(wasActive);
	}

	private ComponentDefinition removeLastActivatedComponent() {
		return activationSequence.remove(activationSequence.size() - 1);
	}

	public <T> void register(final Class<T> clazz, final T service, final int producePriority) {
		register(clazz, service, producePriority, 42);
	}

	public void register(final Class<?> clazz, final Object service, final int producePriority, final int componentPriority) {
		if (!clazz.isInstance(service)) {
			throw new IllegalArgumentException("Service does not implement specified class");
		}
		final ServiceInstance serviceInstance = new ServiceInstance("", clazz, producePriority, componentPriority);
		serviceInstance.setService(service);
		registry.registerService(CONTEXT_COMPONENT, serviceInstance);
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<T> getService(final Class<T> clazz) {
		validateContainerStarted();
		return (Optional<T>) registry.getService(clazz);
	}

	public <T> Collection<T> getServices(final Class<T> clazz) {
		validateContainerStarted();
		return registry.getServices(clazz);
	}

	private final List<ComponentDefinition> activationSequence;
	private final ServiceRegistry registry;
	private boolean isStarted;

	public void register(final int componentPriority, final Class<?>... componentClasses) {
		registerComponents(componentPriority, componentClasses);
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<T> getComponent(final Class<T> componentClass) {
		for (final ComponentDefinition def : components) {
			if (componentClass.equals(def.getComponent().getClass())) {
				return Optional.of((T) def.getComponent());
			}
		}
		return Optional.empty();
	}

	public <T> void register(final Class<T> class1, final T config) {
		register(class1, config, 1);
	}
}
