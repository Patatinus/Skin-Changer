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

public class CustomSkinManager {

    // The single instance of our manager
    public static final CustomSkinManager INSTANCE = new CustomSkinManager();

    // The path to the .minecraft/custom_skins folder
    private final Path skinsFolder;

    // The dynamic Identifier that points to the currently active custom skin
    private Identifier currentActiveSkinId = null;
    private String currentActiveSkinName = null;
    private boolean useSlimModel = false;
    private final Map<String, Identifier> previewCache = new HashMap<>();

    private CustomSkinManager() {
        // Find the .minecraft folder and append /custom_skins
        this.skinsFolder = FabricLoader.getInstance().getGameDir().resolve("custom_skins");

        // If the folder doesn't exist yet, create it.
        try {
            if (!Files.exists(skinsFolder)) {
                Files.createDirectories(skinsFolder);
            }
        } catch (Exception e) {
            SkinChangerClient.LOGGER.error("Encountered an error!", e);
        }
    }

    /**
     * Loads a specific .png file from the custom_skins folder into the GPU.
     * @param fileName The name of the file (e.g., "knight.png")
     */
    public void loadAndSetSkin(String fileName) {
        Path skinFile = skinsFolder.resolve(fileName);

        if (!Files.exists(skinFile)) {
            String message = "Skin file not found: " + skinFile;
            SkinChangerClient.LOGGER.info(message);
            return;
        }

        // Use execute() to safely run this on the main graphics thread
        Minecraft.getInstance().execute(() -> {
            try (InputStream inputStream = new FileInputStream(skinFile.toFile())) {
                NativeImage nativeImage = NativeImage.read(inputStream);

                DynamicTexture dynamicTexture = new DynamicTexture(() -> "custom_player_skin", nativeImage);

                Minecraft client = Minecraft.getInstance();

                Identifier newSkinId = Identifier.fromNamespaceAndPath("skin-changer", "dynamic_skin_" + System.currentTimeMillis());

                client.getTextureManager().register(newSkinId, dynamicTexture);

                if (this.currentActiveSkinId != null) {
                    client.getTextureManager().release(this.currentActiveSkinId);
                }

                this.currentActiveSkinId = newSkinId;
                this.currentActiveSkinName = fileName;

            } catch (Exception e) {
                SkinChangerClient.LOGGER.error("Encountered an error!", e);
            }
        });
    }

    public Identifier getCurrentSkinId() {
        return this.currentActiveSkinId;
    }

    public List<String> getAvailableSkins() {
        List<String> skins = new ArrayList<>();
        try {
            Files.list(this.skinsFolder)
                    .filter(path -> path.toString().endsWith(".png"))
                    .forEach(path -> skins.add(path.getFileName().toString()));
        } catch (Exception e) {
            SkinChangerClient.LOGGER.error("Encountered an error!", e);
        }
        return skins;
    }

    public String getCurrentActiveSkinName() {
        return this.currentActiveSkinName;
    }

    public void clearSkin() {
        Minecraft.getInstance().execute(() -> {
            // Delete the texture from the GPU to prevent memory leaks
            if (this.currentActiveSkinId != null) {
                Minecraft.getInstance().getTextureManager().release(this.currentActiveSkinId);
                this.currentActiveSkinId = null;
            }
            // Set our tracker to a special string so the GUI knows what to highlight
            this.currentActiveSkinName = "Default Skin";
        });
    }

    public Identifier getPreviewId(String fileName) {
        // 1. If we already loaded this skin's preview, just return the cached ID!
        if (this.previewCache.containsKey(fileName)) {
            return this.previewCache.get(fileName);
        }

        // 2. Generate a permanent, safe ID for this specific file
        // Identifiers only allow lowercase letters, numbers, and underscores
        String safeName = fileName.toLowerCase().replace(".png", "").replaceAll("[^a-z0-9_.-]", "");
        Identifier previewId = Identifier.fromNamespaceAndPath("skin-changer", "preview_skin_" + safeName);

        // 3. Load the texture onto the GPU
        // (Assuming you have a variable like 'skinsDirectory' or 'skinsFolder' tracking your path)
        Path skinFile = skinsFolder.resolve(fileName);

        try (InputStream inputStream = new FileInputStream(skinFile.toFile())) {
            NativeImage nativeImage = NativeImage.read(inputStream);

            // Your exact DynamicTexture logic
            DynamicTexture dynamicTexture = new DynamicTexture(() -> "custom_skin_preview", nativeImage);
            Minecraft.getInstance().getTextureManager().register(previewId, dynamicTexture);

            // 4. Save it to the cache so we never have to read this file again!
            this.previewCache.put(fileName, previewId);

            return previewId;

        } catch (Exception e) {
            SkinChangerClient.LOGGER.error("Encountered an error!", e);
            return null; // Return null if the file is broken or missing
        }
    }

    public void setUseSlimModel(boolean slim) {
        this.useSlimModel = slim;
    }

    public boolean isUsingSlimModel() {
        return this.useSlimModel;
    }

    public Path getSkinsFolder() {
        return skinsFolder;
    }
}
