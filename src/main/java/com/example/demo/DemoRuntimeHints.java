package com.example.demo;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class DemoRuntimeHints implements RuntimeHintsRegistrar {

	private static final String[] TOMCAT_REFLECTION_TYPES = {
			"org.apache.coyote.AbstractProtocol",
			"org.apache.coyote.http11.AbstractHttp11Protocol",
			"org.apache.coyote.http11.Http11NioProtocol",
			"org.apache.tomcat.util.net.AbstractEndpoint",
			"org.apache.tomcat.util.net.NioEndpoint",
			"org.apache.tomcat.util.net.SocketProperties"
	};

	private static final String[] SPRING_AI_REFLECTION_TYPES = {
			"org.springframework.ai.embedding.EmbeddingOptions",
			"org.springframework.ai.embedding.DefaultEmbeddingOptions",
			"org.springframework.ai.openai.OpenAiEmbeddingOptions"
	};

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		for (String tomcatReflectionType : TOMCAT_REFLECTION_TYPES) {
			hints.reflection()
					.registerTypeIfPresent(classLoader, tomcatReflectionType, MemberCategory.INVOKE_PUBLIC_METHODS);
		}
		for (String springAiReflectionType : SPRING_AI_REFLECTION_TYPES) {
			hints.reflection()
					.registerTypeIfPresent(classLoader, springAiReflectionType, MemberCategory.INVOKE_PUBLIC_METHODS);
		}
		hints.reflection()
				.registerTypeIfPresent(classLoader, "org.springframework.ai.embedding.DefaultEmbeddingOptions",
						MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
		hints.reflection()
				.registerTypeIfPresent(classLoader, "org.springframework.ai.openai.OpenAiEmbeddingOptions",
						MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
	}

}
