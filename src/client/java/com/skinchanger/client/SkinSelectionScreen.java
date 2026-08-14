package com.skinchanger.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

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
        // TODO: Populate with getAvailableCapes() later!
        this.addRenderableWidget(this.capeList);

        // --- 3. ELYTRAS COLUMN (Right) ---
        this.elytraList = new CustomScrollList(this.minecraft, listWidth, listHeight, topY, itemHeight);
        this.elytraList.setX(this.width * 2 / 3 + 5);
        // TODO: Populate with getAvailableElytras() later!
        this.addRenderableWidget(this.elytraList);
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
