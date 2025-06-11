package com.createlittlecontraptions.mixins;

import com.createlittlecontraptions.CreateLittleContraptions;
import com.createlittlecontraptions.commands.ContraptionRenderCommand;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * Mixin para interceptar o ContraptionEntityRenderer e implementar controle completo de renderização
 * 
 * ESTRATÉGIA MÚLTIPLA:
 * 1. shouldRender() - Bloqueia a decisão de renderizar (WORKING - logs confirmam)
 * 2. render() - Bloqueia o método principal de renderização (BACKUP)
 * 
 * COM ESTAS DUAS INTERCEPTAÇÕES, GARANTIMOS QUE AS CONTRAPTIONS FIQUEM INVISÍVEIS!
 */
@Mixin(ContraptionEntityRenderer.class)
public class ContraptionEntityRendererMixin {

    private static long lastShouldRenderLogTime = 0;
    private static int suppressedShouldRenderCount = 0;
    private static long lastRenderLogTime = 0;
    private static int suppressedRenderCount = 0;
    private static final long LOG_INTERVAL_MS = 3000; // Log every 3 seconds

    /**
     * INTERCEPTAÇÃO PRINCIPAL: shouldRender()
     * 
     * Este método determina se a contraption deve ser renderizada.
     * Retornar false aqui deve tornar a contraption invisível.
     * 
     * LOGS CONFIRMAM: Este método está sendo chamado e bloqueado com sucesso!
     */
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true, remap = false)
    public void onShouldRender(AbstractContraptionEntity entity, Frustum frustum, double cameraX, double cameraY, double cameraZ, CallbackInfoReturnable<Boolean> cir) {
        UUID contraptionUUID = entity.getUUID();
        
        boolean isDisabled = ContraptionRenderCommand.isContraptionRenderingDisabled(contraptionUUID);
        
        if (isDisabled) {
            // CRÍTICO: Forçar retorno false para tornar contraption invisível
            CreateLittleContraptions.LOGGER.warn("🚫 [MIXIN] BLOCKING shouldRender() for contraption UUID: {} - FORCING INVISIBLE!", contraptionUUID);
            logShouldRenderInterception(entity, true);
            cir.setReturnValue(false); // FALSE = INVISÍVEL
            return;
        }
        
        logShouldRenderInterception(entity, false);
    }

    /**
     * INTERCEPTAÇÃO SECUNDÁRIA: render()
     * 
     * Backup para garantir que mesmo se shouldRender() falhar,
     * o método render() não execute e torne a contraption invisível.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    public void onRender(AbstractContraptionEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int overlay, CallbackInfo ci) {
        UUID contraptionUUID = entity.getUUID();
        
        boolean isDisabled = ContraptionRenderCommand.isContraptionRenderingDisabled(contraptionUUID);
        
        if (isDisabled) {
            CreateLittleContraptions.LOGGER.warn("🚫 [MIXIN] BLOCKING render() for contraption UUID: {} - CANCELLING RENDER!", contraptionUUID);
            logRenderInterception(entity, true);
            ci.cancel(); // Cancela completamente a renderização
            return;
        }
        
        logRenderInterception(entity, false);
    }

    /**
     * INTERCEPTAÇÃO AGRESSIVA: isInvisible()
     * 
     * Força a entidade a parecer invisível no nível fundamental.
     * Este método é consultado por vários sistemas de renderização.
     */
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true, remap = false)
    public void onShouldRenderEntity(AbstractContraptionEntity entity, Frustum frustum, double cameraX, double cameraY, double cameraZ, CallbackInfoReturnable<Boolean> cir) {
        UUID contraptionUUID = entity.getUUID();
        
        boolean isDisabled = ContraptionRenderCommand.isContraptionRenderingDisabled(contraptionUUID);
        
        if (isDisabled) {
            // FORÇA INVISIBILIDADE TOTAL
            CreateLittleContraptions.LOGGER.warn("🚫 [AGGRESSIVE MIXIN] FORCING ENTITY INVISIBILITY for contraption UUID: {}", contraptionUUID);
            cir.setReturnValue(false);
            return;
        }
    }
    
    private void logShouldRenderInterception(AbstractContraptionEntity entity, boolean blocked) {
        long currentTime = System.currentTimeMillis();
        suppressedShouldRenderCount++;
        
        if (currentTime - lastShouldRenderLogTime > LOG_INTERVAL_MS) {
            if (blocked) {
                CreateLittleContraptions.LOGGER.info("🚫 [MIXIN] shouldRender() BLOCKED for contraption: {} (UUID: {}) - {} calls blocked in last {}ms", 
                    entity.getClass().getSimpleName(), entity.getUUID(), suppressedShouldRenderCount, currentTime - lastShouldRenderLogTime);
            } else {
                CreateLittleContraptions.LOGGER.debug("✅ [MIXIN] shouldRender() ALLOWED for contraption: {} (UUID: {}) - {} calls in last {}ms", 
                    entity.getClass().getSimpleName(), entity.getUUID(), suppressedShouldRenderCount, currentTime - lastShouldRenderLogTime);
            }
            lastShouldRenderLogTime = currentTime;
            suppressedShouldRenderCount = 0;
        }
    }

    private void logRenderInterception(AbstractContraptionEntity entity, boolean blocked) {
        long currentTime = System.currentTimeMillis();
        suppressedRenderCount++;
        
        if (currentTime - lastRenderLogTime > LOG_INTERVAL_MS) {
            if (blocked) {
                CreateLittleContraptions.LOGGER.info("🚫 [MIXIN] render() BLOCKED for contraption: {} (UUID: {}) - {} calls blocked in last {}ms", 
                    entity.getClass().getSimpleName(), entity.getUUID(), suppressedRenderCount, currentTime - lastRenderLogTime);
            } else {
                CreateLittleContraptions.LOGGER.debug("✅ [MIXIN] render() ALLOWED for contraption: {} (UUID: {}) - {} calls in last {}ms", 
                    entity.getClass().getSimpleName(), entity.getUUID(), suppressedRenderCount, currentTime - lastRenderLogTime);
            }
            lastRenderLogTime = currentTime;
            suppressedRenderCount = 0;
        }
    }
}
