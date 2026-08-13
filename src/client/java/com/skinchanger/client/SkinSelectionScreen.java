package com.skinchanger.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SkinSelectionScreen extends Screen {

    public SkinSelectionScreen() {
        // The title of the window
        super(Component.literal("Select Custom Skin"));
    }

    @Override
    protected void init() {
        List<String> skins = CustomSkinManager.INSTANCE.getAvailableSkins();

        // Start placing buttons 40 pixels down from the top of the screen
        int yOffset = 40;

        for (String skinName : skins) {

            Button button = Button.builder(Component.literal(skinName), b -> {
                CustomSkinManager.INSTANCE.loadAndSetSkin(skinName);
                this.onClose();
            })
            .bounds(this.width / 2 - 100, yOffset, 200, 20)
            .build();

            this.addRenderableWidget(button);

            yOffset += 24; // Move down 24 pixels for the next button
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

        try {
            graphics.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        } catch (Exception e) {
            // Ignore if the method name changed in this mapping
        }

        super.extractRenderState(graphics, mouseX, mouseY, a);
    }
}
