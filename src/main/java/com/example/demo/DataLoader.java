package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private final VectorStore vectorStore;

    public DataLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Document> inventory = List.of(
                product("Labrador Bark Control Chews", 15, "health", "dog"),
                product("Heavy Duty Rope for Large Dog Breeds", 12, "toy", "dog"),
                product("Grain-Free Beef Dog Food for Active Dogs", 24, "food", "dog"),
                product("Salmon and Brown Rice Dog Food", 22, "food", "dog"),
                product("Turkey and Sweet Potato Dog Food", 21, "food", "dog"),
                product("Peanut Butter Training Treats for Dogs", 9, "food", "dog"),
                product("Dental Stick Chews for Small Dogs", 13, "health", "dog"),
                product("Chicken Jerky Bites for Dogs", 11, "food", "dog"),
                product("Squeaky Tennis Ball Pack for Dogs", 10, "toy", "dog"),
                product("Rubber Flying Disc for Dogs", 14, "toy", "dog"),
                product("Deshedding Brush for Double-Coat Dogs", 16, "tool", "dog"),
                product("Cooling Dog Mat for Summer Travel", 28, "habitat", "dog"),

                product("Silent Laser Pointer for Kittens", 8, "toy", "cat"),
                product("Hair brush for Long Hair Cats", 5, "tool", "cat"),
                product("Gourmet Tuna Soufflé for Sphynx Cats", 18, "food", "cat"),
                product("Gourmet Chicken Soup for Senior Cats", 12, "food", "cat"),
                product("Indoor Chicken and Rice Cat Food", 19, "food", "cat"),
                product("Salmon Pate Wet Cat Food", 17, "food", "cat"),
                product("Catnip Mouse Toy for House Cats", 7, "toy", "cat"),
                product("Feather Wand Toy for Curious Cats", 9, "toy", "cat"),
                product("Crunchy Dental Treats for Cats", 8, "health", "cat"),
                product("Urinary Support Chews for Cats", 14, "health", "cat"),
                product("Self-Cleaning Slicker Brush for Cats", 13, "tool", "cat"),
                product("Window Perch for Indoor Cats", 32, "habitat", "cat"),

                product("Fish Tank for Small Fish", 48, "habitat", "fish"),
                product("Tropical Flake Food for Community Fish", 6, "food", "fish"),
                product("Sinking Pellets for Goldfish", 7, "food", "fish"),
                product("Aquarium Water Conditioner for Freshwater Fish", 11, "health", "fish"),
                product("Decorative Cave for Betta Fish", 15, "habitat", "fish"),
                product("Quiet Bubble Filter for Fish Tanks", 22, "habitat", "fish"),
                product("Floating Thermometer for Fish Tanks", 6, "tool", "fish"),
                product("Fine Mesh Net for Aquarium Fish", 5, "tool", "fish"));

        vectorStore.add(inventory);

        log.info("Successfully loaded {} products into vector store", inventory.size());
    }

    private static Document product(String name, int price, String type, String animal) {
        return new Document(name, Map.of("price", price, "type", type, "animal", animal));
    }
}
