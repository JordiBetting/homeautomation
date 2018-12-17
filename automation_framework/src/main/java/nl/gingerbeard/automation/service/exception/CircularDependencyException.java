package nl.gingerbeard.automation.service.exception;

import java.util.Collection;

import nl.gingerbeard.automation.service.ComponentDefinition;

public class CircularDependencyException extends ComponentException {
	private static final long serialVersionUID = 690989595748083674L;

	public CircularDependencyException(final Collection<ComponentDefinition> toActivate) {
		super("Loop detected"); // TODO
	}
}