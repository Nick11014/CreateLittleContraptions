package com.createlittlecontraptions.mixins.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.createlittlecontraptions.CreateLittleContraptions;
import com.simibubi.create.AllMovementBehaviours;

@Mixin(value = AllMovementBehaviours.class, remap = false)
public class AllMovementBehavioursMixin {
    
    @Inject(method = "registerDefaults", at = @At("TAIL"))
    private static void onRegisterDefaults(CallbackInfo ci) {
        CreateLittleContraptions.LOGGER.info("AllMovementBehavioursMixin: Registering LittleTiles MovementBehaviour");
        try {
            CreateLittleContraptions.registerLittleTilesMovementBehaviour();
            CreateLittleContraptions.LOGGER.info("AllMovementBehavioursMixin: Successfully registered LittleTiles MovementBehaviour");
        } catch (Exception e) {
            CreateLittleContraptions.LOGGER.error("AllMovementBehavioursMixin: Failed to register LittleTiles MovementBehaviour", e);
        }
    }
}
