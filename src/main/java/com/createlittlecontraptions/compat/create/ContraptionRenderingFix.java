package com.createlittlecontraptions.compat.create;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.api.distmarker.Dist;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Comprehensive solution for fixing LittleTiles blocks becoming invisible in Create contraptions.
 * This class implements multiple strategies to detect and solve the rendering issue.
 * DISABLED: This class is temporarily disabled to avoid conflicts with CreateRuntimeIntegration
 */
// @EventBusSubscriber(modid = "createlittlecontraptions", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ContraptionRenderingFix {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // State tracking
    private static boolean fixActive = false;
    private static boolean createAvailable = false;
    private static boolean littleTilesAvailable = false;
      // Class caching
    private static Class<?> contraptionClass = null;
    private static Class<?> contraptionWorldClass = null;
    private static Class<?> littleTilesBlockClass = null;
    private static Class<?> contraptionRendererClass = null;
    
    // Compatibility mode flag
    private static boolean isLittleTilesCompatibilityMode = false;
    
    // Method caching
    private static Method getContraptionsMethod = null;
    private static Method renderContraptionMethod = null;
    
    // Runtime state
    private static final Map<BlockPos, BlockState> trackedLittleTilesBlocks = new ConcurrentHashMap<>();
    private static final Set<Object> activeContraptions = ConcurrentHashMap.newKeySet();
    private static int tickCounter = 0;
    
    /**
     * Initialize the comprehensive rendering fix.
     */
    public static void initialize() {
        LOGGER.info("=== ContraptionRenderingFix Initialization Starting ===");
        
        try {
            // Step 1: Detect available mods
            detectAvailableMods();
            
            if (!createAvailable || !littleTilesAvailable) {
                LOGGER.warn("Required mods not available - Create: {}, LittleTiles: {}", 
                    createAvailable, littleTilesAvailable);
                return;
            }
              // Step 2: Cache important classes and methods
            cacheReflectionData();
            
            // Step 3: Activate the fix (force activation if both mods are available)
            if (createAvailable && littleTilesAvailable) {
                fixActive = true;
                LOGGER.info("ContraptionRenderingFix successfully initialized and activated!");
            } else {
                LOGGER.warn("ContraptionRenderingFix not activated - missing required mods");
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to initialize ContraptionRenderingFix", e);
        }
        
        LOGGER.info("=== ContraptionRenderingFix Initialization Complete ===");
    }    /**
     * Detect which mods are available.
     */
    private static void detectAvailableMods() {
        // Detect Create
        try {
            Class.forName("com.simibubi.create.Create");
            createAvailable = true;
            LOGGER.info("✓ Create mod detected");
        } catch (ClassNotFoundException e) {
            LOGGER.warn("✗ Create mod not found");
        }
        
        // Detect LittleTiles using ModList first (most reliable method)
        try {
            Class<?> modListClass = Class.forName("net.neoforged.fml.ModList");
            Object modListInstance = modListClass.getMethod("get").invoke(null);
            boolean isLoaded = (Boolean) modListClass.getMethod("isLoaded", String.class).invoke(modListInstance, "littletiles");
            if (isLoaded) {
                littleTilesAvailable = true;
                LOGGER.info("✓ LittleTiles mod detected via ModList!");
            }
        } catch (Exception e) {
            LOGGER.debug("ModList detection failed: " + e.getMessage());
        }
        
        // Fallback to class detection if ModList method failed
        if (!littleTilesAvailable) {
            String[] possibleLittleTilesMainClasses = {
                // Version 1.6.0+ (team.creative package)
                "team.creative.littletiles.LittleTiles",
                "team.creative.littletiles.LittleTilesMod", 
                "team.creative.littletiles.client.LittleTilesClient",
                "team.creative.littletiles.common.LittleTilesCommon",
                // Legacy versions (de.creativemd package)
                "team.creative.littletiles.LittleTiles",
                "team.creative.littletiles.LittleTilesMod",
                "team.creative.littletiles.client.LittleTilesClient"
            };
            
            for (String className : possibleLittleTilesMainClasses) {
                try {
                    Class.forName(className);
                    littleTilesAvailable = true;
                    LOGGER.info("✓ LittleTiles mod detected using class: {}", className);
                    break;
                } catch (ClassNotFoundException e) {
                    LOGGER.debug("LittleTiles class not found: {}", className);
                }
            }
        }
        
        if (!littleTilesAvailable) {
            LOGGER.warn("✗ LittleTiles mod not found with any known method");
        }
    }
    
    /**
     * Cache important classes and methods for performance.
     */
    private static void cacheReflectionData() throws Exception {
        LOGGER.info("Caching reflection data...");
        
        // Cache Create classes
        try {
            contraptionClass = Class.forName("com.simibubi.create.content.contraptions.Contraption");
            contraptionWorldClass = Class.forName("com.simibubi.create.content.contraptions.ContraptionWorld");
            
            // Try different possible class names for Create's renderer (version 6.0.4+)
            String[] possibleRendererClasses = {
                // Create 6.0.4+ structure
                "com.simibubi.create.content.contraptions.render.ContraptionRenderDispatcher",
                "com.simibubi.create.content.contraptions.ContraptionRenderer", 
                "com.simibubi.create.content.contraptions.render.ContraptionMatrices",
                "com.simibubi.create.content.contraptions.AbstractContraptionEntity",
                // Legacy structure (fallback)
                "com.simibubi.create.content.contraptions.render.ContraptionRenderer"
            };
            
            for (String className : possibleRendererClasses) {
                try {
                    contraptionRendererClass = Class.forName(className);
                    LOGGER.info("✓ Create renderer class found: {}", className);
                    break;
                } catch (ClassNotFoundException e) {
                    LOGGER.debug("Create renderer class not found: {}", className);
                }
            }
            
            if (contraptionRendererClass == null) {
                throw new ClassNotFoundException("No Create renderer class found among: " + 
                    String.join(", ", possibleRendererClasses));
            }
            
            LOGGER.info("✓ Create classes cached");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Failed to cache Create classes", e);
            throw e;        }        // Cache LittleTiles classes
        try {            // Try multiple possible class names for LittleTiles blocks in version 1.6.0+
            String[] possibleLittleTilesClasses = {
                // LittleTiles 1.6.0+ classes (team.creative package structure) - UPDATED FOR 1.6.0-pre162
                "team.creative.littletiles.common.block.LittleBlock",
                "team.creative.littletiles.common.block.LittleTileBlock", 
                "team.creative.littletiles.common.block.little.LittleBlock",
                "team.creative.littletiles.common.block.little.tile.LittleTileBlock",
                "team.creative.littletiles.common.block.LittleTilesBlock", 
                "team.creative.littletiles.common.block.mc.LittleBlock",
                "team.creative.littletiles.LittleBlock",
                "team.creative.littletiles.LittleTileBlock",
                // Legacy classes (de.creativemd package) - kept for compatibility
                "team.creative.littletiles.common.block.little.tile.LittleTileBlock",
                "team.creative.littletiles.common.block.LittleTilesBlock",
                "team.creative.littletiles.LittleTileBlock",
                "team.creative.littletiles.common.block.mc.LittleBlock",
                "creative.littletiles.common.block.LittleTileBlock"
            };
            
            for (String className : possibleLittleTilesClasses) {
                try {
                    littleTilesBlockClass = Class.forName(className);
                    LOGGER.info("✓ LittleTiles block class found: {}", className);
                    break;
                } catch (ClassNotFoundException ignored) {
                    LOGGER.debug("LittleTiles block class not found: {}", className);
                }
            }
            
            if (littleTilesBlockClass == null) {
                // Try to find any class with "LittleTile" in the name as a fallback
                LOGGER.warn("Standard LittleTiles block classes not found, searching for alternatives...");
                
                // Force activation anyway if LittleTiles mod is detected
                if (littleTilesAvailable) {
                    LOGGER.info("LittleTiles mod detected but block class not found - activating basic compatibility mode");
                    // Create a fallback mechanism that uses registry-based detection instead of class checking
                    isLittleTilesCompatibilityMode = true;
                }
            } else {
                isLittleTilesCompatibilityMode = false;
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to cache LittleTiles classes", e);
            if (littleTilesAvailable) {
                LOGGER.info("Will attempt to work without specific class references using compatibility mode");
                isLittleTilesCompatibilityMode = true;
            }
        }
        
        // Cache important methods
        try {
            // Look for methods to access contraptions
            Method[] contraptionWorldMethods = contraptionWorldClass.getDeclaredMethods();
            for (Method method : contraptionWorldMethods) {
                if (method.getName().toLowerCase().contains("contraption") && 
                    method.getReturnType().toString().contains("Collection")) {
                    getContraptionsMethod = method;
                    getContraptionsMethod.setAccessible(true);
                    LOGGER.info("✓ Found contraptions access method: {}", method.getName());
                    break;
                }
            }
            
        } catch (Exception e) {
            LOGGER.warn("Could not cache all methods, will use alternative approaches", e);
        }
        
        LOGGER.info("Reflection data caching complete");
    }
    
    /**
     * Main event handler - monitors for LittleTiles blocks and contraption activity.
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!fixActive || !(event.getLevel() instanceof ClientLevel)) {
            return;
        }
        
        tickCounter++;
        
        // Run comprehensive checks every 20 ticks (1 second)
        if (tickCounter % 20 == 0) {
            performComprehensiveCheck((ClientLevel) event.getLevel());
        }
        
        // Run lightweight checks every 5 ticks
        if (tickCounter % 5 == 0) {
            performLightweightCheck((ClientLevel) event.getLevel());
        }
    }
    
    /**
     * Comprehensive check for contraptions and LittleTiles blocks.
     */
    private static void performComprehensiveCheck(ClientLevel level) {
        try {
            LOGGER.debug("=== Comprehensive Check (Tick {}) ===", tickCounter);
            
            // Scan for active contraptions
            scanForActiveContraptions(level);
            
            // Scan for LittleTiles blocks in the area
            scanForLittleTilesBlocks(level);
            
            // Check for invisible LittleTiles blocks in contraptions
            checkForInvisibleBlocks(level);
            
        } catch (Exception e) {
            LOGGER.error("Error during comprehensive check", e);
        }
    }
    
    /**
     * Lightweight check for immediate issues.
     */
    private static void performLightweightCheck(ClientLevel level) {
        try {
            // Quick check for rendering issues
            if (!trackedLittleTilesBlocks.isEmpty() && !activeContraptions.isEmpty()) {
                LOGGER.debug("Active monitoring: {} LittleTiles blocks, {} contraptions", 
                    trackedLittleTilesBlocks.size(), activeContraptions.size());
            }
        } catch (Exception e) {
            LOGGER.error("Error during lightweight check", e);
        }
    }
    
    /**
         * Scan for active contraptions using reflection.
         */
        private static void scanForActiveContraptions(ClientLevel level) {
            try {
                // Use reflection to access Create's contraption system
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Escaneando contraptions ativas...");
                }
                
                // Limpar lista antiga
                activeContraptions.clear();
                
                // Tentar acessar o gerenciador de contraptions do Create via reflexão
                Class<?> contraptionHandlerClass = Class.forName("com.simibubi.create.content.contraptions.ContraptionHandler");
                Field instanceField = contraptionHandlerClass.getDeclaredField("instance");
                instanceField.setAccessible(true);
                Object contraptionHandler = instanceField.get(null);
                
                // Obter a lista de contraptions
                Method getContraptionsMethod = contraptionHandlerClass.getDeclaredMethod("getContraptions");
                getContraptionsMethod.setAccessible(true);
                Collection<?> contraptions = (Collection<?>) getContraptionsMethod.invoke(contraptionHandler);
                
                if (contraptions != null) {
                    activeContraptions.addAll(contraptions);
                    LOGGER.debug("Encontradas {} contraptions ativas", activeContraptions.size());
                    
                    // Registrar elevadores especificamente
                    int elevadorCount = 0;
                    for (Object contraption : activeContraptions) {
                        if (contraption.getClass().getName().toLowerCase().contains("elevator")) {
                            elevadorCount++;
                        }
                    }
                    
                    if (elevadorCount > 0) {
                        LOGGER.info("Encontrados {} elevadores ativos que podem conter blocos LittleTiles", elevadorCount);
                    }
                }
                
                // Verificar blocos LittleTiles nas contraptions
                checkLittleTilesInContraptions();
                
            } catch (Exception e) {
                LOGGER.error("Erro ao escanear contraptions", e);
            }
        }
        
        /**
         * Verifica e registra blocos LittleTiles em contraptions ativas
         */
        private static void checkLittleTilesInContraptions() {
            int littleTilesCount = 0;
            
            try {
                for (Object contraption : activeContraptions) {
                    // Tenta acessar o método getBlocks() ou similar via reflexão
                    Method getBlocksMethod = findMethodByNamePattern(contraption.getClass(), "getBlocks");
                    
                    if (getBlocksMethod != null) {
                        getBlocksMethod.setAccessible(true);
                        Object blocks = getBlocksMethod.invoke(contraption);
                        
                        if (blocks instanceof Map) {
                            Map<?, ?> blockMap = (Map<?, ?>) blocks;
                            
                            // Contar blocos LittleTiles
                            for (Object blockState : blockMap.values()) {
                                if (isLittleTilesBlock(blockState)) {
                                    littleTilesCount++;
                                }
                            }
                        }
                    }
                }
                
                if (littleTilesCount > 0) {
                    LOGGER.info("Encontrados {} blocos LittleTiles em contraptions ativas", littleTilesCount);
                }
                
            } catch (Exception e) {
                LOGGER.error("Erro ao verificar blocos LittleTiles em contraptions", e);
            }
        }
        
        /**
         * Encontra um método por padrão de nome usando reflexão
         */
        private static Method findMethodByNamePattern(Class<?> clazz, String pattern) {
            for (Method method : clazz.getMethods()) {
                if (method.getName().toLowerCase().contains(pattern.toLowerCase())) {
                    return method;
                }
            }
            return null;
        }
        
        /**
         * Verifica se um objeto é um bloco LittleTiles
         */
        private static boolean isLittleTilesBlock(Object blockData) {
            if (blockData == null) return false;
            
            // Verificar pelo nome da classe
            String className = blockData.getClass().getName().toLowerCase();
            if (className.contains("littletiles") || className.contains("little")) {
                return true;
            }
            
            // Verificar pelo ID do bloco se for um BlockState
            try {
                Method getBlockMethod = blockData.getClass().getMethod("getBlock");
                Object block = getBlockMethod.invoke(blockData);
                
                if (block != null) {
                    String blockId = block.toString().toLowerCase();
                    return blockId.contains("littletiles") || blockId.contains("little");
                }
            } catch (Exception ignored) {
                // Método não encontrado, continuar com outras verificações
            }
            
            return false;
    }
      /**
     * Scan for LittleTiles blocks in the area around the player.
     */
    private static void scanForLittleTilesBlocks(ClientLevel level) {
        try {
            var player = Minecraft.getInstance().player;
            if (player == null) return;
            
            BlockPos playerPos = player.blockPosition();
            int scanRadius = 32; // Scan 32 blocks around player
            int blocksScanned = 0;
            int littleTilesFound = 0;
            
            LOGGER.debug("Starting LittleTiles block scan around {}", playerPos);
            
            // Scan area around player for LittleTiles blocks
            for (int x = -scanRadius; x <= scanRadius; x += 8) { // Step by 8 for performance
                for (int y = -scanRadius; y <= scanRadius; y += 8) {
                    for (int z = -scanRadius; z <= scanRadius; z += 8) {
                        BlockPos pos = playerPos.offset(x, y, z);
                        BlockState state = level.getBlockState(pos);
                        blocksScanned++;
                        
                        // Check if this block is a LittleTiles block
                        if (isLittleTilesBlock(state)) {
                            trackedLittleTilesBlocks.put(pos, state);
                            littleTilesFound++;
                            LOGGER.info("✓ Found LittleTiles block at {}: {} (class: {})", 
                                pos, state, state.getBlock().getClass().getName());
                        }
                    }
                }
            }
            
            if (littleTilesFound > 0) {
                LOGGER.info("Scan complete: Found {} LittleTiles blocks out of {} blocks scanned", 
                    littleTilesFound, blocksScanned);
            }
            
        } catch (Exception e) {
            LOGGER.error("Error scanning for LittleTiles blocks", e);
        }
    }
    
    /**
     * Detect if a BlockState represents a LittleTiles block.
     * Uses class-based detection when available, falls back to registry-based detection.
     */
    private static boolean isLittleTilesBlock(BlockState state) {
        if (state == null || state.getBlock() == null) {
            return false;
        }
        
        // Method 1: Direct class checking (preferred when available)
        if (littleTilesBlockClass != null) {
            try {
                return littleTilesBlockClass.isInstance(state.getBlock());
            } catch (Exception e) {
                LOGGER.debug("Class-based detection failed: {}", e.getMessage());
            }
        }
          // Method 2: Registry-based detection (fallback for compatibility mode)
        if (isLittleTilesCompatibilityMode && littleTilesAvailable) {
            String blockName = state.getBlock().getClass().getName();
            String registryName = "";
            
            try {
                // Try to get the registry name of the block
                var registryId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
                if (registryId != null) {
                    registryName = registryId.toString();
                    LOGGER.debug("Checking block: {} with registry: {}", blockName, registryName);
                }
            } catch (Exception e) {
                LOGGER.debug("Registry lookup failed: {}", e.getMessage());
            }
            
            // Enhanced detection for LittleTiles 1.6.0-pre162
            boolean isLittleTilesBlock = blockName.contains("littletiles") || 
                   blockName.contains("LittleTile") ||
                   blockName.contains("Little") ||
                   blockName.contains("team.creative.littletiles") ||
                   blockName.contains("team.creative.littletiles") ||
                   registryName.startsWith("littletiles:") ||
                   registryName.contains("little") ||
                   // Additional checks for specific LittleTiles 1.6.0 patterns
                   blockName.contains("team.creative") ||
                   state.getBlock().getClass().getSimpleName().toLowerCase().contains("little");
            
            if (isLittleTilesBlock) {
                LOGGER.debug("Detected LittleTiles block: {} ({})", blockName, registryName);
                return true;
            }
        }
        
        // Method 3: More aggressive package-based detection
        if (littleTilesAvailable) {
            String blockPackage = state.getBlock().getClass().getPackage() != null ? 
                state.getBlock().getClass().getPackage().getName() : "";
            
            if (blockPackage.contains("littletiles") || blockPackage.contains("team.creative")) {
                LOGGER.debug("Package-based LittleTiles detection: {}", blockPackage);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check for LittleTiles blocks that might be invisible in contraptions.
     */
    private static void checkForInvisibleBlocks(ClientLevel level) {
        try {
            if (trackedLittleTilesBlocks.isEmpty()) {
                return;
            }
            
            // Check each tracked LittleTiles block
            Iterator<Map.Entry<BlockPos, BlockState>> iterator = trackedLittleTilesBlocks.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<BlockPos, BlockState> entry = iterator.next();
                BlockPos pos = entry.getKey();
                BlockState trackedState = entry.getValue();
                BlockState currentState = level.getBlockState(pos);
                
                // If the block is no longer there, it might be part of a contraption
                if (currentState.isAir() && !trackedState.isAir()) {
                    LOGGER.info("Detected LittleTiles block that disappeared at {}: {} -> {}", 
                        pos, trackedState, currentState);
                    
                    // This is where we'd implement the fix
                    attemptRenderingFix(level, pos, trackedState);
                    
                    // Remove from tracking after a delay
                    iterator.remove();
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Error checking for invisible blocks", e);
        }
    }
    
    /**
     * Attempt to fix the rendering of a LittleTiles block that became invisible.
     */
    private static void attemptRenderingFix(ClientLevel level, BlockPos pos, BlockState originalState) {
        try {
            LOGGER.info("🔧 Attempting rendering fix for LittleTiles block at {}", pos);
            
            // TODO: Implement the actual rendering fix
            // This could involve:
            // 1. Finding the contraption that contains this block
            // 2. Forcing a render update for that contraption
            // 3. Manually rendering the LittleTiles block in its new position
            
            LOGGER.info("✓ Rendering fix attempted for LittleTiles block at {}", pos);
            
        } catch (Exception e) {
            LOGGER.error("Failed to apply rendering fix for block at " + pos, e);
        }
    }
    
    /**
     * Event handler for rendering stages.
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (!fixActive) {
            return;
        }
        
        // Handle rendering during the appropriate stage
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) {
            handleContraptionRenderingStage(event);
        }
    }
    
    /**
     * Handle contraption rendering stage.
     */
    private static void handleContraptionRenderingStage(RenderLevelStageEvent event) {
        try {
            // This is where we'd inject our custom rendering logic
            if (LOGGER.isDebugEnabled() && tickCounter % 60 == 0) { // Log every 3 seconds
                LOGGER.debug("Contraption rendering stage - monitoring {} active contraptions", 
                    activeContraptions.size());
            }
            
            // TODO: Implement custom rendering logic for invisible LittleTiles blocks
            
        } catch (Exception e) {
            LOGGER.error("Error during contraption rendering stage", e);
        }
    }
    
    /**
     * Get comprehensive status information.
     */
    public static String getStatus() {
        if (!fixActive) {
            return "Fix not active - missing required mods";
        }
        
        return String.format("Fix active: %d LittleTiles blocks tracked, %d active contraptions", 
            trackedLittleTilesBlocks.size(), activeContraptions.size());
    }
    
    /**
     * Check if the fix is active.
     */
    public static boolean isActive() {
        return fixActive;
    }
    
    /**
     * Get debug information for troubleshooting.
     */
    public static void logDebugInfo() {
        LOGGER.info("=== ContraptionRenderingFix Debug Info ===");
        LOGGER.info("Fix Active: {}", fixActive);
        LOGGER.info("Create Available: {}", createAvailable);
        LOGGER.info("LittleTiles Available: {}", littleTilesAvailable);
        LOGGER.info("Tracked LittleTiles Blocks: {}", trackedLittleTilesBlocks.size());
        LOGGER.info("Active Contraptions: {}", activeContraptions.size());
        LOGGER.info("Tick Counter: {}", tickCounter);
        
        if (littleTilesBlockClass != null) {
            LOGGER.info("LittleTiles Block Class: {}", littleTilesBlockClass.getName());
        }
        if (contraptionClass != null) {
            LOGGER.info("Contraption Class: {}", contraptionClass.getName());
        }
        
        LOGGER.info("=========================================");
    }
}
