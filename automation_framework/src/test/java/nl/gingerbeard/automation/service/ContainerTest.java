package nl.gingerbeard.automation.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterables;

import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Component;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;
import nl.gingerbeard.automation.service.exception.ComponentException;
import nl.gingerbeard.automation.service.exception.UnresolvedDependencyException;

public class ContainerTest {

	@Component
	static class ProvideRequiresComponent {

		@Provides
		public String myProvides;

		@Requires
		public Integer myRequires;
	}

	@Component
	static class ProvidingComponent {

		@Provides
		public String provided;

		@Activate
		public void init() {
			provided = "Hello";
		}
	}

	@Component
	static class ProvidingComponent2 {

		@Provides
		public String provided2;

		@Activate
		public void init() {
			provided2 = "Hello2";
		}
	}

	@Component
	static class RequiringComponent {
		@Requires
		public String myStringRequire;

		@Activate
		public void init() {

		}
	}

	@Test
	public void registerComponent_dependencyResolved() {
		final Container container = new Container();
		container.register(ProvidingComponent.class);
		container.register(RequiringComponent.class);

		container.start();

		// final RequiringComponent receiver = container.getService(String.class);
		final Optional<String> received = container.getService(String.class);
		final Optional<RequiringComponent> require = container.getComponent(RequiringComponent.class);

		assertTrue(require.isPresent());
		assertEquals("Hello", require.get().myStringRequire);

		assertTrue(received.isPresent());
		assertEquals("Hello", received.get());
	}

	@Test
	public void getComponent_doesNotExist_resultEmpty() {
		final Container container = new Container();
		container.register(ProvidingComponent.class);

		container.start();

		final Optional<Void> received = container.getComponent(Void.class);
		assertFalse(received.isPresent());
	}

	@Test
	public void unresolvedDependency_exceptionThrown() {
		final Container container = new Container();
		container.register(RequiringComponent.class);

		try {
			container.start();
			fail("Exception expected");
		} catch (final UnresolvedDependencyException e) {
			assertEquals("The following dependencies could not be resolved (missing or circular dependency):\n" + "nl.gingerbeard.automation.service.ContainerTest$RequiringComponent "
					+ "missing [myStringRequire]", e.getMessage());
		}
	}

	public static class CircularA {
		@Provides
		public CircularA a;

		@Requires
		public CircularB b;
	}

	public static class CircularB {
		@Provides
		public CircularB b;

		@Requires
		public CircularC c;
	}

	public static class CircularC {
		@Provides
		public CircularC c;

		@Requires
		public CircularA a;
	}

	@Test
	public void circularDependency_exceptionThrown() {
		final Container container = new Container();
		container.register(CircularA.class);
		container.register(CircularB.class);
		container.register(CircularC.class);

		try {
			container.start();
			fail("Exception expected");
		} catch (final UnresolvedDependencyException e) {
			assertTrue(e.getMessage().startsWith("The following dependencies could not be resolved (missing or circular dependency):"));
			assertTrue(e.getMessage().contains("nl.gingerbeard.automation.service.ContainerTest$CircularB missing [c]"));
			assertTrue(e.getMessage().contains("nl.gingerbeard.automation.service.ContainerTest$CircularA missing [b]"));
			assertTrue(e.getMessage().contains("nl.gingerbeard.automation.service.ContainerTest$CircularC missing [a]"));
		}
	}

	@Test
	public void emptyContainer_start_noException() {
		final Container container = new Container();
		container.start();
	}

	@Test
	public void getService_notExisting_returnsOptionalEmpty() {
		final Container container = new Container();
		container.start();
		final Optional<String> result = container.getService(String.class);
		assertEquals(Optional.empty(), result);
	}

	@Test
	public void getService_withoutStart_throwsException() {
		final Container container = new Container();

		try {
			container.getService(String.class);
			fail("Expected exception");
		} catch (final ComponentException e) {
			assertEquals("Cannot perform requested operation when Container is not started.", e.getMessage());
		}
	}

	@Test
	public void multipleProvides() {
		final Container container = new Container();
		container.register(ProvidingComponent.class);
		container.register(ProvidingComponent2.class);
		container.start();

		final Collection<String> services = container.getServices(String.class);

		assertEquals(2, services.size());
		final String firstString = Iterables.get(services, 0);
		final String secondString = Iterables.get(services, 1);
		assertTrue(firstString.startsWith("Hello"));
		assertTrue(secondString.startsWith("Hello"));
		assertNotEquals(firstString, secondString);

		// arbitrary order, so use XOR
		assertTrue(firstString.equals("Hello") ^ secondString.equals("Hello"));
		assertTrue(firstString.equals("Hello2") ^ secondString.equals("Hello2"));
	}
}
