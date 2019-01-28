package nl.gingerbeard.automation.service.exception;

import java.util.Collection;

import nl.gingerbeard.automation.service.ComponentDefinition;
import nl.gingerbeard.automation.service.ServiceRegistry;

public class UnresolvedDependencyException extends ComponentException {
	private static final long serialVersionUID = 4652387655373227167L;

	private static String getExceptionText(final Collection<ComponentDefinition> components, final ServiceRegistry registry) {
		final StringBuilder exceptionText = new StringBuilder("The following dependencies could not be resolved (missing or circular dependency):");
		for (final ComponentDefinition componentDefinition : components) {

			exceptionText.append("\n").append(componentDefinition).append(" missing ").append(componentDefinition.getUnResolvedFieldNames(registry));
		}

		return exceptionText.toString();
	}

	public UnresolvedDependencyException(final Collection<ComponentDefinition> components, final ServiceRegistry registry) {
		super(getExceptionText(components, registry));
	}
}