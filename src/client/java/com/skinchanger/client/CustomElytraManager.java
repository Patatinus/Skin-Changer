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

public class CustomElytraManager {

    public static final CustomElytraManager INSTANCE = new CustomElytraManager();

    private final Path elytrasFolder;

    private Identifier currentActiveElytraId = null;
    private String currentActiveElytraName = null;
    private boolean forceCapeElytra = true;

    private CustomElytraManager() {
        this.elytrasFolder = FabricLoader.getInstance().getGameDir().resolve("custom_elytras");

        // If the folder doesn't exist yet, create it.
        try {
            if (!Files.exists(elytrasFolder)) {
                Files.createDirectories(elytrasFolder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadAndSetElytra(String fileName) {
        Path elytraFile = elytrasFolder.resolve(fileName);

        if (!Files.exists(elytraFile)) {
            System.out.println("Elytra file not found: " + elytraFile);
            return;
        }

        // Use execute() to safely run this on the main graphics thread
        Minecraft.getInstance().execute(() -> {
            try (InputStream inputStream = new FileInputStream(elytraFile.toFile())) {
                NativeImage nativeImage = NativeImage.read(inputStream);

                DynamicTexture dynamicTexture = new DynamicTexture(() -> "custom_player_elytra", nativeImage);

                Minecraft client = Minecraft.getInstance();

                Identifier newElytraId = Identifier.fromNamespaceAndPath("skin-changer", "dynamic_elytra_" + System.currentTimeMillis());

                client.getTextureManager().register(newElytraId, dynamicTexture);

                if (this.currentActiveElytraId != null) {
                    client.getTextureManager().release(this.currentActiveElytraId);
                }

                this.currentActiveElytraId = newElytraId;
                this.currentActiveElytraName = fileName;

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Identifier getCurrentElytraId() {
        return this.currentActiveElytraId;
    }

    public List<String> getAvailableElytras() {
        List<String> elytras = new ArrayList<>();
        try {
            Files.list(this.elytrasFolder)
                    .filter(path -> path.toString().endsWith(".png"))
                    .forEach(path -> elytras.add(path.getFileName().toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return elytras;
    }

    public String getCurrentActiveElytraName() {
        return this.currentActiveElytraName;
    }

    public void clearElytra() {
        Minecraft.getInstance().execute(() -> {
            // Delete the texture from the GPU to prevent memory leaks
            if (this.currentActiveElytraId != null) {
                Minecraft.getInstance().getTextureManager().release(this.currentActiveElytraId);
                this.currentActiveElytraId = null;
            }
            // Set our tracker to a special string so the GUI knows what to highlight
            this.currentActiveElytraName = "Default Elytra";
        });
    }

    public void setForceCapeElytra(boolean force) {
        this.forceCapeElytra = force;
    }

    public boolean shouldForceCapeElytra() {
        return this.forceCapeElytra;
    }
}

