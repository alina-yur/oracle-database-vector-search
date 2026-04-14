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
            Map.entry("kittens", "cat"),
            Map.entry("feline", "cat"),
            Map.entry("dog", "dog"),
            Map.entry("dogs", "dog"),
            Map.entry("puppy", "dog"),
            Map.entry("puppies", "dog"),
            Map.entry("canine", "dog"),
            Map.entry("fish", "fish"),
            Map.entry("fishes", "fish"),
            Map.entry("aquatic", "fish"));
    private static final Map<String, String> TYPE_KEYWORDS = Map.ofEntries(
            Map.entry("food", "food"),
            Map.entry("foods", "food"),
            Map.entry("meal", "food"),
            Map.entry("meals", "food"),
            Map.entry("treat", "food"),
            Map.entry("treats", "food"),
            Map.entry("snack", "food"),
            Map.entry("snacks", "food"),
            Map.entry("toy", "toy"),
            Map.entry("toys", "toy"),
            Map.entry("rope", "toy"),
            Map.entry("laser", "toy"),
            Map.entry("pointer", "toy"),
            Map.entry("brush", "tool"),
            Map.entry("brushes", "tool"),
            Map.entry("comb", "tool"),
            Map.entry("groom", "tool"),
            Map.entry("grooming", "tool"),
            Map.entry("tank", "habitat"),
            Map.entry("habitat", "habitat"),
            Map.entry("aquarium", "habitat"),
            Map.entry("health", "health"),
            Map.entry("chew", "health"),
            Map.entry("chews", "health"),
            Map.entry("supplement", "health"),
            Map.entry("supplements", "health"));

    private final VectorStore vectorStore;

    public PetStoreSearchController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @GetMapping("/search")
    public List<String> search(@RequestParam String query) {
        SearchIntent intent = SearchIntent.fromQuery(query);
        List<Document> results = buildSearchRequests(query, intent).stream()
                .map(this.vectorStore::similaritySearch)
                .filter(found -> !found.isEmpty())
                .findFirst()
                .orElse(List.of());

        return results.stream()
                .map(Document::getText)
                .toList();
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
}
