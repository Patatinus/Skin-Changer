package com.skinchanger.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkinChangerClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("skin-changer");
	@Override
	public void onInitializeClient() {

		SkinChangerConfig.load();

		KeyMapping.Category skinCategory = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath("skin-changer", "category")
		);

		KeyMapping openGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"Open Skin Menu",
				GLFW.GLFW_KEY_V,  // The default key
				skinCategory
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// consumeClick() checks if the button was just pressed down
			while (openGuiKey.consumeClick()) {
				Minecraft.getInstance().gui.setScreen(new SkinSelectionScreen());
			}
		});
	}
}