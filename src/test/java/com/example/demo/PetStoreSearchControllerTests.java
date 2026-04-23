package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PetStoreSearchControllerTests {

	private static final int SEARCH_LIMIT = 10;
	private static final double UNFILTERED_SIMILARITY_THRESHOLD = 0.4;

	@Test
	void usesAnimalAndTypeFilterWhenBothArePresentInTheQuery() {
		var responses = new LinkedHashMap<SearchRequest, List<Document>>();
		responses.put(request("find cat food", filter("cat", "food")), List.of(
				new Document("Gourmet Tuna Soufflé for Sphynx Cats"),
				new Document("Gourmet Chicken Soup for Senior Cats")));
		responses.put(request("find cat food", null), List.of(
				new Document("Hair brush for Long Hair Cats"),
				new Document("Silent Laser Pointer for Kittens")));

		var vectorStore = new StubVectorStore(responses);
		var controller = new PetStoreSearchController(vectorStore);

		assertThat(controller.search("find cat food"))
				.containsExactly("Gourmet Tuna Soufflé for Sphynx Cats", "Gourmet Chicken Soup for Senior Cats");
		assertThat(vectorStore.requests()).containsExactly(request("find cat food", filter("cat", "food")));
	}

	@Test
	void fallsBackToAnimalFilterWhenSpecificTypeDoesNotExist() {
		var responses = new LinkedHashMap<SearchRequest, List<Document>>();
		responses.put(request("find dog food", filter("dog", null)), List.of(
				new Document("Labrador Bark Control Chews"),
				new Document("Heavy Duty Rope for Large Dog Breeds")));
		responses.put(request("find dog food", filter(null, "food")), List.of(
				new Document("Gourmet Chicken Soup for Senior Cats"),
				new Document("Gourmet Tuna Soufflé for Sphynx Cats")));
		responses.put(request("find dog food", null), List.of(
				new Document("Gourmet Chicken Soup for Senior Cats"),
				new Document("Gourmet Tuna Soufflé for Sphynx Cats")));

		var vectorStore = new StubVectorStore(responses);
		var controller = new PetStoreSearchController(vectorStore);

		assertThat(controller.search("find dog food"))
				.containsExactly("Labrador Bark Control Chews", "Heavy Duty Rope for Large Dog Breeds");
		assertThat(vectorStore.requests()).containsExactly(
				request("find dog food", filter("dog", "food")),
				request("find dog food", filter("dog", null)));
	}

	@Test
	void usesTypeFilterBeforeBroadSemanticSearchWhenNoAnimalIsPresent() {
		var responses = new LinkedHashMap<SearchRequest, List<Document>>();
		responses.put(request("find food", filter(null, "food")), List.of(
				new Document("Gourmet Tuna Soufflé for Sphynx Cats"),
				new Document("Gourmet Chicken Soup for Senior Cats")));
		responses.put(request("find food", null), List.of(
				new Document("Labrador Bark Control Chews"),
				new Document("Hair brush for Long Hair Cats")));

		var vectorStore = new StubVectorStore(responses);
		var controller = new PetStoreSearchController(vectorStore);

		assertThat(controller.search("find food"))
				.containsExactly("Gourmet Tuna Soufflé for Sphynx Cats", "Gourmet Chicken Soup for Senior Cats");
		assertThat(vectorStore.requests()).containsExactly(request("find food", filter(null, "food")));
	}

	@Test
	void recognizesPluralHabitatKeywordsForFishQueries() {
		var responses = new LinkedHashMap<SearchRequest, List<Document>>();
		responses.put(request("fish tanks", filter("fish", "habitat")), List.of(
				new Document("Fish Tank for Small Fish"),
				new Document("Quiet Bubble Filter for Fish Tanks")));
		responses.put(request("do you have fish aquariums", filter("fish", "habitat")), List.of(
				new Document("Fish Tank for Small Fish"),
				new Document("Quiet Bubble Filter for Fish Tanks")));

		var vectorStore = new StubVectorStore(responses);
		var controller = new PetStoreSearchController(vectorStore);

		assertThat(controller.search("fish tanks"))
				.containsExactly("Fish Tank for Small Fish", "Quiet Bubble Filter for Fish Tanks");
		assertThat(controller.search("do you have fish aquariums"))
				.containsExactly("Fish Tank for Small Fish", "Quiet Bubble Filter for Fish Tanks");
		assertThat(vectorStore.requests()).containsExactly(
				request("fish tanks", filter("fish", "habitat")),
				request("do you have fish aquariums", filter("fish", "habitat")));
	}

	@Test
	void returnsStructuredProductResultsForTheUi() {
		var responses = new LinkedHashMap<SearchRequest, List<Document>>();
		responses.put(request("dog toys", filter("dog", "toy")), List.of(
				new Document("Heavy Duty Rope for Large Dog Breeds",
						Map.of("price", 12, "type", "toy", "animal", "dog")),
				new Document("Rubber Flying Disc for Dogs",
						Map.of("price", 14, "type", "toy", "animal", "dog"))));

		var vectorStore = new StubVectorStore(responses);
		var controller = new PetStoreSearchController(vectorStore);

		assertThat(controller.searchProducts("dog toys")).containsExactly(
				new PetStoreSearchController.ProductResult("Heavy Duty Rope for Large Dog Breeds", 12, "toy", "dog"),
				new PetStoreSearchController.ProductResult("Rubber Flying Disc for Dogs", 14, "toy", "dog"));
		assertThat(vectorStore.requests()).containsExactly(request("dog toys", filter("dog", "toy")));
	}

	private static SearchRequest request(String query, org.springframework.ai.vectorstore.filter.Filter.Expression filterExpression) {
		var builder = SearchRequest.builder()
				.query(query)
				.topK(SEARCH_LIMIT)
				.similarityThreshold(filterExpression == null ? UNFILTERED_SIMILARITY_THRESHOLD
						: SearchRequest.SIMILARITY_THRESHOLD_ACCEPT_ALL);

		if (filterExpression != null) {
			builder.filterExpression(filterExpression);
		}

		return builder.build();
	}

	private static org.springframework.ai.vectorstore.filter.Filter.Expression filter(String animal, String type) {
		var filters = new FilterExpressionBuilder();
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

	private static final class StubVectorStore implements VectorStore {

		private final Map<SearchRequest, List<Document>> responses;
		private final List<SearchRequest> requests = new ArrayList<>();

		private StubVectorStore(Map<SearchRequest, List<Document>> responses) {
			this.responses = responses;
		}

		@Override
		public List<Document> similaritySearch(SearchRequest request) {
			this.requests.add(request);
			return this.responses.getOrDefault(request, List.of());
		}

		@Override
		public void add(List<Document> documents) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void delete(List<String> idList) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void delete(org.springframework.ai.vectorstore.filter.Filter.Expression filterExpression) {
			throw new UnsupportedOperationException();
		}

		private List<SearchRequest> requests() {
			return this.requests;
		}

	}

}
