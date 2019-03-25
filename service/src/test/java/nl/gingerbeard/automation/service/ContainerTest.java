package nl.gingerbeard.automation.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterables;

import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;
import nl.gingerbeard.automation.service.exception.ComponentException;
import nl.gingerbeard.automation.service.exception.UnresolvedDependencyException;

public class ContainerTest {

	private Container container;

	public ContainerTest() {
		container = null;
	}

	@AfterEach
	public void shutdownContainer() {
		if (container != null) {
			container.shutDown();
			container = null;
		}
	}

	static class ProvideRequiresComponent {

		@Provides
		public String myProvides;

		@Requires
		public Integer myRequires;
	}

	public static class ProvidingComponent {

		@Provides
		public String provided;

		@Activate
		public void init() {
			provided = "Hello";
		}
	}

	public static class ProvidingComponent2 {

		@Provides
		public String provided2;

		@Activate
		public void init() {
			provided2 = "Hello2";
		}
	}

	public static class RequiringComponent {
		@Requires
		public String myStringRequire;

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
	public void registerExternalService_superClass_noException() {
		container = new Container();
		container.register(Object.class, "NotAnInteger", 1);
	}

	@Test
	public void registerExternalService_resolved() {
		container = new Container();
		container.register(RequiringComponent.class);
		container.register(String.class, "hello", 1);
		container.start();

		final Optional<RequiringComponent> component = container.getComponent(RequiringComponent.class);

		assertTrue(component.isPresent());
		assertEquals("hello", component.get().myStringRequire);
	}

	public static class ComponentWithOptionalString {
		@Requires
		public Optional<String> optionalString = Optional.empty();
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

	public static class ComponentWithUninitializedOptionalString {
		@Requires
		public Optional<String> optionalString;
	}

	@Test
	public void optionalField_notFilled_setToEmpty() {
		container = new Container();
		container.register(ComponentWithUninitializedOptionalString.class);
		container.start();

		final Optional<ComponentWithUninitializedOptionalString> component = container.getComponent(ComponentWithUninitializedOptionalString.class);

		assertTrue(component.isPresent());
		assertNotNull(component.get().optionalString);
		assertEquals(Optional.empty(), component.get().optionalString);
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
			assertEquals(
					"No default constructor found for class nl.gingerbeard.automation.service.ContainerTest$NoDefConstructorComponent. Ensure that constructor and class are public, static (in case of nested class) and contain no parameters.",
					e.getMessage());
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
			assertEquals(
					"No default constructor found for class nl.gingerbeard.automation.service.ContainerTest$PrivateConstructorComponent. Ensure that constructor and class are public, static (in case of nested class) and contain no parameters.",
					e.getMessage());
		}
	}

	public static class PrivateActivateComponent {
		@Activate
		private void blaat() {

		}

		public void methodToAvoidMisleadingFindbugsWarning() {
			blaat();
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

	public static class ProvidesPrio10Component {
		@Provides(priority = 10)
		public String provide = "10";

	}

	public static class ProvidesPrio11Component {
		@Provides(priority = 11)
		public String provide = "11";
	}

	@Test
	public void providePriority_highestProvided() {
		container = new Container();
		container.register(ProvidesPrio10Component.class, ProvidesPrio11Component.class, RequiringComponent.class);
		container.start();

		final Optional<RequiringComponent> component = container.getComponent(RequiringComponent.class);

		assertTrue(component.isPresent());
		assertEquals("11", component.get().myStringRequire);
	}

	@Test
	public void providePriority_manyReceivesAll() {
		container = new Container();
		container.register(ProvidesPrio10Component.class, ProvidesPrio11Component.class, ManyRequires.class);
		container.start();

		final Optional<ManyRequires> component = container.getComponent(ManyRequires.class);

		assertTrue(component.isPresent());
		final Many<String> many = component.get().strings;
		assertEquals(2, Iterables.size(many));
		assertTrue(Iterables.contains(many, "11"));
		assertTrue(Iterables.contains(many, "10"));
	}

	@Test
	public void registerExternalServiceWithPriority_highestResolved() {
		container = new Container();
		container.register(RequiringComponent.class);
		container.register(String.class, "10", 10);
		container.register(String.class, "11", 11);
		container.start();

		final Optional<RequiringComponent> component = container.getComponent(RequiringComponent.class);

		assertTrue(component.isPresent());
		assertEquals("11", component.get().myStringRequire);
	}

	public static class PrivateRequiresComponent {
		@Requires
		private String privateString;

		// following 2 methods are to satisfy static code checkers on the fact that the field is never used
		public String getPrivateString() {
			return privateString;
		}

		public void setPrivateString(final String privateString) {
			this.privateString = privateString;
		}

	}

	@Test
	public void privateRequiresRejected() {
		container = new Container();
		container.register(PrivateRequiresComponent.class);
		container.register(ProvidingComponent2.class);

		final ComponentException e = assertThrows(ComponentException.class, () -> container.start());
		assertEquals("Service privateString of nl.gingerbeard.automation.service.ContainerTest$PrivateRequiresComponent cannot be set", e.getMessage());
	}

	public static class EmptyComponent {
	}

	@Test
	public void emptyComponent() {
		container = new Container();
		container.register(EmptyComponent.class);
		container.start();
	}

	@Test
	public void registerServiceOfWrongType() {
		container = new Container();
		final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> container.register(String.class, new Object(), 1, 1));
		assertEquals("Service does not implement specified class", e.getMessage());
	}

	@Test
	public void registerSimple_resolves() {
		container = new Container();
		container.register(RequiringComponent.class);

		container.register(String.class, "Test");

		assertDoesNotThrow(() -> container.start());
	}

}
