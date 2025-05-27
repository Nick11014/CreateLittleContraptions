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

            // 3. Carregar dados do NBT USANDO handleUpdate com isClient = true
            // A hipótese é que isClient=true pode evitar o caminho que chama markDirty()
            LOGGER.debug("📦 [CLC Renderer] Chamando virtualBE.handleUpdate(nbt, true) para {}", localPos);
            try {
                virtualBE.handleUpdate(nbt, true); // Passando true para isClient
                LOGGER.info("✅ [CLC Renderer] virtualBE.handleUpdate(nbt, true) completado para {}", localPos);

                // 4. Chamar onLoad para finalizar a inicialização do lado do cliente (inclui render.onLoad())
                // Isto é importante para que o BERenderManager processe as tiles carregadas.
                LOGGER.debug("📦 [CLC Renderer] Chamando virtualBE.onLoad() após handleUpdate para {}", localPos);
                virtualBE.onLoad();
                LOGGER.info("✅ [CLC Renderer] virtualBE.onLoad() completado para {}", localPos);

            } catch (UnsupportedOperationException uoe) {
                LOGGER.error("❌ CRÍTICO: handleUpdate(nbt, true) ou onLoad falhou com UnsupportedOperationException para {}. Causa: {}", localPos, uoe.getMessage(), uoe);
                return; // Não conseguimos carregar os dados, abortar
            } catch (Exception e) {
                LOGGER.error("❌ CRÍTICO: Falha ao chamar handleUpdate(nbt, true) ou onLoad para {}. Causa: {}", localPos, e.getMessage(), e);
                return; // Abortar em caso de outra exceção
            }

            // Adicionando novos logs de depuração
            if (renderWorld != null) {
                LOGGER.debug("🔍 [CLC Renderer] Verificando renderWorld.isClientSide: {} para {}", renderWorld.isClientSide(), localPos);
            } else {
                LOGGER.warn("⚠️ [CLC Renderer] renderWorld é NULO antes da verificação de virtualBE.render para {}", localPos);
            }
            if (virtualBE != null && virtualBE.getLevel() != null) { // virtualBE.getLevel() pode ser nulo se setLevel falhou ou não foi chamado
                LOGGER.debug("🔍 [CLC Renderer] Verificando virtualBE.isClient(): {} para {}", virtualBE.isClient(), localPos);
            } else {
                LOGGER.warn("⚠️ [CLC Renderer] virtualBE ou virtualBE.getLevel() é NULO antes da verificação de virtualBE.render para {}", localPos);
            }
            LOGGER.debug("🔍 [CLC Renderer] Verificando se virtualBE.render é nulo ANTES do if para {}", localPos);            // 5. Verificações detalhadas do BERenderManager
            LOGGER.debug("🔍 [CLC Renderer] Verificando virtualBE.render para {}", localPos);
            if (virtualBE.render != null) {
                LOGGER.debug("✅ [CLC Renderer] virtualBE.render NÃO é nulo para {}", localPos);
                
                // Verificar se há tiles carregadas
                if (virtualBE.tiles != null) {
                    LOGGER.debug("🔍 [CLC Renderer] virtualBE.tiles existe. Verificando conteúdo para {}", localPos);
                    // Tentar obter informações básicas sobre as tiles sem causar exceções
                    try {
                        boolean hasTiles = !virtualBE.tiles.isEmpty();
                        LOGGER.debug("🔍 [CLC Renderer] virtualBE.tiles.isEmpty(): {} para {}", !hasTiles, localPos);
                    } catch (Exception e) {
                        LOGGER.warn("⚠️ [CLC Renderer] Erro ao verificar virtualBE.tiles para {}: {}", localPos, e.getMessage());
                    }
                } else {
                    LOGGER.warn("⚠️ [CLC Renderer] virtualBE.tiles é NULO para {}", localPos);
                }
                
                // Tentar diferentes métodos para obter estruturas
                LOGGER.debug("🔍 [CLC Renderer] Tentando virtualBE.rendering() para {}", localPos);
                Iterable<LittleStructure> structuresToRender = null;
                try {
                    structuresToRender = virtualBE.rendering();
                    LOGGER.debug("✅ [CLC Renderer] virtualBE.rendering() executado sem exceção para {}", localPos);
                } catch (Exception e) {
                    LOGGER.error("❌ [CLC Renderer] Erro ao chamar virtualBE.rendering() para {}: {}", localPos, e.getMessage(), e);
                }
                
                // Tentar método alternativo loadedStructures diretamente
                LOGGER.debug("🔍 [CLC Renderer] Tentando virtualBE.loadedStructures(TICK_RENDERING) para {}", localPos);
                try {
                    var altStructures = virtualBE.loadedStructures(team.creative.littletiles.common.structure.attribute.LittleStructureAttribute.TICK_RENDERING);
                    if (altStructures != null) {
                        int count = 0;
                        for (var struct : altStructures) {
                            if (struct != null) count++;
                        }
                        LOGGER.debug("🔍 [CLC Renderer] loadedStructures(TICK_RENDERING) retornou {} estruturas para {}", count, localPos);
                        if (structuresToRender == null) {
                            structuresToRender = altStructures;
                            LOGGER.debug("🔄 [CLC Renderer] Usando loadedStructures como fallback para {}", localPos);
                        }
                    } else {
                        LOGGER.debug("🔍 [CLC Renderer] loadedStructures(TICK_RENDERING) retornou null para {}", localPos);
                    }
                } catch (Exception e) {
                    LOGGER.error("❌ [CLC Renderer] Erro ao chamar loadedStructures(TICK_RENDERING) para {}: {}", localPos, e.getMessage(), e);
                }
                
                boolean renderedSomething = false;
                if (structuresToRender != null) {
                    LOGGER.debug("🔍 [CLC Renderer] Iterando sobre estruturas para {}", localPos);
                    int structureCount = 0;
                    for (LittleStructure structure : structuresToRender) {
                        if (structure == null) continue;
                        structureCount++;

                        LOGGER.debug("➡️ [CLC Renderer] Encontrada estrutura #{}: {} para BE em {}", structureCount, structure.getClass().getSimpleName(), localPos);
                        
                        try {
                            poseStack.pushPose();
                            float partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                            structure.renderTick(poseStack, bufferSource, localPos, partialTicks);
                            poseStack.popPose();
                            renderedSomething = true;
                            LOGGER.debug("✅ [CLC Renderer] structure.renderTick() executado para estrutura #{} em {}", structureCount, localPos);
                        } catch (Exception e) {
                            LOGGER.error("❌ [CLC Renderer] Erro ao renderizar estrutura #{} em {}: {}", structureCount, localPos, e.getMessage(), e);
                        }
                    }
                    LOGGER.debug("🔍 [CLC Renderer] Total de estruturas processadas: {} para {}", structureCount, localPos);
                } else {
                     LOGGER.warn("⚠️ [CLC Renderer] Todas as tentativas de obter estruturas retornaram null para {}", localPos);
                }

                if (!renderedSomething) {
                    LOGGER.warn("⚠️ [CLC Renderer] Nenhuma estrutura encontrada ou renderizada para {}. Investigar NBT ou inicialização do BERenderManager.", localPos);
                } else {
                    LOGGER.info("✅ [CLC Renderer] {} estrutura(s) renderizada(s) com sucesso para {}", renderedSomething ? "Algumas" : "Nenhuma", localPos);
                }

                LOGGER.info("🎉 [CLC Renderer] renderMovementBehaviourTile finalizado para: {}", localPos);

            } else {
                LOGGER.warn("⚠️ [CLC Renderer] virtualBE.render é NULO para {}. BERenderManager não foi inicializado.", localPos);
                
                // Verificar se podemos forçar a inicialização
                LOGGER.debug("🔍 [CLC Renderer] Tentando verificar por que virtualBE.render é nulo para {}", localPos);
                if (virtualBE.getLevel() != null) {
                    LOGGER.debug("🔍 [CLC Renderer] virtualBE.getLevel() não é nulo. isClientSide: {} para {}", virtualBE.getLevel().isClientSide(), localPos);
                } else {
                    LOGGER.warn("⚠️ [CLC Renderer] virtualBE.getLevel() é NULO para {}", localPos);
                }
            }

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
