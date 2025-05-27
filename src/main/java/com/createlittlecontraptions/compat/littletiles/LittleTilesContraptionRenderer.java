package com.createlittlecontraptions.compat.littletiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

// LittleTiles imports - Based on Gemini analysis
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.structure.LittleStructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LittleTilesContraptionRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(LittleTilesContraptionRenderer.class);
    private static boolean initialized = false;

    /**
     * Initialize the LittleTiles contraption renderer.
     * Called during mod initialization.
     */
    public static void initialize() {
        LOGGER.info("Initializing LittleTiles contraption renderer...");
        initialized = true;
        LOGGER.info("LittleTiles contraption renderer initialized successfully.");
    }

    /**
     * Check if the renderer has been initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }    /**
     * Legacy wrapper method for mixin compatibility.
     * Simplified approach to avoid constructor issues.
     */
    public static void renderLittleTileBEInContraption(
            PoseStack poseStack, 
            MultiBufferSource bufferSource,
            Level realLevel,
            Level renderLevel,
            BETiles blockEntity,
            float partialTicks,
            boolean lightTransform) {
        
        LOGGER.debug("🔄 [CLC Renderer] Legacy wrapper called for BE at {}", blockEntity.getBlockPos());
        
        try {
            // For now, we'll do a simplified direct rendering approach
            // This avoids the constructor issues while still providing some rendering capability
            
            BlockPos localPos = blockEntity.getBlockPos();
            LOGGER.info("🎨 [CLC Renderer] Legacy wrapper - attempting direct LittleTiles rendering for {}", localPos);            // Get the NBT data from the existing blockEntity
            CompoundTag nbt = new CompoundTag();
            // Use a safer way to get NBT data - try different approaches
            try {
                // Try the standard save method
                nbt = blockEntity.saveWithFullMetadata(realLevel.registryAccess());
            } catch (Exception e) {
                LOGGER.debug("🔄 [CLC Renderer] saveWithFullMetadata failed, trying saveWithoutMetadata: {}", e.getMessage());
                try {
                    nbt = blockEntity.saveWithoutMetadata(realLevel.registryAccess());
                } catch (Exception e2) {
                    LOGGER.debug("🔄 [CLC Renderer] saveWithoutMetadata failed, using fallback: {}", e2.getMessage());
                    // Create minimal NBT data as fallback
                    nbt.putString("_marker", "clc_legacy_render");
                    nbt.putString("id", "littletiles:tiles"); // Basic tile marker
                }
            }
            
            if (nbt.isEmpty() || nbt.size() <= 1) { // Account for the marker
                LOGGER.warn("⚠️ [CLC Renderer] Legacy wrapper - NBT is empty for {}", localPos);
                return;
            }
            
            // Try to create a virtual BE and render its structures directly
            BETiles virtualBE = new BETiles(localPos, blockEntity.getBlockState());
            virtualBE.setLevel(realLevel); // Use the real level as fallback
            virtualBE.handleUpdate(nbt, false);
            
            if (virtualBE.isEmpty()) {
                LOGGER.warn("⚠️ [CLC Renderer] Legacy wrapper - virtualBE is empty after loading NBT");
                return;
            }
            
            // Get renderable structures
            var structuresToRender = virtualBE.rendering();
            boolean hasStructures = false;
            int structureCount = 0;
            
            // Count structures and check if any exist
            for (LittleStructure structure : structuresToRender) {
                if (structure != null) {
                    hasStructures = true;
                    structureCount++;
                }
            }
            
            if (!hasStructures) {
                LOGGER.debug("📭 [CLC Renderer] Legacy wrapper - no structures to render");
                return;
            }
            
            LOGGER.info("✅ [CLC Renderer] Legacy wrapper - found {} structures to render", structureCount);
            
            // Render each structure directly
            for (LittleStructure structure : structuresToRender) {
                if (structure == null) continue;
                
                poseStack.pushPose();
                try {
                    structure.renderTick(poseStack, bufferSource, localPos, partialTicks);
                    LOGGER.debug("🎨 [CLC Renderer] Legacy wrapper - rendered structure: {}", structure.getClass().getSimpleName());
                } catch (Exception e) {
                    LOGGER.error("❌ [CLC Renderer] Legacy wrapper - error rendering structure: {}", e.getMessage(), e);
                } finally {
                    poseStack.popPose();
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("❌ [CLC Renderer] Error in legacy wrapper for {}: {}", 
                        blockEntity.getBlockPos(), e.getMessage(), e);
        }
    }    /**
     * Render a LittleTiles BlockEntity in a contraption using the main movement behavior approach.
     * This method follows Gemini's analysis of LittleTiles rendering architecture.
     * Updated to use loadAdditional() instead of handleUpdate() to avoid VirtualRenderWorld limitations.
     */
    public static void renderMovementBehaviourTile(MovementContext context, VirtualRenderWorld renderWorld,
                                                  ContraptionMatrices matrices, MultiBufferSource bufferSource) {

        final BlockPos localPos = context.localPos;
        final BlockState blockState = context.state; // BlockState do BlockTile (container)
        final CompoundTag nbt = context.blockEntityData;

        LOGGER.info("🎨 [CLC Renderer] Iniciando renderMovementBehaviourTile para: {} com NBT (existe? {})", localPos, (nbt != null && !nbt.isEmpty()));

        if (nbt == null || nbt.isEmpty()) {
            LOGGER.warn("⚠️ [CLC Renderer] NBT é nulo ou vazio para {}. Abortando renderização.", localPos);
            return;
        }

        PoseStack poseStack = matrices.getModelViewProjection();

        try {
            // 1. Criar instância de BETiles
            BETiles virtualBE = new BETiles(localPos, blockState); 
            
            // 2. Definir o Level. VirtualRenderWorld PRECISA se comportar como isClientSide = true
            // para que o BERenderManager seja criado em BETiles.initClient().
            if (renderWorld == null) { // Fallback, mas idealmente renderWorld nunca é null aqui
                LOGGER.error("❌ [CLC Renderer] VirtualRenderWorld é NULO para {}. Abortando.", localPos);
                return;
            }            virtualBE.setLevel(renderWorld); 
            LOGGER.debug("📦 [CLC Renderer] Level definido para BETiles virtual");

            // 3. Carregar dados do NBT - ESTRATÉGIA SEGURA
            // Como loadAdditional() e tiles não são acessíveis, usamos handleUpdate() com tratamento robusto
            LOGGER.debug("📦 [CLC Renderer] Tentando carregar NBT para {}", localPos);
            
            boolean dataLoaded = false;
            try {
                // Tentar handleUpdate com isClient=false para minimizar operações de markDirty
                virtualBE.handleUpdate(nbt, false);
                dataLoaded = true;
                LOGGER.info("✅ [CLC Renderer] virtualBE.handleUpdate(nbt, false) completado para {}", localPos);
                
            } catch (UnsupportedOperationException uoe) {
                LOGGER.warn("⚠️ [CLC Renderer] handleUpdate falhou com UnsupportedOperationException (provavelmente VirtualRenderWorld.getChunk): {}", uoe.getMessage());
                // Se falhar, não podemos carregar os dados corretamente no VirtualRenderWorld atual
                // Isso indica que precisamos de uma abordagem diferente para o VirtualRenderWorld
                dataLoaded = false;
                
            } catch (Exception e) {
                LOGGER.error("❌ [CLC Renderer] Falha ao carregar NBT para {}: {}", localPos, e.getMessage());
                dataLoaded = false;
            }
            
            if (!dataLoaded) {
                LOGGER.warn("⚠️ [CLC Renderer] Não foi possível carregar dados NBT para {}. Abortando renderização.", localPos);
                return; // Sem dados carregados, não podemos renderizar
            }

            // 4. Chamar onLoad para finalizar a inicialização do lado do cliente (inclui render.onLoad())
            // Isto é importante para que o BERenderManager processe as tiles carregadas.
            try {
                LOGGER.debug("📦 [CLC Renderer] Chamando virtualBE.onLoad() para {}", localPos);
                virtualBE.onLoad();
                LOGGER.info("✅ [CLC Renderer] virtualBE.onLoad() completado para {}", localPos);
            } catch (UnsupportedOperationException uoe) {
                LOGGER.warn("⚠️ [CLC Renderer] onLoad() falhou com UnsupportedOperationException: {}. Continuando sem onLoad.", uoe.getMessage());
                // Continuar sem onLoad - o BERenderManager pode ter sido inicializado de outra forma
            } catch (Exception e) {
                LOGGER.warn("⚠️ [CLC Renderer] onLoad() falhou: {}. Continuando sem onLoad.", e.getMessage());
            }

            // VERIFICAÇÃO: BERenderManager (virtualBE.render) está inicializado?
            if (virtualBE.isClient() && virtualBE.render == null) {
                LOGGER.warn("⚠️ [CLC Renderer] BERenderManager (virtualBE.render) é NULO após carregamento para {}. A renderização pode falhar.", localPos);
            } else if (virtualBE.isClient()) {
                LOGGER.info("✅ [CLC Renderer] BERenderManager (virtualBE.render) está PRESENTE para {}", localPos);
            }// 5. Obter estruturas renderizáveis e renderizá-las
            Iterable<LittleStructure> structuresToRender = virtualBE.rendering(); // Deve ser a lista correta agora
            
            boolean renderedSomething = false;
            if (structuresToRender != null) {
                for (LittleStructure structure : structuresToRender) {
                    if (structure == null) continue;

                    LOGGER.debug("➡️ [CLC Renderer] Tentando renderizar estrutura: {} para BE em {}", structure.getClass().getSimpleName(), localPos);
                    
                    poseStack.pushPose();
                    // A PoseStack de matrices.getModelViewProjection() já deve estar correta para o espaço da contraption.
                    // O parâmetro 'pos' para renderTick é a posição do BETiles, que é 'localPos' no contexto da contraption.
                    
                    float partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);

                    structure.renderTick(poseStack, bufferSource, localPos, partialTicks);
                    
                    poseStack.popPose();
                    renderedSomething = true;
                    LOGGER.debug("✅ [CLC Renderer] structure.renderTick() chamado para {} em {}", structure.getClass().getSimpleName(), localPos);
                }
            } else {
                 LOGGER.warn("⚠️ [CLC Renderer] virtualBE.rendering() retornou null para {}", localPos);
            }


            if (!renderedSomething) {
                LOGGER.warn("⚠️ [CLC Renderer] Nenhuma estrutura com TICK_RENDERING encontrada ou renderizada para {}. Verifique se há tiles visíveis na estrutura.", localPos);
            }

            LOGGER.info("🎉 [CLC Renderer] renderMovementBehaviourTile finalizado para: {}", localPos);

        } catch (UnsupportedOperationException uoe) {
            // Se ainda recebermos o erro de getChunk, precisamos isolar o que o está causando
            LOGGER.error("❌ CRÍTICO: UnsupportedOperationException (provavelmente getChunk) em renderMovementBehaviourTile para {}. Causa: {}", localPos, uoe.getMessage(), uoe);
            // Investigar qual parte (loadAdditional, onLoad, rendering) está acionando isso.
        } 
        catch (Exception e) {
            LOGGER.error("❌ [CLC Renderer] Exceção crítica em renderMovementBehaviourTile para {}: {}", localPos, e.getMessage(), e);
        }
    }
}
