package com.skinchanger.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.skinchanger.client.CustomCapeManager;
import com.skinchanger.client.CustomElytraManager;
import com.skinchanger.client.CustomSkinManager;
import net.minecraft.client.Minecraft;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class SkinChangerConfig {
    // Pretty-printing makes the JSON file readable in a text editor!
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save() {
        try {
            // Put it right in the standard Minecraft config folder
            Path configPath = Minecraft.getInstance().gameDirectory.toPath().resolve("config/skinchanger.json");
            Files.createDirectories(configPath.getParent());

            JsonObject json = new JsonObject();

            // 1. Skin Data
            json.addProperty("activeSkin", CustomSkinManager.INSTANCE.getCurrentActiveSkinName());
            json.addProperty("useSlimModel", CustomSkinManager.INSTANCE.isUsingSlimModel());

            // 2. Cape Data
            json.addProperty("activeCape", CustomCapeManager.INSTANCE.getCurrentActiveCapeName()); // Nullable!
            json.addProperty("forceCapeElytra", CustomElytraManager.INSTANCE.shouldForceCapeElytra());

            // 3. Elytra Data
            json.addProperty("activeElytra", CustomElytraManager.INSTANCE.getCurrentActiveElytraName()); // Nullable!

            // Write to disk
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(json, writer);
            }
        } catch (Exception e) {
            System.err.println("Failed to save Skin Changer config!");
            e.printStackTrace();
        }
    }

    public static void load() {
        try {
            Path configPath = Minecraft.getInstance().gameDirectory.toPath().resolve("config/skinchanger.json");
            if (!Files.exists(configPath)) return;

            try (Reader reader = Files.newBufferedReader(configPath)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);

                // Load Skin
                if (json.has("activeSkin") && !json.get("activeSkin").isJsonNull()) {
                    CustomSkinManager.INSTANCE.loadAndSetSkin(json.get("activeSkin").getAsString());
                } else {
                    CustomSkinManager.INSTANCE.clearSkin();
                }

                if (json.has("useSlimModel")) {
                    CustomSkinManager.INSTANCE.setUseSlimModel(json.get("useSlimModel").getAsBoolean());
                }

                // Load Cape
                if (json.has("activeCape") && !json.get("activeCape").isJsonNull()) {
                    CustomCapeManager.INSTANCE.loadAndSetCape(json.get("activeCape").getAsString());
                } else {
                    CustomCapeManager.INSTANCE.clearCape();
                }

                if (json.has("forceCapeElytra")) {
                    CustomElytraManager.INSTANCE.setForceCapeElytra(json.get("forceCapeElytra").getAsBoolean());
                }

                // Load Elytra
                if (json.has("activeElytra") && !json.get("activeElytra").isJsonNull()) {
                    String savedElytraName = json.get("activeElytra").getAsString();

                    if (savedElytraName.equals("Custom Cape's Elytra")) {
                        // It's our special button! Don't look for a file.
                        CustomElytraManager.INSTANCE.setForceCapeElytra(true);
                        CustomElytraManager.INSTANCE.setCurrentActiveElytraName("Custom Cape's Elytra");

                    } else if (savedElytraName.equals("Default Elytra")) {
                        CustomElytraManager.INSTANCE.clearElytra();

                    } else {
                        // It's a real file, load it normally!
                        CustomElytraManager.INSTANCE.loadAndSetElytra(savedElytraName);
                        // Ensure the boolean is turned off so the file actually shows up
                        CustomElytraManager.INSTANCE.setForceCapeElytra(false);
                    }
                } else {
                    CustomElytraManager.INSTANCE.clearElytra();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load Skin Changer config!");
            e.printStackTrace();
        }
    }
}