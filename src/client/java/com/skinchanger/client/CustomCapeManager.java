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

public class CustomCapeManager {

    // The single instance of our manager
    public static final CustomCapeManager INSTANCE = new CustomCapeManager();

    // The path to the .minecraft/custom_capes folder
    private final Path capesFolder;

    // The dynamic Identifier that points to the currently active custom cape
    private Identifier currentActiveCapeId = null;
    private String currentActiveCapeName = null;

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
