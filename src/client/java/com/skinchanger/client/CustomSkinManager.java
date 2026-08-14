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
import java.util.List;

public class CustomSkinManager {

    // The single instance of our manager
    public static final CustomSkinManager INSTANCE = new CustomSkinManager();

    // The path to the .minecraft/custom_skins folder
    private final Path skinsFolder;

    // The dynamic Identifier that points to the currently active custom skin
    private Identifier currentActiveSkinId = null;
    private String currentActiveSkinName = null;

    private CustomSkinManager() {
        // Find the .minecraft folder and append /custom_skins
        this.skinsFolder = FabricLoader.getInstance().getGameDir().resolve("custom_skins");

        // If the folder doesn't exist yet, create it.
        try {
            if (!Files.exists(skinsFolder)) {
                Files.createDirectories(skinsFolder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a specific .png file from the custom_skins folder into the GPU.
     * @param fileName The name of the file (e.g., "knight.png")
     */
    public void loadAndSetSkin(String fileName) {
        Path skinFile = skinsFolder.resolve(fileName);

        if (!Files.exists(skinFile)) {
            System.out.println("Skin file not found: " + skinFile);
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
                e.printStackTrace();
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
            e.printStackTrace();
        }
        return skins;
    }

    public String getCurrentActiveSkinName() {
        return this.currentActiveSkinName;
    }
}
