package com.createlittlecontraptions.compat.littletiles;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

// Import the actual LittleTiles classes for structures, groups, rendering, etc.
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.block.little.tile.parent.BlockParentCollection;
import team.creative.littletiles.common.structure.attribute.LittleStructureAttribute;
import team.creative.littletiles.common.block.entity.BETiles;

// Import additional LittleTiles classes for rendering investigation
import team.creative.littletiles.client.render.tile.LittleRenderBox;
// Import for accessing individual tiles
import team.creative.littletiles.common.block.little.tile.LittleTile;

// Import main mod class for logger
import com.createlittlecontraptions.CreateLittleContraptions;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * API Facade for directly parsing and rendering LittleTiles structures from NBT
 * without requiring a fully initialized BETiles instance that interacts with Level.
 * 
 * This implements Gemini's "Direct Structure Rendering" strategy to bypass
 * VirtualRenderWorld limitations while maintaining access to LittleTiles rendering logic.
 */
public class LittleTilesAPIFacade {    private static final Logger LOGGER = LogManager.getLogger("CreateLittleContraptions/LTAPIFacade");    // ULTRA AGGRESSIVE THROTTLING - Reduce logging to absolute minimum
    private static long lastLogTime = 0;
    private static final long LOG_INTERVAL_MS = 5000; // 5 seconds for debugging
    
    // Detailed logs throttling - very restrictive 
    private static long lastDetailedLogTime = 0;
    private static final long DETAILED_LOG_INTERVAL_MS = 10000; // 10 seconds for debugging
    
    // Development debugging logs throttling - extremely restrictive
    private static long lastDevLogTime = 0;
    private static final long DEV_LOG_INTERVAL_MS = 15000; // 15 seconds for debugging
    
    // Per-message throttling for specific repeated messages
    private static long lastRenderingBoxesTime = 0;
    private static long lastPrimaryApproachTime = 0;
    private static long lastBlockParentCollectionTime = 0;
    private static long lastIndividualRenderingTime = 0;
    private static final long PER_MESSAGE_INTERVAL_MS = 8000; // 8 seconds per specific message for debugging
    
    // Debug control flags
    private static boolean debugEnabled = false;
    
    /**
     * Enables or disables debug logging for LittleTiles contraption rendering.
     * 
     * @param enabled true to enable debug logging, false to disable
     */
    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
        if (enabled) {
            LOGGER.info("LittleTiles contraption rendering debug logging ENABLED");
        } else {
            LOGGER.info("LittleTiles contraption rendering debug logging DISABLED");
        }
    }
    
    /**
     * Checks if debug logging is currently enabled.
     * 
     * @return true if debug logging is enabled
     */
    public static boolean isDebugEnabled() {
        return debugEnabled;
    }
    
    /**
     * Logs a debug message if debug logging is enabled.
     * 
     * @param message The debug message to log
     */
    public static void logDebug(String message) {
        if (debugEnabled) {
            LOGGER.debug("[LT-RENDERING] " + message);
        }
    }
    
    /**
     * Logs an error message.
     * 
     * @param message The error message to log
     */
    public static void logError(String message) {
        LOGGER.error("[LT-RENDERING] " + message);
    }
    
    /**
     * Logs an info message.
     * 
     * @param message The info message to log
     */
    public static void logInfo(String message) {
        LOGGER.info("[LT-RENDERING] " + message);
    }
    
    /**
     * Check if enough time has passed to allow logging (throttling)
     */
    private static boolean shouldLog() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLogTime >= LOG_INTERVAL_MS) {
            lastLogTime = currentTime;
            return true;
        }
        return false;
    }
      /**
     * Check if enough time has passed to allow detailed/debug logging (even more restrictive throttling)
     */
    private static boolean shouldLogDetailed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDetailedLogTime >= DETAILED_LOG_INTERVAL_MS) {
            lastDetailedLogTime = currentTime;
            return true;
        }
        return false;
    }
    
    /**
     * Check if enough time has passed to allow development logging (very restrictive - 5 minutes)
     */
    private static boolean shouldLogDev() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDevLogTime >= DEV_LOG_INTERVAL_MS) {
            lastDevLogTime = currentTime;
            return true;
        }
        return false;
    }
    
    /**
     * Specific throttling methods for high-frequency messages
     */
    private static boolean shouldLogRenderingBoxes() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRenderingBoxesTime >= PER_MESSAGE_INTERVAL_MS) {
            lastRenderingBoxesTime = currentTime;
            return true;
        }
        return false;
    }
    
    private static boolean shouldLogPrimaryApproach() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPrimaryApproachTime >= PER_MESSAGE_INTERVAL_MS) {
            lastPrimaryApproachTime = currentTime;
            return true;
        }
        return false;
    }
    
    private static boolean shouldLogBlockParentCollection() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBlockParentCollectionTime >= PER_MESSAGE_INTERVAL_MS) {
            lastBlockParentCollectionTime = currentTime;
            return true;
        }
        return false;
    }
    
    private static boolean shouldLogIndividualRendering() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastIndividualRenderingTime >= PER_MESSAGE_INTERVAL_MS) {
            lastIndividualRenderingTime = currentTime;
            return true;
        }
        return false;
    }
    /**
     * Represents the data needed to render LittleTiles structures
     * This contains the parsed tile collection and grid information from NBT
     */
    public static class ParsedLittleTilesData {
        private final BlockParentCollection tiles;
        private final LittleGrid grid;
        private final BlockPos containerPos;
        private final BlockState containerState;

        public ParsedLittleTilesData(BlockParentCollection tiles, LittleGrid grid, BlockPos containerPos, BlockState containerState) {
            this.tiles = tiles;
            this.grid = grid;
            this.containerPos = containerPos;
            this.containerState = containerState;
        }

        public BlockParentCollection getTiles() { return tiles; }
        public LittleGrid getGrid() { return grid; }
        public BlockPos getContainerPos() { return containerPos; }
        public BlockState getContainerState() { return containerState; }
        
        public boolean isEmpty() {
            return tiles == null || tiles.isCompletelyEmpty();
        }
        
        public Iterable<LittleStructure> getRenderableStructures() {
            if (tiles == null) return List.of();
            return tiles.loadedStructures(LittleStructureAttribute.TICK_RENDERING);
        }
    }    /**
     * Parses the essential rendering information from BETiles NBT.
     * This method AVOIDS calling any Level-dependent methods and focuses on
     * direct deserialization of the tile data structure.
     * 
     * @param tileNBT The CompoundTag from MovementContext.blockEntityData
     * @param containerState The BlockState of the container block
     * @param containerPos The position of the container block
     * @param provider HolderLookup.Provider for NBT parsing
     * @return ParsedLittleTilesData containing the renderable structures, or null if parsing failed
     */
    public static ParsedLittleTilesData parseStructuresFromNBT(CompoundTag tileNBT, BlockState containerState, 
                                                              BlockPos containerPos, HolderLookup.Provider provider) {
        if (tileNBT == null) {
            LOGGER.warn("parseStructuresFromNBT: tileNBT is null.");
            return null;
        }
        LOGGER.debug("parseStructuresFromNBT for state: {}, pos: {}", containerState, containerPos);

        try {
            // Extract the grid information - this is saved directly in the main NBT by BETiles.saveAdditional
            LittleGrid grid;
            try {
                grid = LittleGrid.getOrThrow(tileNBT);
            } catch (Exception e) {
                LOGGER.warn("Failed to extract LittleGrid from NBT for {}: {}", containerPos, e.getMessage());
                grid = LittleGrid.MIN; // Fallback to minimum grid
            }

            // Extract the "content" tag - this contains the actual tile data
            // Based on BETiles.saveAdditional: nbt.put("content", tiles.save(new LittleServerFace(this), provider))
            if (!tileNBT.contains("content", Tag.TAG_COMPOUND)) {
                LOGGER.warn("NBT for {} at {} does not contain 'content' tag", containerState, containerPos);
                return null;
            }

            CompoundTag contentNBT = tileNBT.getCompound("content");
            if (contentNBT.isEmpty()) {
                LOGGER.warn("Content NBT for {} at {} is empty", containerState, containerPos);
                return null;
            }            // Create a minimal BlockParentCollection WITH a BETiles instance for rendering
            // The issue was that we were creating it with null BETiles, making BERenderManager inaccessible
            
            BlockParentCollection tiles;
            try {
                // Create a temporary BETiles instance for rendering purposes
                // We need to use reflection to avoid compile dependencies
                Class<?> beTilesClass = Class.forName("team.creative.littletiles.common.block.entity.BETiles");
                java.lang.reflect.Constructor<?> constructor = beTilesClass.getDeclaredConstructor(
                    net.minecraft.core.BlockPos.class, 
                    net.minecraft.world.level.block.state.BlockState.class
                );
                Object tempBETiles = constructor.newInstance(containerPos, containerState);
                
                LOGGER.warn("[CLC/LTAPIFacade] Created temporary BETiles for rendering: {}", tempBETiles.getClass().getName());
                
                // Cast tempBETiles to the proper type for the constructor
                tiles = new BlockParentCollection((team.creative.littletiles.common.block.entity.BETiles) tempBETiles, true); // true = client side for rendering
                
            } catch (Exception e) {
                LOGGER.warn("[CLC/LTAPIFacade] Failed to create BETiles, falling back to null: {}", e.getMessage());
                tiles = new BlockParentCollection(null, false); // fallback to original approach
            }
            
            // Load the content directly into the tiles collection
            // Based on BETiles.loadAdditional: tiles.load(nbt.getCompound("content"), provider)
            if (provider != null) {
                tiles.load(contentNBT, provider);
            } else {
                // Try to load without provider - this might work for basic tiles
                LOGGER.warn("Loading tiles without HolderLookup.Provider - some tiles may not load correctly");
                try {
                    // Get a client-side registry access if available
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.level != null) {
                        tiles.load(contentNBT, mc.level.registryAccess());
                    } else {
                        LOGGER.error("Cannot load tiles: no client level available and no provider given");
                        return null;
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to load tiles with client registry access: {}", e.getMessage());
                    return null;
                }
            }
            
            LOGGER.info("Successfully parsed LittleTiles data from NBT for {} - Grid: {}, Tiles count: {}", 
                       containerPos, grid, tiles.totalSize());
            
            return new ParsedLittleTilesData(tiles, grid, containerPos, containerState);

        } catch (Exception e) {
            LOGGER.error("Error parsing LittleTiles structures from NBT for {} at {}: {}", containerState, containerPos, e.getMessage(), e);
            return null;
        }
    }    /**
     * Renders the parsed LittleTiles structures using LittleTiles' own rendering logic.
     * Based on Gemini's guidance to investigate BlockParentCollection.render() and LittleRenderBox methods.
     */
    public static void renderDirectly(ParsedLittleTilesData parsedData, PoseStack poseStack, MultiBufferSource bufferSource, 
                                      int combinedLight, int combinedOverlay, float partialTicks) {
        if (parsedData == null || parsedData.isEmpty()) {
            return;
        }

        BlockParentCollection tiles = parsedData.getTiles();
        LittleGrid grid = parsedData.getGrid();
        BlockPos containerPos = parsedData.getContainerPos();

        if (tiles == null) {
            LOGGER.warn("renderDirectly: BlockParentCollection is null for {}. Cannot render.", containerPos);
            return;
        }        // Only log this method call once every 10 seconds to avoid spam
        if (shouldLog()) {
            LOGGER.info("[CLC/LTAPIFacade] Attempting to render BlockParentCollection for {} (Grid: {}, Size: {} tiles)", 
                        containerPos, grid, tiles.totalSize());
        }poseStack.pushPose();
        
        // --- PRIMARY APPROACH: Find and call render method on BlockParentCollection ---        // Based on Gemini's analysis, this should have a render method like:
        // tiles.render(poseStack, bufferSource, combinedLight, combinedOverlay, partialTicks, grid);
        
        if (shouldLog()) {
            LOGGER.info("[CLC/LTAPIFacade] Investigating BlockParentCollection render methods...");
            investigateRenderMethods(tiles, "BlockParentCollection");
            
            // --- SECONDARY APPROACH: Investigate LittleRenderBox for individual tile rendering ---
            LOGGER.info("[CLC/LTAPIFacade] Investigating LittleRenderBox static methods...");
            investigateLittleRenderBox();
        }
        // --- ATTEMPT ACTUAL RENDERING ---
        boolean renderedSomething = false;
        
        // Try to call the render method on BlockParentCollection
        try {
            // Look for common method patterns from BETiles renderer:
            // Method 1: tiles.render(pose, source, light, overlay, partialTicks)
            Method renderMethod = findRenderMethod(tiles.getClass(), 
                "render", PoseStack.class, MultiBufferSource.class, int.class, int.class, float.class);
              if (renderMethod != null) {
                if (shouldLog()) {
                    LOGGER.info("[CLC/LTAPIFacade] Found BlockParentCollection.render method with 5 params, calling it...");
                }
                renderMethod.invoke(tiles, poseStack, bufferSource, combinedLight, combinedOverlay, partialTicks);
                renderedSomething = true;
                if (shouldLog()) {
                    LOGGER.info("[CLC/LTAPIFacade] Successfully called tiles.render() for {}", containerPos);
                }
            } else {
                // Try Method 2: with grid parameter
                renderMethod = findRenderMethod(tiles.getClass(), 
                    "render", PoseStack.class, LittleGrid.class, MultiBufferSource.class, int.class, int.class);
                  if (renderMethod != null) {
                    if (shouldLog()) {
                        LOGGER.info("[CLC/LTAPIFacade] Found BlockParentCollection.render method with grid param, calling it...");
                    }
                    renderMethod.invoke(tiles, poseStack, grid, bufferSource, combinedLight, combinedOverlay);
                    renderedSomething = true;
                    if (shouldLog()) {
                        LOGGER.info("[CLC/LTAPIFacade] Successfully called tiles.render(grid) for {}", containerPos);
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.warn("[CLC/LTAPIFacade] Failed to call BlockParentCollection.render(): {}", e.getMessage());
        }
        
        // --- FALLBACK: Try individual tile rendering with LittleRenderBox ---
        boolean hasTiles = false;
        try {
            hasTiles = tiles.allTiles().iterator().hasNext();
        } catch (Exception e) {
            LOGGER.warn("[CLC/LTAPIFacade] Error checking if tiles collection has elements: {}", e.getMessage());
        }          if (!renderedSomething && hasTiles) {            if (shouldLog()) {
                LOGGER.info("[CLC/LTAPIFacade] Attempting individual tile rendering via LittleRenderBox...");
                
                // === NOVA ABORDAGEM: Usar virtual BETiles em vez de diagnóstico ===
                LOGGER.info("[CLC/LTAPIFacade] Using new virtual BETiles approach instead of problematic BlockParentCollection");
            }
              try {                // Try the corrected BERenderManager approach first
                if (shouldLog()) {
                    LOGGER.info("[CLC/LTAPIFacade] Trying BERenderManager.getRenderingBoxes approach (correct method location)...");
                }
                renderedSomething = attemptBERenderManagerApproach(tiles, poseStack, bufferSource, combinedLight, combinedOverlay, partialTicks, grid);
                
                // If that fails, try the original approach with better debugging
                if (!renderedSomething && shouldLog()) {
                    LOGGER.info("[CLC/LTAPIFacade] LittleTile.getRenderingBoxes approach failed, trying original getRenderingBox approach with enhanced debugging...");
                    renderedSomething = attemptIndividualTileRendering(tiles, poseStack, bufferSource, combinedLight, combinedOverlay, partialTicks, grid);
                } else if (!renderedSomething) {
                    // Run without logging to avoid spam
                    renderedSomething = attemptIndividualTileRendering(tiles, poseStack, bufferSource, combinedLight, combinedOverlay, partialTicks, grid);
                }
                
            } catch (Exception e) {
                LOGGER.error("[CLC/LTAPIFacade] Error during individual tile rendering: {}", e.getMessage(), e);
            }
        }
        
        if (!renderedSomething) {
            if (shouldLog()) {
                LOGGER.warn("[CLC/LTAPIFacade] No rendering method succeeded for {}. LittleTiles may remain invisible.", containerPos);
            }
        }
        
        poseStack.popPose();
    }
      /**
     * Investigates available render methods on the given object using reflection
     */
    private static void investigateRenderMethods(Object object, String objectType) {
        // Only investigate and log if throttling allows it
        if (!shouldLog()) return;
        
        Class<?> clazz = object.getClass();
        LOGGER.info("[CLC/LTAPIFacade] === {} Method Investigation ===", objectType);
        LOGGER.info("[CLC/LTAPIFacade] Class: {}", clazz.getName());
        
        Method[] methods = clazz.getMethods();
        int renderMethodCount = 0;
        
        for (Method method : methods) {
            if (method.getName().toLowerCase().contains("render")) {
                renderMethodCount++;
                Class<?>[] params = method.getParameterTypes();
                StringBuilder paramStr = new StringBuilder();
                for (int i = 0; i < params.length; i++) {
                    if (i > 0) paramStr.append(", ");
                    paramStr.append(params[i].getSimpleName());
                }
                LOGGER.info("[CLC/LTAPIFacade] Render method #{}: {}({}) returns {}", 
                           renderMethodCount, method.getName(), paramStr.toString(), method.getReturnType().getSimpleName());
            }
        }
        
        if (renderMethodCount == 0) {
            LOGGER.warn("[CLC/LTAPIFacade] No render methods found in {}", objectType);
        }
        
        // Also check superclasses
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && !superClass.equals(Object.class)) {
            LOGGER.info("[CLC/LTAPIFacade] Checking superclass: {}", superClass.getSimpleName());
            // Don't recurse infinitely, just check one level up
        }
    }
    
    /**
     * Investigates LittleRenderBox static methods
     */    private static void investigateLittleRenderBox() {
        // Only investigate and log if throttling allows it
        if (!shouldLog()) return;
        
        try {
            Class<?> renderBoxClass = LittleRenderBox.class;
            LOGGER.info("[CLC/LTAPIFacade] === LittleRenderBox Static Method Investigation ===");
            LOGGER.info("[CLC/LTAPIFacade] Class: {}", renderBoxClass.getName());
            
            Method[] methods = renderBoxClass.getMethods();
            int staticRenderMethods = 0;
            
            for (Method method : methods) {
                if (java.lang.reflect.Modifier.isStatic(method.getModifiers()) && 
                    method.getName().toLowerCase().contains("render")) {
                    staticRenderMethods++;
                    Class<?>[] params = method.getParameterTypes();
                    StringBuilder paramStr = new StringBuilder();
                    for (int i = 0; i < params.length; i++) {
                        if (i > 0) paramStr.append(", ");
                        paramStr.append(params[i].getSimpleName());
                    }
                    LOGGER.info("[CLC/LTAPIFacade] Static render method #{}: {}({}) returns {}", 
                               staticRenderMethods, method.getName(), paramStr.toString(), method.getReturnType().getSimpleName());
                }
            }
            
            if (staticRenderMethods == 0) {
                LOGGER.warn("[CLC/LTAPIFacade] No static render methods found in LittleRenderBox");
            }
            
        } catch (Exception e) {
            LOGGER.error("[CLC/LTAPIFacade] Error investigating LittleRenderBox: {}", e.getMessage());
        }
    }
    
    /**
     * Attempts to find a specific render method by name and parameter types
     */
    private static Method findRenderMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }    /**
     * Attempts to render individual tiles using the discovered getRenderingBox method
     */
    private static boolean attemptIndividualTileRendering(BlockParentCollection tiles, PoseStack poseStack, 
                                                         MultiBufferSource bufferSource, int combinedLight, 
                                                         int combinedOverlay, float partialTicks, LittleGrid grid) {        boolean renderedSomething = false;
        int tileCount = 0;
        
        if (shouldLogIndividualRendering()) {
            LOGGER.info("[CLC/LTAPIFacade] Starting individual tile rendering...");
        }
        
        // === GEMINI'S PRIMARY RECOMMENDATION: Try BlockParentCollection.render() FIRST ===
        // This is based on the PDF Listing 10: be.mainGroup.render(pose, source, light, overlay, partialTicks);
        // The 'tiles' object IS a BlockParentCollection, which should be equivalent to 'be.mainGroup'
        try {
            if (shouldLogPrimaryApproach()) {
                LOGGER.info("[CLC/LTAPIFacade] === Trying Gemini's Primary Approach: BlockParentCollection.render() ===");
            }
            if (shouldLogDev()) {
                if (shouldLogBlockParentCollection()) {
                    LOGGER.info("[CLC/LTAPIFacade] tiles object type: {}", tiles.getClass().getName());
                }
            }
            
            // Look for: render(PoseStack, MultiBufferSource, int, int, float)
            Method mainRenderMethod = tiles.getClass().getMethod("render", 
                PoseStack.class, MultiBufferSource.class, int.class, int.class, float.class);
            
            LOGGER.info("[CLC/LTAPIFacade] Found render method: {}", mainRenderMethod);
            mainRenderMethod.invoke(tiles, poseStack, bufferSource, combinedLight, combinedOverlay, partialTicks);
            LOGGER.info("[CLC/LTAPIFacade] *** SUCCESS: Invoked tiles.render(PoseStack, MultiBufferSource, int, int, float) ***");
            LOGGER.info("[CLC/LTAPIFacade] Check game visuals now - this should have rendered ALL tiles in the collection!");
            return true; // If this works, it renders everything at once!
              } catch (NoSuchMethodException e) {
            if (shouldLogDetailed()) {
                LOGGER.warn("[CLC/LTAPIFacade] Could not find render(PoseStack, MultiBufferSource, int, int, float) method on BlockParentCollection: {}", tiles.getClass().getName());
                LOGGER.info("[CLC/LTAPIFacade] Available methods on {}:", tiles.getClass().getName());
                for (Method method : tiles.getClass().getMethods()) {
                    if (method.getName().contains("render")) {
                        LOGGER.info("[CLC/LTAPIFacade]   - {}", method);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("[CLC/LTAPIFacade] Error invoking main render method on BlockParentCollection: ", e);
        }
        
        // === FALLBACK: Try individual tile rendering (original approach) ===
        if (shouldLogDev()) {
            LOGGER.info("[CLC/LTAPIFacade] === Falling back to individual tile rendering approach ===");
        }
        
        try {
            // === PRIMARY APPROACH: Try BlockParentCollection.render() directly ===
            // Based on Gemini's analysis and Listing 10: be.mainGroup.render(pose, source, light, overlay, partialTicks)
            // This should be the most efficient approach, rendering all tiles in the collection at once
            
            if (shouldLogDev()) {
                LOGGER.info("[CLC/LTAPIFacade] Attempting BlockParentCollection.render() method (Gemini's primary recommendation)...");
            }
            try {
                Method mainRenderMethod = tiles.getClass().getMethod("render", 
                    PoseStack.class, MultiBufferSource.class, int.class, int.class, float.class);
                
                LOGGER.info("[CLC/LTAPIFacade] Found tiles.render() method, invoking...");
                mainRenderMethod.invoke(tiles, poseStack, bufferSource, combinedLight, combinedOverlay, partialTicks);
                LOGGER.info("[CLC/LTAPIFacade] SUCCESS: tiles.render() completed for BlockParentCollection class: {}", tiles.getClass().getName());
                return true; // If successful, we're done - all tiles rendered at once
                
            } catch (NoSuchMethodException e) {                if (shouldLogDetailed()) {
                    LOGGER.warn("[CLC/LTAPIFacade] BlockParentCollection.render() method not found on class: {}", tiles.getClass().getName());
                    LOGGER.info("[CLC/LTAPIFacade] Falling back to individual tile rendering via getRenderingBox...");
                }
            } catch (Exception e) {
                LOGGER.error("[CLC/LTAPIFacade] Error invoking BlockParentCollection.render(): {}", e.getMessage());
                LOGGER.info("[CLC/LTAPIFacade] Falling back to individual tile rendering via getRenderingBox...");
            }
            
            // === FALLBACK APPROACH: Individual tile rendering via getRenderingBox ===
            if (shouldLogDev()) {
                if (shouldLogIndividualRendering()) {
                    LOGGER.info("[CLC/LTAPIFacade] Starting individual tile rendering using getRenderingBox method...");
                }
            }
            try {
                // Get the getRenderingBox method discovered during reflection investigation
                Method getRenderingBoxMethod = null;
                Class<?> boxClass = null;
                
                for (Method method : tiles.getClass().getMethods()) {
                    if (method.getName().equals("getRenderingBox") && method.getParameterCount() == 3) {
                        getRenderingBoxMethod = method;
                        Class<?>[] paramTypes = method.getParameterTypes();
                        boxClass = paramTypes[1]; // The second parameter should be the box type
                        if (shouldLogDev()) {
                            LOGGER.info("[CLC/LTAPIFacade] Found getRenderingBox method with box type: {}", boxClass.getName());
                        }
                        break;
                    }
                }
                
                if (getRenderingBoxMethod == null) {
                    LOGGER.error("[CLC/LTAPIFacade] getRenderingBox method not found");
                    return false;
                }
                
                // Define render types to try
                net.minecraft.client.renderer.RenderType[] renderTypes = {
                    net.minecraft.client.renderer.RenderType.solid(),
                    net.minecraft.client.renderer.RenderType.cutout(),
                    net.minecraft.client.renderer.RenderType.cutoutMipped(),
                    net.minecraft.client.renderer.RenderType.translucent()
                };                // Iterate through all tiles and attempt to render each one
                for (var tilePair : tiles.allTiles()) {
                    if (tilePair != null && tilePair.value != null) {
                        tileCount++;
                        LittleTile tile = tilePair.value;                        // CRITICAL FIX: Extract individual LittleBox instances from each LittleTile
                        // The log shows tiles contain multiple boxes: "[0,0,0 -> 5,16,16], [5,4,1 -> 10,16,16]..."
                        // We need to iterate over each individual box and render them separately
                        
                        if (shouldLogDetailed()) {
                            LOGGER.info("[CLC/LTAPIFacade] === Box Extraction Debug for Tile #{} ===", tileCount);
                            LOGGER.info("[CLC/LTAPIFacade] Tile class: {}", tile.getClass().getName());
                            LOGGER.info("[CLC/LTAPIFacade] Tile string representation: {}", tile.toString());
                        }
                        
                        // Try to find a collection or iterable of LittleBox instances within the tile
                        java.util.List<Object> individualBoxes = new java.util.ArrayList<>();
                        
                        try {
                            // Method 1: Look for boxes collection/iterable
                            String[] boxCollectionMethods = {"getBoxes", "boxes", "getAllBoxes", "iterator", "stream"};
                            for (String methodName : boxCollectionMethods) {
                                try {
                                    Method method = tile.getClass().getMethod(methodName);
                                    Object result = method.invoke(tile);                                    if (result != null) {
                                        if (shouldLogDetailed()) {
                                            LOGGER.info("[CLC/LTAPIFacade] Method {} returned: {} (type: {})", 
                                                methodName, result.toString(), result.getClass().getName());
                                        }
                                        
                                        // Check if it's iterable
                                        if (result instanceof Iterable) {
                                            if (shouldLogDetailed()) {
                                                LOGGER.info("[CLC/LTAPIFacade] Found iterable collection via {}", methodName);
                                            }
                                            for (Object box : (Iterable<?>) result) {
                                                if (box != null && box.getClass().getName().contains("LittleBox")) {
                                                    individualBoxes.add(box);
                                                    LOGGER.debug("[CLC/LTAPIFacade] Added box: {}", box);
                                                }
                                            }
                                            if (!individualBoxes.isEmpty()) {
                                                if (shouldLogDetailed()) {
                                                    LOGGER.info("[CLC/LTAPIFacade] Successfully extracted {} boxes via {}", 
                                                        individualBoxes.size(), methodName);
                                                }
                                                break;
                                            }
                                        }
                                        
                                        // Check if it's a single LittleBox
                                        if (result.getClass().getName().contains("LittleBox")) {
                                            individualBoxes.add(result);
                                            if (shouldLogDetailed()) {
                                                LOGGER.info("[CLC/LTAPIFacade] Found single box via {}", methodName);
                                            }
                                            break;
                                        }
                                    }
                                } catch (NoSuchMethodException nsme) {
                                    // Method doesn't exist, try next
                                } catch (Exception e) {
                                    LOGGER.debug("[CLC/LTAPIFacade] Method {} failed: {}", methodName, e.getMessage());
                                }
                            }
                              // Method 2: Try accessing the 'boxes' field directly if methods failed
                            if (individualBoxes.isEmpty()) {
                                if (shouldLogDetailed()) {
                                    LOGGER.info("[CLC/LTAPIFacade] No box collection method worked, trying fields...");
                                }
                                try {
                                    java.lang.reflect.Field boxesField = tile.getClass().getDeclaredField("boxes");
                                    boxesField.setAccessible(true);
                                    Object boxesValue = boxesField.get(tile);
                                    
                                    if (boxesValue != null) {
                                        if (shouldLogDetailed()) {
                                            LOGGER.info("[CLC/LTAPIFacade] Found 'boxes' field: {} (type: {})", 
                                                boxesValue, boxesValue.getClass().getName());
                                        }
                                        
                                        if (boxesValue instanceof Iterable) {
                                            for (Object box : (Iterable<?>) boxesValue) {
                                                if (box != null && box.getClass().getName().contains("LittleBox")) {
                                                    individualBoxes.add(box);
                                                }
                                            }
                                            if (shouldLogDetailed()) {
                                                LOGGER.info("[CLC/LTAPIFacade] Extracted {} boxes from 'boxes' field", individualBoxes.size());
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    LOGGER.debug("[CLC/LTAPIFacade] Field access failed: {}", e.getMessage());
                                }
                            }
                              // Method 3: If we still have no boxes, examine the tile's superclass or parent collection
                            if (individualBoxes.isEmpty()) {
                                if (shouldLogDetailed()) {
                                    LOGGER.warn("[CLC/LTAPIFacade] Could not extract individual boxes from LittleTile directly");
                                    LOGGER.info("[CLC/LTAPIFacade] Attempting to use tilePair.key as fallback, but this may cause type mismatch");
                                    // We'll skip this tile rather than cause a type mismatch
                                    LOGGER.warn("[CLC/LTAPIFacade] Skipping tile #{} due to box extraction failure", tileCount);
                                }
                                continue; // Skip this tile
                            }
                            
                        } catch (Exception e) {
                            LOGGER.error("[CLC/LTAPIFacade] Exception during box collection extraction: {}", e.getMessage());
                            continue; // Skip this tile
                        }
                          // Now iterate over each individual box and render it
                        for (int boxIndex = 0; boxIndex < individualBoxes.size(); boxIndex++) {
                            Object individualBox = individualBoxes.get(boxIndex);
                            
                            // Only log detailed processing info when throttling allows
                            if (shouldLogDetailed()) {
                                LOGGER.info("[CLC/LTAPIFacade] Processing tile #{}, box #{}: {} (type: {})", 
                                    tileCount, boxIndex + 1, individualBox, individualBox.getClass().getName());
                            }
                            
                            // Try each render type until one works for this individual box
                            for (net.minecraft.client.renderer.RenderType renderType : renderTypes) {
                                try {                                    // Only log detailed debug info once per minute to avoid spam
                                    if (shouldLogDetailed()) {
                                        LOGGER.info("[CLC/LTAPIFacade] === getRenderingBox Call Debug ===");
                                        LOGGER.info("[CLC/LTAPIFacade] Calling getRenderingBox with:");
                                        LOGGER.info("[CLC/LTAPIFacade]   tiles: {} (type: {})", tiles, tiles.getClass().getName());
                                        LOGGER.info("[CLC/LTAPIFacade]   tile: {} (type: {})", tile, tile.getClass().getName());
                                        LOGGER.info("[CLC/LTAPIFacade]   individualBox: {} (type: {})", individualBox, individualBox.getClass().getName());
                                        LOGGER.info("[CLC/LTAPIFacade]   renderType: {}", renderType);
                                        LOGGER.info("[CLC/LTAPIFacade] Expected signature: getRenderingBox(LittleTile, LittleBox, RenderType)");
                                        // === GEMINI'S CRITICAL DEBUG: Log method declaring class ===
                                        LOGGER.info("[CLC/LTAPIFacade] tiles object class: {}", tiles.getClass().getName());
                                        LOGGER.info("[CLC/LTAPIFacade] getRenderingBoxMethod DECLARED BY: {}", getRenderingBoxMethod.getDeclaringClass().getName());
                                    }
                                    
                                    // Call tiles.getRenderingBox(tile, individualBox, renderType)
                                    Object result = getRenderingBoxMethod.invoke(tiles, tile, individualBox, renderType);
                                    
                                    // === GEMINI'S DEBUG RECOMMENDATION: Log actual return type ===
                                    if (result != null) {
                                        if (shouldLogDetailed()) {
                                            LOGGER.info("[CLC/LTAPIFacade] getRenderingBox for tile {} (box type {}), renderType {} returned: {} (Class: {})", 
                                                tile.getClass().getSimpleName(), individualBox.getClass().getSimpleName(), renderType, result, result.getClass().getName());
                                        }
                                        // Now we have a LittleRenderBox, let's try to render it
                                        if (renderLittleRenderBox(result, poseStack, bufferSource, combinedLight, combinedOverlay, renderType, tiles, tile)) {
                                            renderedSomething = true;
                                            if (shouldLog()) {
                                                LOGGER.info("[CLC/LTAPIFacade] Successfully rendered tile #{}, box #{} with renderType {}", tileCount, boxIndex + 1, renderType);
                                            }
                                            break; // Move to next renderType or box
                                        } else {
                                            if (shouldLogDetailed()) {
                                                LOGGER.info("[CLC/LTAPIFacade] Failed to render tile #{}, box #{} with renderType {}", tileCount, boxIndex + 1, renderType);
                                            }
                                        }
                                    } else {
                                        if (shouldLogDetailed()) {
                                            LOGGER.debug("[CLC/LTAPIFacade] getRenderingBox for tile {} (box type {}), renderType {} returned NULL.", 
                                                tile.getClass().getSimpleName(), individualBox.getClass().getSimpleName(), renderType);
                                        }
                                    }
                                      } catch (Exception e) {
                                    if (shouldLogDetailed()) {
                                        LOGGER.warn("[CLC/LTAPIFacade] Failed to get/render box for tile #{}, box #{}, renderType {}: {}", tileCount, boxIndex + 1, renderType, e.getMessage());
                                    }
                                }
                            }
                        } // End of individual box iteration
                    }
                }
                
                if (shouldLogDev()) {
                    LOGGER.info("[CLC/LTAPIFacade] Processed {} individual tiles, rendered: {}", tileCount, renderedSomething);
                }
                
            } catch (Exception e) {
                LOGGER.error("[CLC/LTAPIFacade] Error during individual tile rendering: {}", e.getMessage(), e);
            }
            
        } catch (Exception e) {
            LOGGER.error("[CLC/LTAPIFacade] Error during tile rendering attempt: {}", e.getMessage(), e);
        }        
        return renderedSomething;
    }
    
    /**
     * Enhanced method to render a LittleRenderBox using Gemini's recommended patterns
     */
    private static boolean renderLittleRenderBox(Object renderBoxInstance, PoseStack poseStack, 
                                                 MultiBufferSource bufferSource, int combinedLight, 
                                                 int combinedOverlay, net.minecraft.client.renderer.RenderType renderType,
                                                 BlockParentCollection parentCollection, LittleTile tileItself) {
        try {
            // === OPTION A: Does the renderBoxInstance itself have a render/buffer method? ===
            try {
                // Attempt to find a method like "renderToBuffer" or "buffer" as Gemini suggested
                Method[] possibleMethods = {
                    findBufferMethod(renderBoxInstance.getClass(), "renderToBuffer", 
                        PoseStack.Pose.class, com.mojang.blaze3d.vertex.VertexConsumer.class, 
                        int.class, int.class, float.class, float.class, float.class, float.class),
                    findBufferMethod(renderBoxInstance.getClass(), "buffer", 
                        com.mojang.blaze3d.vertex.VertexConsumer.class, PoseStack.Pose.class),
                    findBufferMethod(renderBoxInstance.getClass(), "tessellate", 
                        com.mojang.blaze3d.vertex.VertexConsumer.class),
                    findBufferMethod(renderBoxInstance.getClass(), "addQuads",
                        com.mojang.blaze3d.vertex.VertexConsumer.class)
                };
                
                VertexConsumer consumer = bufferSource.getBuffer(renderType);
                  for (Method method : possibleMethods) {
                    if (method != null) {
                        try {
                            if (shouldLogDetailed()) {
                                LOGGER.info("[CLC/LTAPIFacade] Trying render method: {} on class {}", method.getName(), renderBoxInstance.getClass().getName());
                            }
                            
                            if (method.getName().equals("renderToBuffer") && method.getParameterCount() == 8) {
                                method.invoke(renderBoxInstance, poseStack.last(), consumer, combinedLight, combinedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
                                if (shouldLog()) {
                                    LOGGER.info("[CLC/LTAPIFacade] Successfully called renderToBuffer on {}", renderBoxInstance.getClass().getName());
                                }
                                return true;
                            } else if (method.getName().equals("buffer") && method.getParameterCount() == 2) {
                                method.invoke(renderBoxInstance, consumer, poseStack.last());
                                if (shouldLog()) {
                                    LOGGER.info("[CLC/LTAPIFacade] Successfully called buffer on {}", renderBoxInstance.getClass().getName());
                                }
                                return true;
                            } else if (method.getName().equals("tessellate") && method.getParameterCount() == 1) {
                                method.invoke(renderBoxInstance, consumer);
                                if (shouldLog()) {
                                    LOGGER.info("[CLC/LTAPIFacade] Successfully called tessellate on {}", renderBoxInstance.getClass().getName());
                                }
                                return true;
                            } else if (method.getName().equals("addQuads") && method.getParameterCount() == 1) {
                                method.invoke(renderBoxInstance, consumer);
                                if (shouldLog()) {
                                    LOGGER.info("[CLC/LTAPIFacade] Successfully called addQuads on {}", renderBoxInstance.getClass().getName());
                                }
                                return true;
                            }
                            
                        } catch (Exception e) {
                            if (shouldLogDetailed()) {
                                LOGGER.info("[CLC/LTAPIFacade] Render method {} failed: {}", method.getName(), e.getMessage());
                            }
                        }
                    }
                }
                
            } catch (Exception e) {
                LOGGER.error("[CLC/LTAPIFacade] Error attempting direct renderBox methods: {}", e.getMessage());
            }
            
            // === OPTION B: Use parent collection render method (as suggested by Gemini) ===
            if (parentCollection != null) {
                try {
                    Method parentRenderMethod = findBufferMethod(parentCollection.getClass(), "render", 
                        PoseStack.class, MultiBufferSource.class, int.class, int.class, float.class);
                    
                    if (parentRenderMethod != null) {
                        parentRenderMethod.invoke(parentCollection, poseStack, bufferSource, combinedLight, combinedOverlay, 0.0f);
                        LOGGER.info("[CLC/LTAPIFacade] Successfully called render on parentCollection {}", parentCollection.getClass().getName());
                        return true;
                    }
                    
                } catch (Exception e) {
                    LOGGER.error("[CLC/LTAPIFacade] Error invoking render on parentCollection {}: {}", parentCollection.getClass().getName(), e.getMessage());
                }
            }
            
            LOGGER.warn("[CLC/LTAPIFacade] All rendering attempts failed for renderBoxInstance type {} and parentCollection type {}", 
                renderBoxInstance != null ? renderBoxInstance.getClass().getName() : "null",
                parentCollection != null ? parentCollection.getClass().getName() : "null");
            return false;
            
        } catch (Exception e) {
            LOGGER.error("[CLC/LTAPIFacade] Error in renderLittleRenderBox: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Helper method to find buffer/render methods with specific signatures
     */    private static Method findBufferMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Gets the collision shape for the parsed LittleTiles structures.
     * 
     * @param parsedData The data returned by parseStructuresFromNBT
     * @return VoxelShape representing the collision shape of all tiles
     */
    public static VoxelShape getCollisionShape(ParsedLittleTilesData parsedData) {
        if (parsedData == null || parsedData.isEmpty()) {
            return Shapes.empty();
        }
        
        try {
            // TODO: Implement collision shape calculation from parsed tile data
            // This would involve iterating through all tiles and combining their collision shapes
            // For now, return a default block shape as a placeholder
            LOGGER.debug("getCollisionShape called for {} - using placeholder implementation", parsedData.getContainerPos());
            return Shapes.block(); // Placeholder - should calculate actual shape from tiles
            
        } catch (Exception e) {
            LOGGER.error("Error getting collision shape for {}: {}", parsedData.getContainerPos(), e.getMessage(), e);
            return Shapes.block(); // Fallback
        }
    }    /**
     * Fixed approach using BERenderManager.getRenderingBoxes() - the correct method location
     * Based on analysis showing that getRenderingBoxes exists on BERenderManager, not on LittleTile
     */
    private static boolean attemptBERenderManagerApproach(BlockParentCollection tiles, PoseStack poseStack, 
                                                          MultiBufferSource bufferSource, int combinedLight, 
                                                          int combinedOverlay, float partialTicks, LittleGrid grid) {
        boolean renderedSomething = false;
        
        if (shouldLogRenderingBoxes()) {
            LOGGER.info("[CLC/LTAPIFacade] === Using BERenderManager.getRenderingBoxes() (Correct Approach) ===");
        }
        
        try {
            // Try to access the BETiles and BERenderManager that should contain the rendering data
            // The tiles BlockParentCollection should be associated with a BETiles instance
              Object beTiles = null;
            Object renderManager = null;
            
            LOGGER.warn("[CLC/LTAPIFacade] Starting BETiles search - tiles class: {}", tiles != null ? tiles.getClass().getName() : "null");
            
            // Method 1: Try to get BETiles from the parent collection
            try {
                // Look for methods that might give us access to the BETiles
                Method getBeTilesMethod = findBufferMethod(tiles.getClass(), "getBe");
                if (getBeTilesMethod == null) {
                    getBeTilesMethod = findBufferMethod(tiles.getClass(), "getBlockEntity");
                }
                if (getBeTilesMethod == null) {
                    getBeTilesMethod = findBufferMethod(tiles.getClass(), "getTiles");
                }
                  if (getBeTilesMethod != null) {
                    LOGGER.warn("[CLC/LTAPIFacade] Found getBeTiles method: {} - attempting invoke", getBeTilesMethod.getName());
                    beTiles = getBeTilesMethod.invoke(tiles);
                    LOGGER.warn("[CLC/LTAPIFacade] Invoke result - beTiles: {} (type: {})", 
                        beTiles, beTiles != null ? beTiles.getClass().getName() : "null");
                } else {
                    LOGGER.warn("[CLC/LTAPIFacade] No getBeTiles method found via standard names");
                }            } catch (Exception e) {
                LOGGER.warn("[CLC/LTAPIFacade] Could not access BETiles via direct method: {}", e.getMessage(), e);
            }
            
            LOGGER.warn("[CLC/LTAPIFacade] After Method 1 - beTiles: {}", beTiles != null ? beTiles.getClass().getName() : "null");
            
            // Method 2: Try reflection to access private/protected fields
            if (beTiles == null) {
                LOGGER.warn("[CLC/LTAPIFacade] Attempting Method 2 - reflection field access");
                try {
                    java.lang.reflect.Field[] fields = tiles.getClass().getDeclaredFields();
                    for (java.lang.reflect.Field field : fields) {
                        if (field.getType().getName().contains("BETiles") || 
                            field.getName().toLowerCase().contains("be") || 
                            field.getName().toLowerCase().contains("tiles")) {
                            field.setAccessible(true);
                            Object fieldValue = field.get(tiles);
                            if (fieldValue != null && fieldValue.getClass().getName().contains("BETiles")) {
                                beTiles = fieldValue;
                                LOGGER.info("[CLC/LTAPIFacade] Found BETiles via field {}: {} (type: {})", 
                                    field.getName(), beTiles, beTiles.getClass().getName());
                                break;
                            }
                        }
                    }                } catch (Exception e) {
                    LOGGER.warn("[CLC/LTAPIFacade] Could not access BETiles via reflection: {}", e.getMessage(), e);
                }
            }
            
            LOGGER.warn("[CLC/LTAPIFacade] After Method 2 - beTiles: {}", beTiles != null ? beTiles.getClass().getName() : "null");
              // Method 3: Get BERenderManager from BETiles
            if (beTiles != null) {
                LOGGER.warn("[CLC/LTAPIFacade] METHOD 3 START - beTiles found! Attempting to get BERenderManager");
                try {
                    Method getRenderManagerMethod = findBufferMethod(beTiles.getClass(), "getRenderManager");
                    LOGGER.warn("[CLC/LTAPIFacade] METHOD 3.1 - Checking getRenderManager method");
                    if (getRenderManagerMethod == null) {
                        getRenderManagerMethod = findBufferMethod(beTiles.getClass(), "getManager");
                    }
                    LOGGER.warn("[CLC/LTAPIFacade] METHOD 3.2 - Checking getManager method");
                    if (getRenderManagerMethod == null) {
                        getRenderManagerMethod = findBufferMethod(beTiles.getClass(), "renderManager");
                    }
                    LOGGER.warn("[CLC/LTAPIFacade] METHOD 3.3 - Checking renderManager field");
                    if (getRenderManagerMethod != null) {
                        LOGGER.warn("[CLC/LTAPIFacade] METHOD 3.4 - Found method {}, invoking...", getRenderManagerMethod.getName());
                        renderManager = getRenderManagerMethod.invoke(beTiles);
                        LOGGER.warn("[CLC/LTAPIFacade] METHOD 3.5 - Found BERenderManager via {}: {} (type: {})", 
                            getRenderManagerMethod.getName(), renderManager, renderManager != null ? renderManager.getClass().getName() : "null");
                    } else {                        LOGGER.warn("[CLC/LTAPIFacade] METHOD 3.6 - No method found, trying field access");
                        // Try field access for render manager
                        java.lang.reflect.Field[] fields = beTiles.getClass().getDeclaredFields();
                        LOGGER.warn("[CLC/LTAPIFacade] METHOD 3.6.1 - BETiles has {} fields to examine", fields.length);
                        
                        for (java.lang.reflect.Field field : fields) {
                            LOGGER.warn("[CLC/LTAPIFacade] METHOD 3.6.2 - Examining field: {} (type: {})", 
                                field.getName(), field.getType().getName());
                                
                            if (field.getType().getName().contains("BERenderManager") || 
                                field.getName().toLowerCase().contains("render")) {
                                field.setAccessible(true);
                                Object fieldValue = field.get(beTiles);
                                LOGGER.warn("[CLC/LTAPIFacade] METHOD 3.6.3 - Field {} value: {} (type: {})", 
                                    field.getName(), fieldValue, fieldValue != null ? fieldValue.getClass().getName() : "null");
                                    
                                if (fieldValue != null && fieldValue.getClass().getName().contains("BERenderManager")) {
                                    renderManager = fieldValue;
                                    LOGGER.warn("[CLC/LTAPIFacade] METHOD 3.7 - Found BERenderManager via field {}: {} (type: {})", 
                                        field.getName(), renderManager, renderManager.getClass().getName());
                                    break;
                                }
                            }
                        }
                          if (renderManager == null) {
                            LOGGER.warn("[CLC/LTAPIFacade] METHOD 3.8 - No BERenderManager found in any field");
                            
                            // Try to initialize the BERenderManager if the render field exists but is null
                            for (java.lang.reflect.Field field : fields) {
                                if (field.getName().equals("render") && 
                                    field.getType().getName().contains("BERenderManager")) {
                                    try {
                                        LOGGER.warn("[CLC/LTAPIFacade] METHOD 3.9 - Attempting to initialize BERenderManager");
                                        
                                        // Create a new BERenderManager instance
                                        Class<?> renderManagerClass = Class.forName("team.creative.littletiles.client.render.block.BERenderManager");
                                        java.lang.reflect.Constructor<?> constructor = renderManagerClass.getDeclaredConstructor(
                                            Class.forName("team.creative.littletiles.common.block.entity.BETiles")
                                        );
                                        
                                        Object newRenderManager = constructor.newInstance(beTiles);
                                        
                                        // Set the field
                                        field.setAccessible(true);
                                        field.set(beTiles, newRenderManager);
                                        
                                        renderManager = newRenderManager;
                                        LOGGER.warn("[CLC/LTAPIFacade] METHOD 3.10 - Successfully initialized BERenderManager: {}", 
                                            renderManager.getClass().getName());
                                        break;
                                        
                                    } catch (Exception e) {
                                        LOGGER.warn("[CLC/LTAPIFacade] METHOD 3.11 - Failed to initialize BERenderManager: {}", e.getMessage());
                                    }
                                }
                            }
                        }
                    }                } catch (Exception e) {
                    LOGGER.warn("[CLC/LTAPIFacade] METHOD 3 ERROR - Could not access BERenderManager: {}", e.getMessage());
                }
            } else {
                LOGGER.warn("[CLC/LTAPIFacade] METHOD 3 SKIPPED - beTiles is NULL - cannot proceed to get BERenderManager");
            }            // Method 4: Try simple cachedBoxes() first, then getRenderingBoxes with context
            if (renderManager != null) {
                LOGGER.warn("[CLC/LTAPIFacade] METHOD 4 START - BERenderManager found, attempting cachedBoxes() first");
                try {
                    // Try cachedBoxes() first - this is simpler and doesn't need RenderingBlockContext
                    Method cachedBoxesMethod = renderManager.getClass().getMethod("cachedBoxes");
                    if (cachedBoxesMethod != null) {
                        LOGGER.warn("[CLC/LTAPIFacade] METHOD 4.1 - Found cachedBoxes method, calling it");
                        Object result = cachedBoxesMethod.invoke(renderManager);
                        LOGGER.warn("[CLC/LTAPIFacade] METHOD 4.2 - cachedBoxes result: {}", result != null ? result.getClass().getName() : "null");
                        
                        if (result != null) {
                            LOGGER.info("[CLC/LTAPIFacade] ✅ Got rendering boxes: {}", result.getClass().getSimpleName());
                            if (attemptDirectBoxRendering(result, poseStack, bufferSource, combinedLight, combinedOverlay, partialTicks)) {
                                renderedSomething = true;
                            }
                        } else {
                            LOGGER.warn("[CLC/LTAPIFacade] cachedBoxes returned null, will try getRenderingBoxes with context");
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("[CLC/LTAPIFacade] METHOD 4.3 - cachedBoxes failed: {}, trying getRenderingBoxes", e.getMessage());
                }
                
                // Fallback to getRenderingBoxes with context if cachedBoxes failed
                if (!renderedSomething) {
                    try {
                        // Look for getRenderingBoxes(RenderingBlockContext) method
                        LOGGER.warn("[CLC/LTAPIFacade] METHOD 4.4 - Looking for getRenderingBoxes method");
                        Method getRenderingBoxesMethod = findBufferMethod(renderManager.getClass(), "getRenderingBoxes", 
                            Class.forName("team.creative.littletiles.client.render.cache.build.RenderingBlockContext"));
                        
                        if (getRenderingBoxesMethod != null) {
                            LOGGER.warn("[CLC/LTAPIFacade] METHOD 4.5 - Found getRenderingBoxes method on BERenderManager: {}", getRenderingBoxesMethod);
                            // Try with null context first (simple approach)
                            Object result = null;
                            try {
                                LOGGER.warn("[CLC/LTAPIFacade] METHOD 4.6 - Trying getRenderingBoxes with null context");
                                result = getRenderingBoxesMethod.invoke(renderManager, (Object) null);
                                
                                if (result != null) {
                                    LOGGER.info("[CLC/LTAPIFacade] ✅ Got rendering boxes: {}", result.getClass().getSimpleName());
                                    if (attemptDirectBoxRendering(result, poseStack, bufferSource, combinedLight, combinedOverlay, partialTicks)) {
                                        renderedSomething = true;
                                    }
                                }
                            } catch (Exception e) {
                                LOGGER.warn("[CLC/LTAPIFacade] METHOD 4.7 - getRenderingBoxes with null failed: {}", e.getMessage());
                            }
                        } else {
                            LOGGER.warn("[CLC/LTAPIFacade] METHOD 4.8 - Could not find getRenderingBoxes method on BERenderManager class: {}", renderManager.getClass().getName());
                        }
                    } catch (Exception e) {
                        LOGGER.warn("[CLC/LTAPIFacade] METHOD 4 ERROR - Error calling BERenderManager.getRenderingBoxes: {}", e.getMessage());
                    }
                }
            } else {
                LOGGER.warn("[CLC/LTAPIFacade] METHOD 4 SKIPPED - Could not access BERenderManager - tiles may not be properly initialized");
            }
            
        } catch (Exception e) {
            LOGGER.error("[CLC/LTAPIFacade] Error in BERenderManager approach: {}", e.getMessage(), e);
        }
        
        if (shouldLogRenderingBoxes()) {
            LOGGER.info("[CLC/LTAPIFacade] BERenderManager approach rendered: {}", renderedSomething);
        }
        return renderedSomething;
    }
    
    /**
     * Attempts to render boxes directly from the rendering boxes collection
     */
    private static boolean attemptDirectBoxRendering(Object result, 
                                                   com.mojang.blaze3d.vertex.PoseStack poseStack,
                                                   net.minecraft.client.renderer.MultiBufferSource bufferSource,
                                                   int combinedLight, int combinedOverlay, float partialTicks) {
        boolean renderedSomething = false;
        try {
            if (result instanceof java.util.Map) {
                java.util.Map<?, ?> renderingBoxMap = (java.util.Map<?, ?>) result;
                LOGGER.warn("[CLC/LTAPIFacade] Processing {} rendering box groups", renderingBoxMap.size());
                
                for (Object layerMapList : renderingBoxMap.values()) {
                    if (layerMapList != null) {
                        // Try to iterate over the ChunkLayerMapList
                        try {
                            if (layerMapList instanceof Iterable) {
                                for (Object renderBox : (Iterable<?>) layerMapList) {
                                    if (renderBox != null && renderBox.getClass().getName().contains("LittleRenderBox")) {
                                        LOGGER.warn("[CLC/LTAPIFacade] Processing LittleRenderBox: {}", renderBox.getClass().getName());
                                        
                                        // Try to render this box with different render types
                                        net.minecraft.client.renderer.RenderType[] renderTypes = {
                                            net.minecraft.client.renderer.RenderType.solid(),
                                            net.minecraft.client.renderer.RenderType.cutout(),
                                            net.minecraft.client.renderer.RenderType.cutoutMipped(),
                                            net.minecraft.client.renderer.RenderType.translucent()
                                        };
                                        
                                        for (net.minecraft.client.renderer.RenderType renderType : renderTypes) {
                                            if (renderLittleRenderBox(renderBox, poseStack, bufferSource, combinedLight, combinedOverlay, renderType, null, null)) {
                                                renderedSomething = true;
                                                LOGGER.warn("[CLC/LTAPIFacade] Successfully rendered LittleRenderBox with renderType {}", renderType);
                                                break; // Move to next render box
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.warn("[CLC/LTAPIFacade] Error processing layer map list: {}", e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("[CLC/LTAPIFacade] Error in attemptDirectBoxRendering: {}", e.getMessage());
        }
        return renderedSomething;
    }
    
    /**
     * Renders LittleTiles using a virtual BETiles instance with proper BERenderManager access.
     * This approach solves the "Could not access BERenderManager - tiles may not be properly initialized" issue.
     */
    public static boolean renderWithVirtualBETiles(Object virtualBETiles, 
                                                  com.mojang.blaze3d.vertex.PoseStack poseStack,
                                                  net.minecraft.client.renderer.MultiBufferSource buffer,
                                                  int packedLight, int packedOverlay, float partialTicks) {
        try {
            LOGGER.info("🔧 [CLC/LTAPIFacade] Starting renderWithVirtualBETiles");
            
            // Verify that the virtual BETiles has a valid BERenderManager
            java.lang.reflect.Field renderField = virtualBETiles.getClass().getDeclaredField("render");
            renderField.setAccessible(true);
            Object renderManager = renderField.get(virtualBETiles);
            
            if (renderManager == null) {
                LOGGER.warn("⚠️ [CLC/LTAPIFacade] Virtual BETiles has null BERenderManager, attempting to initialize");
                
                // Try to initialize the render manager if it's null
                initializeBERenderManager(virtualBETiles, renderField);
                renderManager = renderField.get(virtualBETiles);
                
                if (renderManager == null) {
                    LOGGER.warn("❌ [CLC/LTAPIFacade] Failed to initialize BERenderManager on virtual BETiles");
                    return false;
                }
            }
            
            LOGGER.info("✅ [CLC/LTAPIFacade] Virtual BETiles has valid BERenderManager: {}", 
                                                renderManager.getClass().getSimpleName());
            
            // Now use the established BERenderManager approach
            return renderWithBERenderManager(virtualBETiles, renderManager, poseStack, buffer, packedLight, packedOverlay, partialTicks);
            
        } catch (Exception e) {
            LOGGER.error("❌ [CLC/LTAPIFacade] renderWithVirtualBETiles failed: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Helper method to initialize BERenderManager on a BETiles instance
     */
    private static void initializeBERenderManager(Object beTiles, java.lang.reflect.Field renderField) {
        try {
            // Get BERenderManager class and create instance
            Class<?> renderManagerClass = Class.forName("team.creative.littletiles.client.render.entity.BERenderManager");
            java.lang.reflect.Constructor<?> constructor = renderManagerClass.getConstructor(
                Class.forName("team.creative.littletiles.common.block.entity.BETiles")
            );
            
            Object newRenderManager = constructor.newInstance(beTiles);
            renderField.set(beTiles, newRenderManager);
            
            LOGGER.info("✅ [CLC/LTAPIFacade] Successfully initialized BERenderManager on virtual BETiles");
            
        } catch (Exception e) {
            LOGGER.warn("⚠️ [CLC/LTAPIFacade] Failed to initialize BERenderManager: {}", e.getMessage());
        }
    }
    
    /**
     * Renders using the BERenderManager approach (the successful path from our previous implementation)
     */
    private static boolean renderWithBERenderManager(Object beTiles, Object renderManager, 
                                                    com.mojang.blaze3d.vertex.PoseStack poseStack,
                                                    net.minecraft.client.renderer.MultiBufferSource buffer,
                                                    int packedLight, int packedOverlay, float partialTicks) {
        try {
            LOGGER.info("🎨 [CLC/LTAPIFacade] Using BERenderManager approach");
            
            // Create RenderingBlockContext with proper handler
            Class<?> handlerClass = Class.forName("team.creative.littletiles.client.render.cache.build.RenderingLevelHandler");
            java.lang.reflect.Method getLevelMethod = beTiles.getClass().getMethod("getLevel");
            Object level = getLevelMethod.invoke(beTiles);
            java.lang.reflect.Method ofMethod = handlerClass.getMethod("of", net.minecraft.world.level.Level.class);
            Object renderingHandler = ofMethod.invoke(null, level);
            
            // Get section position
            java.lang.reflect.Method sectionPosMethod = handlerClass.getMethod("sectionPos", 
                Class.forName("team.creative.littletiles.common.block.entity.BETiles"));
            long sectionPos = (Long) sectionPosMethod.invoke(renderingHandler, beTiles);
            
            // Create RenderingBlockContext
            Class<?> contextClass = Class.forName("team.creative.littletiles.client.render.cache.build.RenderingBlockContext");
            java.lang.reflect.Constructor<?> contextConstructor = contextClass.getConstructor(
                Class.forName("team.creative.littletiles.common.block.entity.BETiles"),
                boolean.class, long.class, handlerClass
            );
            Object context = contextConstructor.newInstance(beTiles, true, sectionPos, renderingHandler);
            
            // Call getRenderingBoxes
            java.lang.reflect.Method getRenderingBoxesMethod = findBufferMethod(renderManager.getClass(), "getRenderingBoxes", contextClass);
            if (getRenderingBoxesMethod == null) {
                LOGGER.warn("⚠️ [CLC/LTAPIFacade] getRenderingBoxes method not found");
                return false;
            }
            
            Object renderingBoxes = getRenderingBoxesMethod.invoke(renderManager, context);
            
            if (renderingBoxes == null) {
                LOGGER.warn("⚠️ [CLC/LTAPIFacade] getRenderingBoxes returned null");
                return false;
            }
            
            LOGGER.info("✅ [CLC/LTAPIFacade] Got rendering boxes: {}", renderingBoxes.getClass().getSimpleName());
            
            // Process and render the boxes
            return processAndRenderBoxes(renderingBoxes, poseStack, buffer, packedLight, packedOverlay);
            
        } catch (Exception e) {
            LOGGER.error("❌ [CLC/LTAPIFacade] BERenderManager approach failed: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Process and render the boxes returned by getRenderingBoxes
     */
    private static boolean processAndRenderBoxes(Object renderingBoxes, 
                                               com.mojang.blaze3d.vertex.PoseStack poseStack,
                                               net.minecraft.client.renderer.MultiBufferSource buffer,
                                               int packedLight, int packedOverlay) {
        try {
            if (!(renderingBoxes instanceof java.util.Map)) {
                LOGGER.warn("⚠️ [CLC/LTAPIFacade] Expected Map but got: {}", renderingBoxes.getClass());
                return false;
            }
            
            java.util.Map<?, ?> renderingBoxMap = (java.util.Map<?, ?>) renderingBoxes;
            LOGGER.info("🎨 [CLC/LTAPIFacade] Processing {} rendering box groups", renderingBoxMap.size());
            
            if (renderingBoxMap.isEmpty()) {
                LOGGER.warn("⚠️ [CLC/LTAPIFacade] Rendering box map is empty - no content to render");
                return false;
            }
            
            boolean anySuccess = false;                for (Object layerMapList : renderingBoxMap.values()) {
                    if (layerMapList != null && layerMapList instanceof Iterable) {
                        LOGGER.info("🔍 [CLC/LTAPIFacade] Processing layerMapList of type: {}", layerMapList.getClass().getName());
                        for (Object renderBox : (Iterable<?>) layerMapList) {
                            if (renderBox != null && renderBox.getClass().getName().contains("LittleRenderBox")) {
                                LOGGER.info("🎯 [CLC/LTAPIFacade] Found LittleRenderBox: {}", renderBox.getClass().getName());
                                // Try to render this individual box
                                boolean boxSuccess = renderIndividualBox(renderBox, poseStack, buffer, packedLight, packedOverlay);
                                if (boxSuccess) {
                                    anySuccess = true;
                                    LOGGER.info("✅ [CLC/LTAPIFacade] Successfully rendered box: {}", renderBox.getClass().getName());
                                } else {
                                    LOGGER.info("❌ [CLC/LTAPIFacade] Failed to render box: {}", renderBox.getClass().getName());
                                }
                            } else {
                                LOGGER.info("🔍 [CLC/LTAPIFacade] Skipping non-LittleRenderBox: {}", renderBox != null ? renderBox.getClass().getName() : "null");
                            }
                        }
                    } else {
                        LOGGER.info("⚠️ [CLC/LTAPIFacade] LayerMapList is null or not iterable: {}", layerMapList != null ? layerMapList.getClass().getName() : "null");
                    }
                }
            
            LOGGER.info("{} [CLC/LTAPIFacade] Box processing complete. Any success: {}", 
                                                anySuccess ? "✅" : "⚠️", anySuccess);
            return anySuccess;
            
        } catch (Exception e) {
            LOGGER.error("❌ [CLC/LTAPIFacade] Failed to process rendering boxes: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Render an individual LittleRenderBox
     */    private static boolean renderIndividualBox(Object renderBox, 
                                             com.mojang.blaze3d.vertex.PoseStack poseStack,
                                             net.minecraft.client.renderer.MultiBufferSource buffer,
                                             int packedLight, int packedOverlay) {
        try {
            LOGGER.info("🔧 [CLC/LTAPIFacade] Attempting to render individual box: {}", renderBox.getClass().getName());
            
            // This is where we would implement the actual rendering of individual boxes
            // For now, we'll try to call a render method on the box if it exists
            java.lang.reflect.Method renderMethod = findBufferMethod(renderBox.getClass(), "render", 
                com.mojang.blaze3d.vertex.PoseStack.class,
                net.minecraft.client.renderer.MultiBufferSource.class,
                int.class, int.class
            );
            
            if (renderMethod != null) {
                LOGGER.info("✅ [CLC/LTAPIFacade] Found render method: {}, invoking...", renderMethod.getName());
                renderMethod.invoke(renderBox, poseStack, buffer, packedLight, packedOverlay);
                LOGGER.info("✅ [CLC/LTAPIFacade] Successfully invoked render method on {}", renderBox.getClass().getSimpleName());
                return true;
            } else {
                LOGGER.info("⚠️ [CLC/LTAPIFacade] No render method found for: {}", renderBox.getClass().getSimpleName());
                
                // Let's examine what methods ARE available on this class
                java.lang.reflect.Method[] methods = renderBox.getClass().getMethods();
                LOGGER.info("📋 [CLC/LTAPIFacade] Available methods on {}:", renderBox.getClass().getSimpleName());
                for (java.lang.reflect.Method method : methods) {
                    if (method.getName().toLowerCase().contains("render") || 
                        method.getName().toLowerCase().contains("buffer") ||
                        method.getName().toLowerCase().contains("tessellate")) {
                        LOGGER.info("🔍 [CLC/LTAPIFacade]   - {}", method.toString());
                    }
                }
                return false;
            }
            
        } catch (Exception e) {
            LOGGER.info("❌ [CLC/LTAPIFacade] Failed to render box {}: {}", 
                                                 renderBox.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }
}
