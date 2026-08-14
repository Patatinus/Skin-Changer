package com.skinchanger.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

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
        CustomScrollList.FileEntry defaultSkinEntry = this.skinList.addFile("Default Skin", () -> {
            CustomSkinManager.INSTANCE.clearSkin(); // Run our new clear method!
        });

        // Highlight it if they don't have a custom skin equipped
        if ("Default Skin".equals(activeSkin)) {
            this.skinList.setSelected(defaultSkinEntry);
        }

        for (String skin : CustomSkinManager.INSTANCE.getAvailableSkins()) {

            CustomScrollList.FileEntry entry = this.skinList.addFile(skin, () -> {
                CustomSkinManager.INSTANCE.loadAndSetSkin(skin);
            });
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
        CustomScrollList.FileEntry defaultCapeEntry = this.capeList.addFile("Default Cape", () -> {
            CustomCapeManager.INSTANCE.clearCape(); // Run our new clear method!
        });

        if ("Default Cape".equals(activeCape)) {
            this.capeList.setSelected(defaultCapeEntry);
        }
        for (String cape : CustomCapeManager.INSTANCE.getAvailableCapes()) {

            CustomScrollList.FileEntry entry = this.capeList.addFile(cape, () -> {
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

        CustomScrollList.FileEntry defaultElytraEntry = this.elytraList.addFile("Default Elytra", () -> {
            CustomElytraManager.INSTANCE.setForceCapeElytra(false);
            CustomElytraManager.INSTANCE.clearElytra();
        });

        this.elytraList.addFile("Custom Cape's Elytra", () -> {
            CustomElytraManager.INSTANCE.setForceCapeElytra(true);
        });

        if ("Default Elytra".equals(activeElytra)) {
            this.elytraList.setSelected(defaultElytraEntry);
        }
        for (String elytra : CustomElytraManager.INSTANCE.getAvailableElytras()) {

            CustomScrollList.FileEntry entry = this.elytraList.addFile(elytra, () -> {
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
        public FileEntry addFile(String fileName, Runnable onClick) {
            FileEntry newEntry = this.new FileEntry(fileName, onClick);
            super.addEntry(newEntry);
            return newEntry;
        }

        // Represents a single row inside the scrollable list
        public class FileEntry extends ObjectSelectionList.Entry<FileEntry> {
            private final String fileName;
            private final Runnable onClick;

            public FileEntry(String fileName, Runnable onClick) {
                this.fileName = fileName;
                this.onClick = onClick;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int top, int left, boolean isHovered, float partialTick) {
                graphics.text(
                        SkinSelectionScreen.this.font,
                        this.fileName,
                        this.getX() + 5,
                        this.getY() + 4,
                        -1,
                        false
                );
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
}
