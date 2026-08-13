package com.skinchanger.client.mixin;

import com.skinchanger.client.CustomSkinManager;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.ClientAsset.ResourceTexture;
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

    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
    private void injectCustomSkin(CallbackInfoReturnable<PlayerSkin> cir) {

        // Ask the manager if a dynamic skin is currently loaded
        Identifier activeDynamicSkin = CustomSkinManager.INSTANCE.getCurrentSkinId();

        // If it's null, the player hasn't selected a skin. Let the game load the normal one.
        if (activeDynamicSkin == null) {
            return;
        }

        ClientAsset.Texture customTexture = new ClientAsset.Texture() {
            @Override
            public Identifier id() {
                return activeDynamicSkin; // The general asset ID
            }

            @Override
            public Identifier texturePath() {
                return activeDynamicSkin; // The actual path the renderer uses! (No .png added)
            }
        };

        PlayerSkin customSkinObj = PlayerSkin.insecure(
                customTexture,
                null,
                null,
                PlayerModelType.SLIM
        );

        cir.setReturnValue(customSkinObj);
    }
}