package com.example.demo;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/petstore")
public class PetStoreSearchController {

    private static final int SEARCH_LIMIT = 10;
    private static final double UNFILTERED_SIMILARITY_THRESHOLD = 0.4;
    private static final Map<String, String> ANIMAL_KEYWORDS = Map.ofEntries(
            Map.entry("cat", "cat"),
            Map.entry("cats", "cat"),
            Map.entry("kitten", "cat"),
            Map.entry("dog", "dog"),
            Map.entry("dogs", "dog"),
            Map.entry("puppy", "dog"),
            Map.entry("fish", "fish"),
            Map.entry("fishes", "fish"));
    private static final Map<String, String> TYPE_KEYWORDS = Map.ofEntries(
            Map.entry("food", "food"),
            Map.entry("foods", "food"),
            Map.entry("treat", "food"),
            Map.entry("treats", "food"),
            Map.entry("toy", "toy"),
            Map.entry("toys", "toy"),
            Map.entry("laser", "toy"),
            Map.entry("brush", "tool"),
            Map.entry("grooming", "tool"),
            Map.entry("tank", "habitat"),
            Map.entry("tanks", "habitat"),
            Map.entry("aquarium", "habitat"),
            Map.entry("aquariums", "habitat"),
            Map.entry("health", "health"),
            Map.entry("supplement", "health"));

    private final VectorStore vectorStore;

    public PetStoreSearchController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @GetMapping("/search")
    public List<String> search(@RequestParam String query) {
        return searchDocuments(query).stream()
                .map(Document::getText)
                .toList();
    }

    @GetMapping("/search/products")
    public List<ProductResult> searchProducts(@RequestParam String query) {
        return searchDocuments(query).stream()
                .map(ProductResult::from)
                .toList();
    }

    private List<Document> searchDocuments(String query) {
        SearchIntent intent = SearchIntent.fromQuery(query);
        return buildSearchRequests(query, intent).stream()
                .map(this.vectorStore::similaritySearch)
                .filter(found -> !found.isEmpty())
                .findFirst()
                .orElse(List.of());
    }

    private List<SearchRequest> buildSearchRequests(String query, SearchIntent intent) {
        Set<SearchRequest> requests = new LinkedHashSet<>();

        requests.add(createSearchRequest(query, buildFilterExpression(intent.animal(), intent.type())));
        requests.add(createSearchRequest(query, buildFilterExpression(intent.animal(), null)));
        requests.add(createSearchRequest(query, buildFilterExpression(null, intent.type())));
        requests.add(createSearchRequest(query, null));

        return List.copyOf(requests);
    }

    private SearchRequest createSearchRequest(String query, org.springframework.ai.vectorstore.filter.Filter.Expression filterExpression) {
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(query)
                .topK(SEARCH_LIMIT)
                .similarityThreshold(filterExpression == null ? UNFILTERED_SIMILARITY_THRESHOLD
                        : SearchRequest.SIMILARITY_THRESHOLD_ACCEPT_ALL);

        if (filterExpression != null) {
            builder.filterExpression(filterExpression);
        }

        return builder.build();
    }

    private org.springframework.ai.vectorstore.filter.Filter.Expression buildFilterExpression(String animal, String type) {
        FilterExpressionBuilder filters = new FilterExpressionBuilder();
        FilterExpressionBuilder.Op animalFilter = animal == null ? null : filters.eq("animal", animal);
        FilterExpressionBuilder.Op typeFilter = type == null ? null : filters.eq("type", type);

        if (animalFilter != null && typeFilter != null) {
            return filters.and(animalFilter, typeFilter).build();
        }
        if (animalFilter != null) {
            return animalFilter.build();
        }
        if (typeFilter != null) {
            return typeFilter.build();
        }
        return null;
    }

    private record SearchIntent(String animal, String type) {

        private static SearchIntent fromQuery(String query) {
            List<String> tokens = Arrays.stream(query.toLowerCase(Locale.ROOT).split("[^a-z]+"))
                    .filter(token -> !token.isBlank())
                    .toList();

            return new SearchIntent(resolveIntentValue(tokens, ANIMAL_KEYWORDS),
                    resolveIntentValue(tokens, TYPE_KEYWORDS));
        }

        private static String resolveIntentValue(List<String> tokens, Map<String, String> keywords) {
            return tokens.stream()
                    .map(keywords::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

    }

    public record ProductResult(String name, Integer price, String type, String animal) {

        private static ProductResult from(Document document) {
            Map<String, Object> metadata = document.getMetadata() == null ? Map.of() : document.getMetadata();
            return new ProductResult(document.getText(), integerValue(metadata.get("price")),
                    stringValue(metadata.get("type")), stringValue(metadata.get("animal")));
        }

        private static String stringValue(Object value) {
            return value == null ? null : value.toString();
        }

        private static Integer integerValue(Object value) {
            if (value instanceof Number number) {
                return number.intValue();
            }
            if (value instanceof String text) {
                try {
                    return Integer.parseInt(text);
                }
                catch (NumberFormatException ignored) {
                    return null;
                }
            }
            return null;
        }

    }
}
