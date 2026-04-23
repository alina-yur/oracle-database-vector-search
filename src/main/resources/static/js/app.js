const searchForm = document.querySelector("#search-form");
const searchInput = document.querySelector("#search-input");
const searchButton = document.querySelector("#search-button");
const resultsRoot = document.querySelector("#results-grid");
const resultsTitle = document.querySelector("#results-title");
const resultsSummary = document.querySelector("#results-summary");
const suggestionButtons = document.querySelectorAll("[data-query]");
const currencyFormatter = new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0
});

const defaultQuery = "cat food with tuna";

searchForm.addEventListener("submit", (event) => {
    event.preventDefault();
    runSearch(searchInput.value);
});

suggestionButtons.forEach((button) => {
    button.addEventListener("click", () => {
        searchInput.value = button.dataset.query;
        runSearch(button.dataset.query);
    });
});

searchInput.value = defaultQuery;
runSearch(defaultQuery);

async function runSearch(query) {
    const normalizedQuery = query.trim();
    if (!normalizedQuery) {
        searchInput.focus();
        return;
    }

    setLoadingState(normalizedQuery);

    try {
        const response = await fetch(`/petstore/search/products?query=${encodeURIComponent(normalizedQuery)}`);
        if (!response.ok) {
            throw new Error(`Unexpected response status: ${response.status}`);
        }

        const products = await response.json();
        renderResults(normalizedQuery, products);
    }
    catch (error) {
        renderError(normalizedQuery, error);
    }
}

function setLoadingState(query) {
    searchButton.disabled = true;
    searchButton.textContent = "Searching";
    resultsTitle.textContent = `Searching for "${query}"`;
    resultsSummary.textContent = "Loading products.";
    resultsRoot.replaceChildren(createSkeleton(), createSkeleton(), createSkeleton());
}

function renderResults(query, products) {
    searchButton.disabled = false;
    searchButton.textContent = "Search";

    resultsTitle.textContent = `Results for "${query}"`;
    resultsSummary.textContent = products.length === 0
            ? "No products found."
            : `${products.length} result${products.length === 1 ? "" : "s"}.`;

    if (products.length === 0) {
        resultsRoot.replaceChildren();
        return;
    }

    resultsRoot.replaceChildren(...products.map(createProductCard));
}

function renderError(query, error) {
    console.error("Search failed", error);
    searchButton.disabled = false;
    searchButton.textContent = "Search";
    resultsRoot.replaceChildren();
    resultsTitle.textContent = `Search unavailable for "${query}"`;
    resultsSummary.textContent = "Backend unavailable.";
}

function createProductCard(product) {
    const article = document.createElement("article");
    article.className = `product-card product-card--${product.animal || "default"}`;

    const head = document.createElement("div");
    head.className = "product-card-head";

    const title = document.createElement("h3");
    title.textContent = product.name;

    head.append(title);

    if (typeof product.price === "number") {
        const price = document.createElement("span");
        price.className = "price-tag";
        price.textContent = currencyFormatter.format(product.price);
        head.append(price);
    }

    const badgeRow = document.createElement("div");
    badgeRow.className = "badge-row";
    badgeRow.append(
            createBadge(product.animal ? animalLabel(product.animal) : "Catalog item"),
            createBadge(product.type ? typeLabel(product.type) : "General")
    );

    article.append(head, badgeRow);
    return article;
}

function createBadge(label) {
    const badge = document.createElement("span");
    badge.className = "badge";
    badge.textContent = label;
    return badge;
}

function createSkeleton() {
    const skeleton = document.createElement("div");
    skeleton.className = "product-card skeleton";
    return skeleton;
}

function animalLabel(animal) {
    return animal.charAt(0).toUpperCase() + animal.slice(1);
}

function typeLabel(type) {
    return type.charAt(0).toUpperCase() + type.slice(1);
}
