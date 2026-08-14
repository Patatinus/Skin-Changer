package com.skinchanger.client.mixin;

import com.skinchanger.client.CustomCapeManager;
import com.skinchanger.client.CustomElytraManager;
import com.skinchanger.client.CustomSkinManager;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;

@Mixin(AbstractClientPlayer.class)
public class PlayerSkinMixin {

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void injectCustomSkin(CallbackInfoReturnable<PlayerSkin> cir) {

        PlayerSkin vanillaSkinObj = cir.getReturnValue();
        if (vanillaSkinObj == null) return;

        // Ask the manager if a dynamic skin is currently loaded
        Identifier activeDynamicSkin = CustomSkinManager.INSTANCE.getCurrentSkinId();
        Identifier activeDynamicCape = CustomCapeManager.INSTANCE.getCurrentCapeId();
        Identifier activeDynamicElytra = CustomElytraManager.INSTANCE.getCurrentElytraId();

        if (activeDynamicSkin == null && activeDynamicCape == null && activeDynamicElytra == null) {
            return;
        }

        ClientAsset.Texture customSkinTexture;
        if (activeDynamicSkin != null) {
            customSkinTexture = new ClientAsset.Texture() {
                @Override
                public Identifier id() { return activeDynamicSkin; }
                @Override
                public Identifier texturePath() { return activeDynamicSkin; }
            };
        } else {
            customSkinTexture = vanillaSkinObj.body();
        }

        PlayerModelType customModel;
        if (activeDynamicSkin != null) {
            customModel = CustomSkinManager.INSTANCE.isUsingSlimModel()
                    ? PlayerModelType.SLIM
                    : PlayerModelType.WIDE;
        } else {
            customModel = vanillaSkinObj.model();
        }

        ClientAsset.Texture customCapeTexture;
        if (activeDynamicCape != null) {
            customCapeTexture = new ClientAsset.Texture() {
                @Override
                public Identifier id() { return activeDynamicCape; }
                @Override
                public Identifier texturePath() { return activeDynamicCape; }
            };
        } else {
            customCapeTexture = vanillaSkinObj.cape();
        }

        ClientAsset.Texture customElytraTexture;
        if (CustomElytraManager.INSTANCE.shouldForceCapeElytra()) {
            customElytraTexture = null;
        } else if (activeDynamicElytra != null) {
            customElytraTexture = new ClientAsset.Texture() {
                @Override
                public Identifier id() { return activeDynamicElytra; }
                @Override
                public Identifier texturePath() { return activeDynamicElytra; }
            };

        } else {
            if (vanillaSkinObj.elytra() != null) {
                // The player has an official Mojang Elytra texture (very rare)
                customElytraTexture = vanillaSkinObj.elytra();

            } else if (vanillaSkinObj.cape() != null) {
                // The player has an official Mojang Cape (we use this for their vanilla wings)
                customElytraTexture = vanillaSkinObj.cape();

            } else {
                Identifier grayWings = Identifier.withDefaultNamespace("textures/entity/equipment/wings/elytra.png");

                customElytraTexture = new ClientAsset.Texture() {
                    @Override
                    public Identifier id() { return grayWings; }
                    @Override
                    public Identifier texturePath() { return grayWings; }
                };
            }
        }

        PlayerSkin customSkinObj = PlayerSkin.insecure(
                customSkinTexture,
                customCapeTexture,
                customElytraTexture,
                customModel
        );

        cir.setReturnValue(customSkinObj);
    }
}