package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;

import static org.assertj.core.api.Assertions.assertThat;

class DemoRuntimeHintsTests {

	@Test
	void registersTomcatProtocolReflectionHints() {
		RuntimeHints hints = new RuntimeHints();

		new DemoRuntimeHints().registerHints(hints, getClass().getClassLoader());

		assertInvokablePublicMethods(hints, "org.apache.coyote.AbstractProtocol");
		assertInvokablePublicMethods(hints, "org.apache.coyote.http11.AbstractHttp11Protocol");
		assertInvokablePublicMethods(hints, "org.apache.coyote.http11.Http11NioProtocol");
		assertInvokablePublicMethods(hints, "org.apache.tomcat.util.net.AbstractEndpoint");
		assertInvokablePublicMethods(hints, "org.apache.tomcat.util.net.NioEndpoint");
		assertInvokablePublicMethods(hints, "org.apache.tomcat.util.net.SocketProperties");
	}

	@Test
	void registersSpringAiEmbeddingReflectionHints() {
		RuntimeHints hints = new RuntimeHints();

		new DemoRuntimeHints().registerHints(hints, getClass().getClassLoader());

		assertInvokablePublicMethods(hints, "org.springframework.ai.embedding.EmbeddingOptions");
		assertInvokablePublicConstructorsAndMethods(hints, "org.springframework.ai.embedding.DefaultEmbeddingOptions");
		assertInvokablePublicConstructorsAndMethods(hints, "org.springframework.ai.openai.OpenAiEmbeddingOptions");
	}

	private void assertInvokablePublicMethods(RuntimeHints hints, String typeName) {
		var typeHint = hints.reflection().getTypeHint(TypeReference.of(typeName));

		assertThat(typeHint).isNotNull();
		assertThat(typeHint.getMemberCategories()).contains(MemberCategory.INVOKE_PUBLIC_METHODS);
	}

	private void assertInvokablePublicConstructorsAndMethods(RuntimeHints hints, String typeName) {
		var typeHint = hints.reflection().getTypeHint(TypeReference.of(typeName));

		assertThat(typeHint).isNotNull();
		assertThat(typeHint.getMemberCategories()).contains(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
				MemberCategory.INVOKE_PUBLIC_METHODS);
	}

}
