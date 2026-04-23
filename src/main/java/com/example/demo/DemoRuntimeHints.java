package com.example.demo;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

import java.util.List;

public class DemoRuntimeHints implements RuntimeHintsRegistrar {

    private static final List<String> INVOKABLE_PUBLIC_METHOD_TYPES = List.of(
            "org.apache.coyote.AbstractProtocol",
            "org.apache.coyote.http11.AbstractHttp11Protocol",
            "org.apache.coyote.http11.Http11NioProtocol",
            "org.apache.tomcat.util.net.AbstractEndpoint",
            "org.apache.tomcat.util.net.NioEndpoint",
            "org.apache.tomcat.util.net.SocketProperties");
    private static final List<String> INVOKABLE_PUBLIC_CONSTRUCTOR_AND_METHOD_TYPES = List.of(
            "org.springframework.ai.embedding.DefaultEmbeddingOptions",
            "org.springframework.ai.openai.OpenAiEmbeddingOptions");
    private static final List<String> SPRING_AI_METHOD_ONLY_TYPES = List.of(
            "org.springframework.ai.embedding.EmbeddingOptions");

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        INVOKABLE_PUBLIC_METHOD_TYPES.forEach(typeName -> hints.reflection().registerType(TypeReference.of(typeName),
                MemberCategory.INVOKE_PUBLIC_METHODS));
        SPRING_AI_METHOD_ONLY_TYPES.forEach(typeName -> hints.reflection().registerType(TypeReference.of(typeName),
                MemberCategory.INVOKE_PUBLIC_METHODS));
        INVOKABLE_PUBLIC_CONSTRUCTOR_AND_METHOD_TYPES.forEach(typeName -> hints.reflection()
                .registerType(TypeReference.of(typeName), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS));
    }
}
