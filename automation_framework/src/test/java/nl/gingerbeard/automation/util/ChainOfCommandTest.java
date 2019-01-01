package nl.gingerbeard.automation.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ChainOfCommandTest {

	static class MyChainOfCommand1 extends ChainOfCommand<String, String> {

		@Override
		protected boolean matches(final String item) {
			return item.startsWith("a");
		}

		@Override
		protected String doWork(final String item) {
			return this.getClass().getSimpleName();
		}
	}

	static class MyChainOfCommand2 extends ChainOfCommand<String, String> {

		@Override
		protected boolean matches(final String item) {
			return item.startsWith("b");
		}

		@Override
		protected String doWork(final String item) {
			return this.getClass().getSimpleName();
		}
	}

	@Test
	public void execute() {
		Optional<String> result;
		final MyChainOfCommand1 first = new MyChainOfCommand1();
		final MyChainOfCommand2 next = new MyChainOfCommand2();
		first.setNextLink(next);

		result = first.execute("alphabet");
		assertTrue(result.isPresent());
		assertEquals("MyChainOfCommand1", result.get());

		result = first.execute("bird");
		assertTrue(result.isPresent());
		assertEquals("MyChainOfCommand2", result.get());
	}

	@Test
	public void execute_notProcesssed() {
		final MyChainOfCommand1 first = new MyChainOfCommand1();
		final MyChainOfCommand2 next = new MyChainOfCommand2();
		first.setNextLink(next);

		final Optional<String> result = first.execute("notImplemented");

		assertEquals(Optional.empty(), result);
	}

	@Test
	public void chainbuilder() {
		Optional<String> result;
		final ChainOfCommand.Builder<String, String> builder = ChainOfCommand.builder();
		final ChainOfCommand<String, String> chain = builder//
				.add(new MyChainOfCommand1())//
				.add(new MyChainOfCommand2())//
				.build();

		result = chain.execute("alphabet");
		assertTrue(result.isPresent());
		assertEquals("MyChainOfCommand1", result.get());

		result = chain.execute("bird");
		assertTrue(result.isPresent());
		assertEquals("MyChainOfCommand2", result.get());
	}

	@Test
	public void chainBuilder_empty_throwsException() {

		final ChainOfCommand.Builder<String, String> builder = ChainOfCommand.builder();
		try {
			builder.build();
			fail("Expected exception");
		} catch (final IllegalStateException e) {
			assertEquals("No links present in chain.", e.getMessage());
		}
	}

}
