package com.skinchanger;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

public class SkinChanger implements ModInitializer {

	public static final String MOD_ID = "skin-changer";

	@Override
	public void onInitialize() {
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
