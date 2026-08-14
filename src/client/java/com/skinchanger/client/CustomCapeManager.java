package com.skinchanger.client;

import net.fabricmc.loader.api.FabricLoader;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomCapeManager {

    // The single instance of our manager
    public static final CustomCapeManager INSTANCE = new CustomCapeManager();

    // The path to the .minecraft/custom_capes folder
    private final Path capesFolder;

    // The dynamic Identifier that points to the currently active custom cape
    private Identifier currentActiveCapeId = null;
    private String currentActiveCapeName = null;
    private final Map<String, Identifier> previewCache = new HashMap<>();

    private CustomCapeManager() {
        // Find the .minecraft folder and append /custom_capes
        this.capesFolder = FabricLoader.getInstance().getGameDir().resolve("custom_capes");

        // If the folder doesn't exist yet, create it.
        try {
            if (!Files.exists(capesFolder)) {
                Files.createDirectories(capesFolder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadAndSetCape(String fileName) {
        Path capeFile = capesFolder.resolve(fileName);

        if (!Files.exists(capeFile)) {
            System.out.println("Cape file not found: " + capeFile);
            return;
        }

        // Use execute() to safely run this on the main graphics thread
        Minecraft.getInstance().execute(() -> {
            try (InputStream inputStream = new FileInputStream(capeFile.toFile())) {
                NativeImage nativeImage = NativeImage.read(inputStream);

                DynamicTexture dynamicTexture = new DynamicTexture(() -> "custom_player_cape", nativeImage);

                Minecraft client = Minecraft.getInstance();

                Identifier newCapeId = Identifier.fromNamespaceAndPath("skin-changer", "dynamic_cape_" + System.currentTimeMillis());

                client.getTextureManager().register(newCapeId, dynamicTexture);

                if (this.currentActiveCapeId != null) {
                    client.getTextureManager().release(this.currentActiveCapeId);
                }

                this.currentActiveCapeId = newCapeId;
                this.currentActiveCapeName = fileName;

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Identifier getCurrentCapeId() {
        return this.currentActiveCapeId;
    }

    public List<String> getAvailableCapes() {
        List<String> capes = new ArrayList<>();
        try {
            Files.list(this.capesFolder)
                    .filter(path -> path.toString().endsWith(".png"))
                    .forEach(path -> capes.add(path.getFileName().toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return capes;
    }

    public Identifier getPreviewId(String fileName) {

        if (this.previewCache.containsKey(fileName)) {
            return this.previewCache.get(fileName);
        }

        // 2. Generate a permanent, safe ID for this specific file
        // Identifiers only allow lowercase letters, numbers, and underscores
        String safeName = fileName.toLowerCase().replace(".png", "").replaceAll("[^a-z0-9_.-]", "");
        Identifier previewId = Identifier.fromNamespaceAndPath("skin-changer", "preview_cape_" + safeName);

        // 3. Load the texture onto the GPU
        Path capeFile = capesFolder.resolve(fileName);

        try (InputStream inputStream = new FileInputStream(capeFile.toFile())) {
            NativeImage nativeImage = NativeImage.read(inputStream);

            // Your exact DynamicTexture logic
            DynamicTexture dynamicTexture = new DynamicTexture(() -> "custom_cape_preview", nativeImage);
            Minecraft.getInstance().getTextureManager().register(previewId, dynamicTexture);

            // 4. Save it to the cache so we never have to read this file again!
            this.previewCache.put(fileName, previewId);

            return previewId;

        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null if the file is broken or missing
        }
    }

    public String getCurrentActiveCapeName() {
        return this.currentActiveCapeName;
    }

    public void clearCape() {
        Minecraft.getInstance().execute(() -> {
            // Delete the texture from the GPU to prevent memory leaks
            if (this.currentActiveCapeId != null) {
                Minecraft.getInstance().getTextureManager().release(this.currentActiveCapeId);
                this.currentActiveCapeId = null;
            }
            // Set our tracker to a special string so the GUI knows what to highlight
            this.currentActiveCapeName = "Default Cape";
        });
    }
}
