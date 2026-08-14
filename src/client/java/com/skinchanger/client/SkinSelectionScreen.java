package com.skinchanger.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SkinSelectionScreen extends Screen {

    private CustomScrollList skinList;
    private CustomScrollList capeList;
    private CustomScrollList elytraList;

    public SkinSelectionScreen() {
        super(Component.literal("Customizer"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        // Calculate the sizes for a perfect 3-column layout
        int listWidth = this.width / 3 - 10;
        int listHeight = this.height - 60;
        int topY = 40;
        int itemHeight = 24;

        // --- 1. SKINS COLUMN (Left) ---
        this.skinList = new CustomScrollList(this.minecraft, listWidth, listHeight, topY, itemHeight);
        this.skinList.setX(5); // Anchor to the far left

        String activeSkin = CustomSkinManager.INSTANCE.getCurrentActiveSkinName();
        // Fallback in case it is null on the first launch
        if (activeSkin == null) activeSkin = "Default Skin";

        // 1. ADD THE DEFAULT OPTION FIRST
        CustomScrollList.FileEntry defaultSkinEntry = this.skinList.addFile("Default Skin", null, null ,() -> {
            CustomSkinManager.INSTANCE.clearSkin(); // Run our new clear method!
        });

        // Highlight it if they don't have a custom skin equipped
        if ("Default Skin".equals(activeSkin)) {
            this.skinList.setSelected(defaultSkinEntry);
        }

        for (String skin : CustomSkinManager.INSTANCE.getAvailableSkins()) {

            Identifier previewId = CustomSkinManager.INSTANCE.getPreviewId(skin);

            CustomScrollList.FileEntry entry = this.skinList.addFile(skin, previewId, IconType.SKIN, () -> {
                CustomSkinManager.INSTANCE.loadAndSetSkin(skin);
            });

            if (skin.equals(activeSkin)) {
                this.skinList.setSelected(entry);
            }
            if (skin.equals(CustomSkinManager.INSTANCE.getCurrentActiveSkinName())) {
                this.skinList.setSelected(entry);
            }
        }
        this.addRenderableWidget(this.skinList);

        // --- 2. CAPES COLUMN (Middle) ---

        this.capeList = new CustomScrollList(this.minecraft, listWidth, listHeight, topY, itemHeight);
        this.capeList.setX(this.width / 3 + 5);
        String activeCape = CustomCapeManager.INSTANCE.getCurrentActiveCapeName();
        // Fallback in case it is null on the first launch
        if (activeCape == null) activeCape = "Default Cape";

        // 1. ADD THE DEFAULT OPTION FIRST
        CustomScrollList.FileEntry defaultCapeEntry = this.capeList.addFile("Default Cape", null, null,() -> {
            CustomCapeManager.INSTANCE.clearCape(); // Run our new clear method!
        });

        if ("Default Cape".equals(activeCape)) {
            this.capeList.setSelected(defaultCapeEntry);
        }
        for (String cape : CustomCapeManager.INSTANCE.getAvailableCapes()) {

            Identifier previewId = CustomCapeManager.INSTANCE.getPreviewId(cape);

            CustomScrollList.FileEntry entry = this.capeList.addFile(cape, previewId, IconType.CAPE, () -> {
                CustomCapeManager.INSTANCE.loadAndSetCape(cape);
            });
            if (cape.equals(CustomCapeManager.INSTANCE.getCurrentActiveCapeName())) {
                this.capeList.setSelected(entry);
            }
        }
        this.addRenderableWidget(this.capeList);

        // --- 3. ELYTRAS COLUMN (Right) ---

        this.elytraList = new CustomScrollList(this.minecraft, listWidth, listHeight, topY, itemHeight);
        this.elytraList.setX(this.width * 2 / 3 + 5);

        String activeElytra = CustomElytraManager.INSTANCE.getCurrentActiveElytraName();
        // Fallback in case it is null on the first launch
        if (activeElytra == null) activeElytra = "Default Elytra";

        CustomScrollList.FileEntry defaultElytraEntry = this.elytraList.addFile("Default Elytra", null, null, () -> {
            CustomElytraManager.INSTANCE.setForceCapeElytra(false);
            CustomElytraManager.INSTANCE.clearElytra();
        });



        this.elytraList.addFile("Custom Cape's Elytra", null, null, () -> {
            CustomElytraManager.INSTANCE.setForceCapeElytra(true);
        });

        if ("Default Elytra".equals(activeElytra)) {
            this.elytraList.setSelected(defaultElytraEntry);
        }
        for (String elytra : CustomElytraManager.INSTANCE.getAvailableElytras()) {

            Identifier previewId = CustomElytraManager.INSTANCE.getPreviewId(elytra);

            CustomScrollList.FileEntry entry = this.elytraList.addFile(elytra, previewId, IconType.ELYTRA, () -> {
                CustomElytraManager.INSTANCE.loadAndSetElytra(elytra);
                CustomElytraManager.INSTANCE.setForceCapeElytra(false);
            });
            if (elytra.equals(CustomElytraManager.INSTANCE.getCurrentActiveElytraName())) {
                this.elytraList.setSelected(entry);
            }
        }
        this.addRenderableWidget(this.elytraList);

        // --- Button for Model Type ---

        Button modelButton = Button.builder(
                        Component.literal("Model: " + (CustomSkinManager.INSTANCE.isUsingSlimModel() ? "Slim" : "Wide")),
                        button -> {
                            // Flip the boolean
                            boolean isNowSlim = !CustomSkinManager.INSTANCE.isUsingSlimModel();
                            CustomSkinManager.INSTANCE.setUseSlimModel(isNowSlim);

                            // Update the text on the button so the player sees it change
                            button.setMessage(Component.literal("Model: " + (isNowSlim ? "Slim" : "Wide")));
                        }
                )
                // Position it at the bottom center of the screen
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20)
                .build();

        this.addRenderableWidget(modelButton);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        try {
            graphics.centeredText(this.font, "Skins", this.width / 6, 25, -1);
            graphics.centeredText(this.font, "Capes", this.width / 2, 25, -1);
            graphics.centeredText(this.font, "Elytras", this.width * 5 / 6, 25, -1);
        } catch (Exception e) {}

        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    // =====================================================================
    // INTERNAL CLASSES FOR THE SCROLLING LIST
    // =====================================================================

    public class CustomScrollList extends ObjectSelectionList<CustomScrollList.FileEntry> {
        public CustomScrollList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        public FileEntry addFile(String fileName, Identifier previewId, IconType iconType, Runnable onClick) {
            FileEntry newEntry = this.new FileEntry(fileName, previewId, iconType, onClick);
            super.addEntry(newEntry);
            return newEntry;
        }

        @Override
        protected int scrollBarX() {
            // this.getX() gets the starting position of the column, then we add the width!
            return this.getX() + this.width - 6;
        }

        @Override
        public int getRowWidth() {
            return this.width - 16;
        }

        // We expose this so we can add buttons to the list from the init() method
        public class FileEntry extends ObjectSelectionList.Entry<FileEntry> {
            private final String fileName;
            private final Identifier previewId;
            private final IconType iconType; // Track what type of file this is!
            private final Runnable onClick;

            public FileEntry(String fileName, Identifier previewId, IconType iconType, Runnable onClick) {
                this.fileName = fileName;
                this.previewId = previewId;
                this.iconType = iconType;
                this.onClick = onClick;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovered, float partialTick) {
                int textOffset = 5;

                if (this.previewId != null) {
                    switch (this.iconType) {
                        case SKIN -> {
                            // Draw Skin Face (16x16 Dest, 8x8 Src, 64x64 Tex)
                            graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, this.previewId,
                                    this.getX() + 2, this.getY() + 2, 8.0F, 8.0F, 16, 16, 8, 8, 64, 64);
                            // Draw Hat Layer
                            graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, this.previewId,
                                    this.getX() + 2, this.getY() + 2, 40.0F, 8.0F, 16, 16, 8, 8, 64, 64);
                            textOffset = 22;
                        }
                        case CAPE -> {
                            // Draw Cape Back (10x16 Dest, 10x16 Src, 64x32 Tex)
                            graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, this.previewId,
                                    this.getX() + 5, this.getY() + 4, 1.0F, 1.0F, 10, 16, 10, 16, 64, 32);
                            textOffset = 20;
                        }
                        case ELYTRA -> {
                            // Draw Elytra Left Wing (10x20 Dest, 10x20 Src, 64x32 Tex)
                            graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, this.previewId,
                                    this.getX() + 5, this.getY() + 2, 36.0F, 2.0F, 10, 20, 10, 20, 64, 32);
                            textOffset = 20;
                        }
                    }
                }

                // Draw the text, dynamically shifted over to make room for whichever icon we just drew
                graphics.text(SkinSelectionScreen.this.font, this.fileName, this.getX() + textOffset, this.getY() + 6, -1, false);
            }

            @Override
            public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {

                CustomScrollList.this.setSelected(this); // Highlights the entry
                this.onClick.run();                      // Loads the skin
                return true;
            }

            @Override
            public Component getNarration() {
                return Component.literal(this.fileName);
            }

        }

    }

    public enum IconType {
        SKIN, CAPE, ELYTRA
    }
}


