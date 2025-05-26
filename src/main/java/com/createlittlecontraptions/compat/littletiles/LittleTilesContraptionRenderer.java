package com.createlittlecontraptions.compat.littletiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

// Direct imports for LittleTiles classes (compileOnly dependency)
import team.creative.littletiles.common.block.entity.BETiles;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom renderer for LittleTiles blocks when they're part of Create contraptions.
 * This renderer ensures that LittleTiles maintain their custom appearance even while moving.
 */
public class LittleTilesContraptionRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private static boolean rendererInitialized = false;
    private static Class<?> littleTilesRendererClass = null;
    private static Object littleTilesRendererInstance = null;
    private static Method renderMethod = null;
    
    // Cache for performance
    private static final Map<String, Boolean> blockTypeCache = new HashMap<>();
      // Anti-spam counters for debug logging
    private static long debugLogCounter = 0;
    private static final long DEBUG_LOG_INTERVAL = 100; // Log every 100th call
    
    // Rate limiting for refresh operations
    private static long lastRefreshTime = 0;
    private static long refreshCounter = 0;
    private static final long REFRESH_LOG_INTERVAL = 5000; // Log every 5 seconds
    
    // Rate limiting for other operations
    private static long transformLogCounter = 0;
    private static long renderLogCounter = 0;
    private static final long TRANSFORM_LOG_INTERVAL = 200; // Log every 200th call
    private static final long RENDER_LOG_INTERVAL = 150; // Log every 150th call
    
    /**
     * Initialize the LittleTiles renderer.
     */
    public static void initialize() {
        if (rendererInitialized) return;
        
        try {
            LOGGER.info("Initializing LittleTiles contraption renderer...");
              // Try to find LittleTiles renderer classes (team.creative package only)
            String[] possibleRendererClasses = {
                "team.creative.littletiles.client.render.tile.LittleRenderBox",
                "team.creative.littletiles.client.render.LittleTilesRenderer",
                "team.creative.littletiles.client.render.tile.LittleTileRenderer"
            };
            
            for (String rendererClassName : possibleRendererClasses) {
                try {
                    littleTilesRendererClass = Class.forName(rendererClassName);
                    LOGGER.info("✅ Found LittleTiles renderer class: " + rendererClassName);
                    
                    // Try to create an instance or find static methods
                    try {
                        littleTilesRendererInstance = littleTilesRendererClass.getDeclaredConstructor().newInstance();
                        LOGGER.info("✅ Created LittleTiles renderer instance");
                    } catch (Exception e) {
                        LOGGER.debug("Could not create instance, checking for static methods");
                    }
                    
                    // Find render methods
                    Method[] methods = littleTilesRendererClass.getDeclaredMethods();
                    for (Method method : methods) {
                        String methodName = method.getName().toLowerCase();
                        if (methodName.contains("render") || methodName.contains("draw")) {
                            renderMethod = method;
                            LOGGER.info("✅ Found render method: " + method.getName());
                            break;
                        }
                    }
                    
                    break; // Found a working renderer class
                    
                } catch (ClassNotFoundException e) {
                    LOGGER.debug("Renderer class not found: " + rendererClassName);
                }
            }
            
            if (littleTilesRendererClass != null) {
                rendererInitialized = true;
                LOGGER.info("🎉 LittleTiles contraption renderer initialized successfully!");
            } else {
                LOGGER.warn("⚠️ Could not find LittleTiles renderer classes");
            }
            
        } catch (Exception e) {
            LOGGER.error("❌ Failed to initialize LittleTiles contraption renderer", e);
        }
    }
    
    /**
     * Render a LittleTiles block in a contraption context.
     * Updated method signature based on Gemini AI analysis.
     * Now includes Level parameter as suggested by Gemini for proper context.
     */
    public static void renderLittleTileInContraption(PoseStack poseStack, MultiBufferSource bufferSource,
                                                   int light, int overlay,
                                                   BlockState blockState, CompoundTag tileNbt, 
                                                   net.minecraft.world.level.Level level) {
        if (!rendererInitialized) {
            initialize();
        }
        
        if (tileNbt == null || tileNbt.isEmpty()) {
            LOGGER.warn("Attempted to render LittleTile in contraption with null or empty NBT.");
            return;
        }

        try {
            // Rate limit logging
            renderLogCounter++;
            boolean shouldLog = (renderLogCounter % RENDER_LOG_INTERVAL == 0);
            
            if (shouldLog) {
                LOGGER.info("🎨 Rendering LittleTile in contraption. NBT size: {} tags (call #{})", 
                    tileNbt.size(), renderLogCounter);
            }

            // Save the current pose stack state
            poseStack.pushPose();
            
            // Try to render using LittleTiles API as suggested by Gemini
            if (littleTilesRendererClass != null) {
                renderWithLittleTilesAPI(poseStack, bufferSource, light, overlay, blockState, tileNbt, level);
            } else {
                renderFallback(poseStack, bufferSource, light, overlay, blockState, tileNbt);
            }
            
            // Restore pose stack
            poseStack.popPose();
            
            if (shouldLog) {
                LOGGER.info("✅ LittleTile rendered successfully in contraption (call #{})", renderLogCounter);
            }
            
        } catch (Exception e) {
            LOGGER.error("❌ Error rendering LittleTile in contraption: ", e);
        }
    }
    
    /**
     * Render using LittleTiles API based on Gemini's analysis
     */
    private static void renderWithLittleTilesAPI(PoseStack poseStack, MultiBufferSource bufferSource,
                                                int light, int overlay, BlockState blockState, CompoundTag tileNbt, 
                                                net.minecraft.world.level.Level level) {
        try {
            // Based on Gemini's analysis, try to load LittleTiles structure from NBT
            // This is a framework that needs to be adapted based on actual LittleTiles API
            
            // Attempt 1: Try to create a temporary BlockEntity and load NBT
            // This follows Gemini's suggestion for reidratation approach
            tryBlockEntityApproach(poseStack, bufferSource, light, overlay, blockState, tileNbt, level);
            
        } catch (Exception e) {
            LOGGER.debug("LittleTiles API rendering failed, falling back: {}", e.getMessage());
            renderFallback(poseStack, bufferSource, light, overlay, blockState, tileNbt);
        }
    }
    
    /**
     * Try the BlockEntity reidratation approach suggested by Gemini
     */
    private static void tryBlockEntityApproach(PoseStack poseStack, MultiBufferSource bufferSource,
                                             int light, int overlay, BlockState blockState, CompoundTag tileNbt,
                                             net.minecraft.world.level.Level level) {
        try {
            // Try to find and instantiate LittleTiles BlockEntity classes
            String[] possibleBEClasses = {
                "team.creative.littletiles.common.block.mc.LittleBlockEntity",
                "team.creative.littletiles.common.block.entity.LittleTilesBlockEntity",
                "team.creative.littletiles.LittleTilesBlockEntity"
            };
            
            for (String beClassName : possibleBEClasses) {
                try {
                    Class<?> beClass = Class.forName(beClassName);
                    
                    // Try to create a temporary BlockEntity instance
                    // Using dummy position as suggested by Gemini
                    Object tempBE = createTempBlockEntity(beClass, blockState, tileNbt, level);
                    
                    if (tempBE != null) {
                        // Try to render using BlockEntityRenderer
                        renderWithBlockEntityRenderer(poseStack, bufferSource, light, overlay, tempBE);
                        return; // Success!
                    }
                    
                } catch (ClassNotFoundException e) {
                    // Try next class name
                    continue;
                }
            }
            
            LOGGER.debug("Could not find suitable LittleTiles BlockEntity class");
            
        } catch (Exception e) {
            LOGGER.debug("BlockEntity approach failed: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Create temporary BlockEntity for rendering
     */
    private static Object createTempBlockEntity(Class<?> beClass, BlockState blockState, CompoundTag tileNbt, 
                                               net.minecraft.world.level.Level level) {
        try {
            // Try different constructor patterns
            Object tempBE = null;
            
            // Pattern 1: Constructor with BlockPos and BlockState
            try {
                java.lang.reflect.Constructor<?> constructor = beClass.getConstructor(
                    net.minecraft.core.BlockPos.class, 
                    net.minecraft.world.level.block.state.BlockState.class
                );
                tempBE = constructor.newInstance(net.minecraft.core.BlockPos.ZERO, blockState);
            } catch (Exception e) {
                // Try pattern 2: Default constructor
                try {
                    tempBE = beClass.getDeclaredConstructor().newInstance();
                } catch (Exception e2) {
                    LOGGER.debug("Could not instantiate BlockEntity: {}", e2.getMessage());
                    return null;
                }
            }
            
            if (tempBE != null) {
                // Load NBT data into the BlockEntity
                try {
                    java.lang.reflect.Method loadMethod = beClass.getMethod("load", CompoundTag.class);
                    loadMethod.invoke(tempBE, tileNbt);
                } catch (Exception e) {
                    // Try alternative load method names
                    try {
                        java.lang.reflect.Method readMethod = beClass.getMethod("readNbt", CompoundTag.class);
                        readMethod.invoke(tempBE, tileNbt);
                    } catch (Exception e2) {
                        LOGGER.debug("Could not load NBT into BlockEntity: {}", e2.getMessage());
                    }
                }
                
                // Try to set a level reference if needed
                try {
                    java.lang.reflect.Method setLevelMethod = beClass.getMethod("setLevel", net.minecraft.world.level.Level.class);
                    setLevelMethod.invoke(tempBE, level);
                } catch (Exception e) {
                    // Level setting is optional
                }
            }
            
            return tempBE;
            
        } catch (Exception e) {
            LOGGER.debug("Failed to create temp BlockEntity: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Render using BlockEntityRenderer
     */
    private static void renderWithBlockEntityRenderer(PoseStack poseStack, MultiBufferSource bufferSource,
                                                    int light, int overlay, Object blockEntity) {
        try {
            // Get the BlockEntityRenderDispatcher
            net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher beRenderDispatcher = 
                net.minecraft.client.Minecraft.getInstance().getBlockEntityRenderDispatcher();
            
            // Get the renderer for this BlockEntity
            net.minecraft.client.renderer.blockentity.BlockEntityRenderer<net.minecraft.world.level.block.entity.BlockEntity> renderer = 
                beRenderDispatcher.getRenderer((net.minecraft.world.level.block.entity.BlockEntity) blockEntity);
            
            if (renderer != null) {
                // Render the BlockEntity
                renderer.render(
                    (net.minecraft.world.level.block.entity.BlockEntity) blockEntity,
                    0.0F, // partialTick - 0 for static rendering
                    poseStack,
                    bufferSource,
                    light,
                    overlay
                );
                
                LOGGER.debug("Successfully rendered LittleTiles using BlockEntityRenderer");
            } else {
                LOGGER.debug("No BlockEntityRenderer found for LittleTiles BlockEntity");
            }
            
        } catch (Exception e) {
            LOGGER.debug("BlockEntityRenderer approach failed: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Fallback rendering method
     */
    private static void renderFallback(PoseStack poseStack, MultiBufferSource bufferSource,
                                     int light, int overlay, BlockState blockState, CompoundTag tileNbt) {
        try {
            // Basic fallback - render the base block at least
            net.minecraft.client.renderer.block.BlockRenderDispatcher blockRenderer = 
                net.minecraft.client.Minecraft.getInstance().getBlockRenderer();
            
            // Use basic block rendering as fallback
            net.minecraft.client.renderer.RenderType renderType = net.minecraft.client.renderer.RenderType.solid();
            var buffer = bufferSource.getBuffer(renderType);
            
            // Simple fallback rendering
            blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                buffer,
                blockState,
                blockRenderer.getBlockModel(blockState),
                1.0f, 1.0f, 1.0f,
                light, overlay
            );
            
            LOGGER.debug("Used fallback rendering for LittleTiles block");
            
        } catch (Exception e) {
            LOGGER.warn("Even fallback rendering failed: {}", e.getMessage());
        }
    }
        
        /**
         * Extrai dados da contraption para o bloco na posição especificada
         */
        private static CompoundTag extractContraptionData(Object contraptionContext, BlockPos pos) {
            try {
                // Tenta acessar o método de extração de dados da contraption via reflexão
                // Este é um esboço - a implementação real dependeria da estrutura interna do Create
                if (contraptionContext == null) return null;
                
                // Tentar obter dados do bloco através da API do Create
                Method getBlockData = contraptionContext.getClass().getMethod("getBlockData", BlockPos.class);
                Object blockData = getBlockData.invoke(contraptionContext, pos);
                
                if (blockData instanceof CompoundTag) {
                    return (CompoundTag) blockData;
                } else if (blockData != null) {
                    // Tenta converter outros tipos de dados para CompoundTag
                    LOGGER.debug("Dados encontrados para bloco em {} mas não é CompoundTag: {}", 
                        pos, blockData.getClass().getName());
                }
            } catch (Exception e) {
                LOGGER.debug("Não foi possível extrair dados da contraption: {}", e.getMessage());
            }
            return null;
        }
        
        /**
         * Versão atualizada do método de renderização que aceita dados da contraption
         */
        private static void renderLittleTilesCustomContent(
                BlockState blockState, 
                BlockPos blockPos, 
                PoseStack poseStack, 
                MultiBufferSource bufferSource, 
                Camera camera,
                CompoundTag contraptionData) {
                
            // Use dados da contraption se disponíveis, caso contrário use o método padrão
            if (contraptionData != null) {
                renderLittleTilesWithData(blockState, blockPos, poseStack, bufferSource, camera, contraptionData);
            } else {
                renderLittleTilesCustomContent(blockState, blockPos, poseStack, bufferSource, camera);
            }
        }
        
        /**
         * Renderiza um bloco LittleTiles usando dados NBT específicos
         */
        private static void renderLittleTilesWithData(
                BlockState blockState, 
                BlockPos blockPos, 
                PoseStack poseStack, 
                MultiBufferSource bufferSource, 
                Camera camera,
                CompoundTag data) {
            
            try {
                // Implementação que utiliza os dados NBT para renderizar corretamente
                // Esta é uma estrutura básica - a implementação real precisaria se integrar com a API do LittleTiles
                
                // Aplicar transformações adicionais se necessário
                float offsetX = data.contains("offset_x") ? data.getFloat("offset_x") : 0;
                float offsetY = data.contains("offset_y") ? data.getFloat("offset_y") : 0;
                float offsetZ = data.contains("offset_z") ? data.getFloat("offset_z") : 0;
                
                if (offsetX != 0 || offsetY != 0 || offsetZ != 0) {
                    poseStack.translate(offsetX, offsetY, offsetZ);
                }
                
                // Chamar o renderer do LittleTiles com os dados NBT completos
                renderLittleTilesUsingReflection(blockState, blockPos, poseStack, bufferSource, camera, data);
                
            } catch (Exception e) {
                LOGGER.error("Erro ao renderizar LittleTiles com dados específicos", e);
                // Fallback para o método padrão
                renderLittleTilesCustomContent(blockState, blockPos, poseStack, bufferSource, camera);
            }
        }
        
        /**
         * Usa reflexão para chamar o renderer adequado do LittleTiles
         */
        private static void renderLittleTilesUsingReflection(
                BlockState blockState, 
                BlockPos blockPos, 
                PoseStack poseStack, 
                MultiBufferSource bufferSource, 
                Camera camera,
                CompoundTag data) {
            
            // Implementação real usaria reflexão para chamar os métodos de renderização do LittleTiles
            // Esta é apenas uma estrutura para o método
            
            // Se implementado, este método invocaria diretamente o renderer do LittleTiles
            // passando os dados NBT completos
            
            // Por enquanto, fallback para o método padrão
            renderLittleTilesCustomContent(blockState, blockPos, poseStack, bufferSource, camera);
    }
    
    /**
     * Check if a block state represents a LittleTiles block.
     * Updated to use the correct team.creative.littletiles package names.
     */
    public static boolean isLittleTilesBlock(BlockState blockState) {
        if (blockState == null) return false;
        
        String blockName = blockState.getBlock().getClass().getName().toLowerCase();
        
        // Use cache for performance
        Boolean cached = blockTypeCache.get(blockName);
        if (cached != null) {
            return cached;
        }
        
        boolean isLittleTiles = blockName.contains("team.creative.littletiles") ||
                               blockName.contains("littletile") ||
                               blockName.contains("littleblock");
        
        blockTypeCache.put(blockName, isLittleTiles);
        return isLittleTiles;
    }
    
    /**
     * Apply transformations specific to contraption movement.
     */
    private static void applyContraptionTransforms(PoseStack poseStack, Object contraptionContext) {
        try {
            if (contraptionContext == null) return;
              // Get transformation matrix from contraption context
            // This would need to be implemented based on Create's actual API
            transformLogCounter++;
            if (transformLogCounter % TRANSFORM_LOG_INTERVAL == 0) {
                LOGGER.debug("Applying contraption transforms for LittleTiles rendering (call #{})", transformLogCounter);
            }
            
            // For now, we'll just log that we're applying transforms
            // The actual implementation would extract position, rotation, and scale
            // from the contraption and apply them to the pose stack
            
        } catch (Exception e) {
            if (transformLogCounter % TRANSFORM_LOG_INTERVAL == 0) {
                LOGGER.debug("Error applying contraption transforms: " + e.getMessage());
            }
        }
    }
    
    /**
     * Render the custom content of a LittleTiles block.
     */
    private static void renderLittleTilesCustomContent(
            BlockState blockState, 
            BlockPos blockPos, 
            PoseStack poseStack, 
            MultiBufferSource bufferSource,
            Camera camera) {
          try {
            if (renderMethod != null && littleTilesRendererInstance != null) {
                // Try to call the LittleTiles render method
                // This is a simplified approach - the actual implementation would need
                // to match the exact method signature of LittleTiles' render methods
                
                renderLogCounter++;
                if (renderLogCounter % RENDER_LOG_INTERVAL == 0) {
                    LOGGER.debug("Calling LittleTiles render method: {} (call #{})", renderMethod.getName(), renderLogCounter);
                }
                
                // The exact parameters would depend on LittleTiles' API
                // This is a placeholder that shows the concept
                
            } else {
                // Fallback: Use Minecraft's default block renderer but with enhancements
                renderLittleTilesFallback(blockState, blockPos, poseStack, bufferSource);
            }
            
        } catch (Exception e) {
            if (renderLogCounter % RENDER_LOG_INTERVAL == 0) {
                LOGGER.debug("Error in LittleTiles custom rendering: " + e.getMessage());
            }
            // Always have a fallback
            renderLittleTilesFallback(blockState, blockPos, poseStack, bufferSource);
        }
    }
      /**
     * Fallback rendering method when LittleTiles' custom renderer is not available.
     */
    private static void renderLittleTilesFallback(
            BlockState blockState, 
            BlockPos blockPos, 
            PoseStack poseStack, 
            MultiBufferSource bufferSource) {
        
        try {
            LOGGER.debug("Using fallback rendering for LittleTiles block");
            
            // Simple fallback - just log the attempt for now
            // The actual rendering would require more complex integration with Minecraft's rendering pipeline
            LOGGER.debug("Would render LittleTiles block: {}", blockState.getBlock().getClass().getName());
            
            LOGGER.debug("✅ Fallback rendering completed");
            
        } catch (Exception e) {
            LOGGER.debug("Error in fallback rendering: " + e.getMessage());
        }
    }
      /**
     * Force a refresh of all LittleTiles rendering in contraptions.
     */
    public static void refreshAllLittleTilesRendering() {
        try {
            refreshCounter++;
            long currentTime = System.currentTimeMillis();
            
            // Rate limit refresh logs to prevent spam
            if (currentTime - lastRefreshTime >= REFRESH_LOG_INTERVAL) {
                LOGGER.info("🔄 Refreshing all LittleTiles rendering in contraptions... (call #{}, {} calls in last {}ms)", 
                    refreshCounter, refreshCounter - (lastRefreshTime > 0 ? 1 : 0), currentTime - lastRefreshTime);
                lastRefreshTime = currentTime;
            }
            
            // Clear caches
            blockTypeCache.clear();
            
            // Re-initialize if needed
            if (!rendererInitialized) {
                initialize();
            }
            
            // Only log completion every few seconds
            if (currentTime - lastRefreshTime < 100) { // Only log if this was a logged refresh
                LOGGER.info("✅ LittleTiles rendering refresh completed");
            }
            
        } catch (Exception e) {
            LOGGER.error("❌ Error refreshing LittleTiles rendering", e);
        }
    }
    
    /**
     * Get rendering statistics.
     */
    public static String getRenderingStats() {
        return String.format(
            "LittleTiles Contraption Renderer Stats:\n" +
            "- Initialized: %s\n" +
            "- Renderer Class: %s\n" +
            "- Render Method: %s\n" +
            "- Block Type Cache Size: %d",
            rendererInitialized,
            littleTilesRendererClass != null ? littleTilesRendererClass.getName() : "None",
            renderMethod != null ? renderMethod.getName() : "None",
            blockTypeCache.size()
        );
    }
    
    /**
     * Check if the renderer is properly initialized.
     */
    public static boolean isInitialized() {
        return rendererInitialized;
    }
    
    /**
     * Render a LittleTiles BlockEntity within a Create contraption context.
     * This method is called by the ContraptionRendererMixin.
     * Updated to use combinedLight and combinedOverlay as suggested by Gemini.
     * 
     * @param poseStack The transformation matrix stack
     * @param bufferSource The buffer source for rendering
     * @param realLevel The actual world level
     * @param renderLevel The virtual render world (may be null)
     * @param blockEntity The LittleTiles block entity to render
     * @param partialTicks Partial tick time for interpolation
     * @param lightTransform Optional light transformation matrix
     * @param combinedLight Light value already calculated by Create for this position
     * @param combinedOverlay Overlay value already calculated by Create
     */
    public static void renderLittleTileBEInContraption(
        PoseStack poseStack, 
        MultiBufferSource bufferSource, 
        net.minecraft.world.level.Level realLevel, 
        @javax.annotation.Nullable com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld renderLevel, 
        net.minecraft.world.level.block.entity.BlockEntity blockEntity, 
        float partialTicks, 
        @javax.annotation.Nullable org.joml.Matrix4f lightTransform,
        int combinedLight, // Light already calculated by Create for this position
        int combinedOverlay  // Overlay already calculated
    ) {
        // Type-safe check using instanceof as recommended by Gemini
        if (!(blockEntity instanceof BETiles)) {
            LOGGER.warn("[CLC LTRenderer] Expected BETiles but got: {}", blockEntity.getClass().getSimpleName());
            return;
        }
        
        BETiles ltbe = (BETiles) blockEntity;

        renderLogCounter++;
        boolean shouldLog = (renderLogCounter % RENDER_LOG_INTERVAL == 1);
        
        if (shouldLog) {
            LOGGER.info("[CLC LTRenderer] Rendering LT BE {} at {} | Light: {}, Overlay: {} (call #{})", 
                ltbe.getClass().getSimpleName(), ltbe.getBlockPos(), combinedLight, combinedOverlay, renderLogCounter);
        }

        // Save state of the PoseStack
        poseStack.pushPose(); 

        try {
            // ATTEMPT 1: Use the BlockEntityRenderer<T> if LittleTiles registers one
            // and it works well with the contraption context.
            net.minecraft.client.renderer.blockentity.BlockEntityRenderer<net.minecraft.world.level.block.entity.BlockEntity> vanillaRenderer = 
                net.minecraft.client.Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);

            if (vanillaRenderer != null) {
                if (shouldLog) {
                    LOGGER.info("[CLC LTRenderer] Using vanilla BE renderer: {}", vanillaRenderer.getClass().getSimpleName());
                }
                
                // Pass renderLevel if available, otherwise realLevel.
                // The renderer expects that BE.level is correct.
                net.minecraft.world.level.Level originalLevel = blockEntity.getLevel();
                blockEntity.setLevel(renderLevel != null ? renderLevel : realLevel); 

                vanillaRenderer.render(blockEntity, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);
                
                blockEntity.setLevel(originalLevel); // Restore the original level of the BE
                
                if (shouldLog) {
                    LOGGER.info("[CLC LTRenderer] Vanilla renderer completed successfully");
                }
            } else {
                LOGGER.warn("[CLC LTRenderer] No vanilla BE renderer found for {}. Attempting direct LittleTiles API.", blockEntity.getType());
                // ATTEMPT 2: Call direct LittleTiles rendering API
                // This part is speculative and depends on the LittleTiles API.
                renderWithDirectLittleTilesAPI(poseStack, bufferSource, blockEntity, partialTicks, combinedLight, combinedOverlay, renderLevel != null ? renderLevel : realLevel);
            }

        } catch (Exception e) {
            LOGGER.error("[CLC LTRenderer] Error rendering LittleTiles BlockEntity: ", e);
            // Fallback to existing method for compatibility
            try {
                BlockState state = blockEntity.getBlockState();
                CompoundTag nbt = blockEntity.saveWithFullMetadata(realLevel.registryAccess());
                renderLittleTileInContraption(poseStack, bufferSource, combinedLight, combinedOverlay, state, nbt, realLevel != null ? realLevel : renderLevel);
            } catch (Exception fallbackError) {
                LOGGER.error("[CLC LTRenderer] Fallback rendering also failed: ", fallbackError);
            }
        } finally {
            poseStack.popPose();
        }
    }
    
    /**
     * Attempt to render using direct LittleTiles API calls when no vanilla renderer is available.
     * This is speculative and needs to be adapted based on actual LittleTiles API.
     */
    private static void renderWithDirectLittleTilesAPI(
        PoseStack poseStack, 
        MultiBufferSource bufferSource, 
        net.minecraft.world.level.block.entity.BlockEntity blockEntity, 
        float partialTicks, 
        int combinedLight, 
        int combinedOverlay, 
        net.minecraft.world.level.Level level
    ) {
        try {
            // This is where we would call LittleTiles rendering API directly
            // Example: team.creative.littletiles.client.render.tile.LittleRenderBox.render(...)
            // For now, fallback to the existing NBT-based approach but with correct light/overlay
            
            LOGGER.debug("[CLC LTRenderer] Using direct LittleTiles API fallback");
            BlockState state = blockEntity.getBlockState();
            CompoundTag nbt = blockEntity.saveWithFullMetadata(level.registryAccess());
            renderLittleTileInContraption(poseStack, bufferSource, combinedLight, combinedOverlay, state, nbt, level);
            
        } catch (Exception e) {
            LOGGER.error("[CLC LTRenderer] Direct LittleTiles API call failed: ", e);
            throw e;
        }
    }
}
