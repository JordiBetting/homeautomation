package nl.gingerbeard.automation.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.Optional;

import org.junit.After;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterables;

import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Component;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;
import nl.gingerbeard.automation.service.exception.ComponentException;
import nl.gingerbeard.automation.service.exception.UnresolvedDependencyException;

public class ContainerTest {

	private Container container;

	@After
	public void shutdownContainer() {
		if (container != null) {
			container.shutDown();
			container = null;
		}
	}

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
		container = new Container();
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
		container = new Container();
		container.register(ProvidingComponent.class);

		container.start();

		final Optional<Void> received = container.getComponent(Void.class);
		assertFalse(received.isPresent());
	}

	@Test
	public void unresolvedDependency_exceptionThrown() {
		container = new Container();
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
		container = new Container();
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
		container = new Container();
		container.start();
	}

	@Test
	public void getService_notExisting_returnsOptionalEmpty() {
		container = new Container();
		container.start();
		final Optional<String> result = container.getService(String.class);
		assertEquals(Optional.empty(), result);
	}

	@Test
	public void getService_withoutStart_throwsException() {
		container = new Container();

		try {
			container.getService(String.class);
			fail("Expected exception");
		} catch (final ComponentException e) {
			assertEquals("Cannot perform requested operation when Container is not started.", e.getMessage());
		}
	}

	@Test
	public void multipleProvides() {
		container = new Container();
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

	public static class Leaf {
		@Provides
		public String provide;

		@Requires
		public StringBuilder builder;

		@Activate
		public void init() {
			provide = "hooray";
			builder.append("L");
		}

		@Deactivate
		public void deinit() {
			builder.append("l");
		}
	}

	public static class Trunk {
		@Requires
		public String fromLeaf;

		@Provides
		public Integer middle;

		@Requires
		public StringBuilder builder;

		@Activate
		public void init() {
			middle = 42;
			builder.append("M");
		}

		@Deactivate
		public void deactivate() {
			builder.append("m");
		}
	}

	public static class Root {
		@Requires
		public Integer fromMiddle;

		@Requires
		public StringBuilder builder;

		@Activate
		public void init() {
			builder.append("R");
		}

		@Deactivate
		public void deactivate() {
			builder.append("r");
		}
	}

	@Test
	public void activateDeactivate_orderOfDependencies() {
		final StringBuilder result = new StringBuilder();

		container = new Container();
		container.register(Trunk.class);
		container.register(Root.class);
		container.register(Leaf.class);
		container.register(StringBuilder.class, result, 1);

		container.start();

		shutdownContainer();

		assertEquals("LMRrml", result.toString());
	}

	@Test
	public void registerExternalService_invalidType_throwsException() {
		container = new Container();
		try {
			container.register(Integer.class, "NotAnInteger", 1);
			fail("Expected exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Service does not implement specified class", e.getMessage());
		}
	}

	public static class ComponentWithOptionalString {
		@Requires
		public Optional<String> optionalString = Optional.empty();
		// TODO: Determine if container shall set optional to empty.
	}

	@Test
	public void optionalRequires_filledWithDependency() {
		container = new Container();
		container.register(ComponentWithOptionalString.class, ProvidingComponent.class);
		container.start();

		final Optional<ComponentWithOptionalString> component = container.getComponent(ComponentWithOptionalString.class);

		assertTrue(component.isPresent());
		assertTrue(component.get().optionalString.isPresent());
	}

	@Test
	public void optionalRequires_notFilled_NoException() {
		container = new Container();
		container.register(ComponentWithOptionalString.class);
		container.start();

		final Optional<ComponentWithOptionalString> component = container.getComponent(ComponentWithOptionalString.class);
		assertTrue(component.isPresent());
		assertFalse(component.get().optionalString.isPresent());
	}

	public static class ComponentProvidingNull {
		@Provides
		public String test = null;
	}

	@Test
	public void produces_null_throwNPE() {
		container = new Container();
		container.register(ComponentProvidingNull.class);

		try {
			container.start();
			fail("Expected exception");
		} catch (final ComponentException e) {
			assertEquals("Service test of nl.gingerbeard.automation.service.ContainerTest$ComponentProvidingNull is null", e.getMessage());
		}
	}

	public static class ManyRequires {
		@Requires
		public Many<String> strings;

		@Provides
		public ManyRequires instance;

		@Activate
		public void initInstance() {
			instance = this;
		}
	}

	@Test
	public void requiresMany_works() {
		container = new Container();
		container.register(ManyRequires.class, ProvidingComponent.class, ProvidingComponent2.class);

		container.start();

		final Optional<ManyRequires> manyRequires = container.getService(ManyRequires.class);
		assertTrue(manyRequires.isPresent());
		final Many<String> many = manyRequires.get().strings;

		assertEquals(2, Iterables.size(many));
		final String firstString = Iterables.get(many, 0);
		final String secondString = Iterables.get(many, 1);

		assertTrue(firstString.startsWith("Hello"));
		assertTrue(secondString.startsWith("Hello"));
		assertNotEquals(firstString, secondString);

		// arbitrary order, so use XOR
		assertTrue(firstString.equals("Hello") ^ secondString.equals("Hello"));
		assertTrue(firstString.equals("Hello2") ^ secondString.equals("Hello2"));
	}

	public static class PrivateProvides {
		@Provides
		private final Object provides = new Object();
	}

	@Test
	public void privateProvides_throwsException() {
		container = new Container();
		container.register(PrivateProvides.class);

		try {
			container.start();
		} catch (final ComponentException e) {
			assertEquals("Service provides of nl.gingerbeard.automation.service.ContainerTest$PrivateProvides cannot be read", e.getMessage());
		}
	}

	public static class PrivateRequires {
		@Requires
		private Object provides;
	}

	@Test
	public void privateRequires_throwsException() {
		container = new Container();
		container.register(PrivateRequires.class);
		container.register(Object.class, new Object(), 1);

		try {
			container.start();
		} catch (final ComponentException e) {
			assertEquals("Service provides of nl.gingerbeard.automation.service.ContainerTest$PrivateRequires cannot be set", e.getMessage());
		}
	}

	public static class NoDefConstructorComponent {
		public NoDefConstructorComponent(final Object haha) {

		}
	}

	@Test
	public void noDefaultConstructor_throwsException() {
		container = new Container();
		try {
			container.register(NoDefConstructorComponent.class);
			fail("Expected exception");
		} catch (final ComponentException e) {
			assertEquals("No default constructor found for class nl.gingerbeard.automation.service.ContainerTest$NoDefConstructorComponent", e.getMessage());
		}
	}

	public static class PrivateConstructorComponent {
		private PrivateConstructorComponent() {

		}
	}

	@Test
	public void privateConstructor_throwsException() {
		container = new Container();
		try {
			container.register(PrivateConstructorComponent.class);
			fail("Expected exception");
		} catch (final ComponentException e) {
			assertEquals("No default constructor found for class nl.gingerbeard.automation.service.ContainerTest$PrivateConstructorComponent", e.getMessage());
		}
	}

	public static class PrivateActivateComponent {
		@Activate
		private void blaat() {

		}
	}

	@Test
	public void privateActivateMethod_throwsException() {
		container = new Container();
		container.register(PrivateActivateComponent.class);
		try {
			container.start();
			fail("Expected exception");
		} catch (final ComponentException e) {
			assertEquals("Method blaat of nl.gingerbeard.automation.service.ContainerTest$PrivateActivateComponent failed", e.getMessage());
		}
	}

	@Test
	public void shutdownBeforeStart_noException() {
		container = new Container();
		container.register(Leaf.class);

		shutdownContainer();
	}

}
