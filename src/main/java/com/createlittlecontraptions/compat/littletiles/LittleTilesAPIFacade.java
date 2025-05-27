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

// Import additional LittleTiles classes for rendering investigation
import team.creative.littletiles.client.render.tile.LittleRenderBox;
// Import for accessing individual tiles
import team.creative.littletiles.common.block.little.tile.LittleTile;

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
public class LittleTilesAPIFacade {    private static final Logger LOGGER = LogManager.getLogger("CreateLittleContraptions/LTAPIFacade");
      // ULTRA AGGRESSIVE THROTTLING - Reduce logging to absolute minimum
    private static long lastLogTime = 0;
    private static final long LOG_INTERVAL_MS = 120000; // 2 minutes
    
    // Detailed logs throttling - very restrictive 
    private static long lastDetailedLogTime = 0;
    private static final long DETAILED_LOG_INTERVAL_MS = 300000; // 5 minutes
    
    // Development debugging logs throttling - extremely restrictive
    private static long lastDevLogTime = 0;
    private static final long DEV_LOG_INTERVAL_MS = 600000; // 10 minutes
    
    // Per-message throttling for specific repeated messages
    private static long lastRenderingBoxesTime = 0;
    private static long lastPrimaryApproachTime = 0;
    private static long lastBlockParentCollectionTime = 0;
    private static long lastIndividualRenderingTime = 0;
    private static final long PER_MESSAGE_INTERVAL_MS = 180000; // 3 minutes per specific message
    
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
            }

            // Create a minimal BlockParentCollection without Level dependencies
            // The key insight: we can create a tiles collection without a BETiles instance
            BlockParentCollection tiles = new BlockParentCollection(null, false); // null BE, not client side
            
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
        }          if (!renderedSomething && hasTiles) {
            if (shouldLog()) {
                LOGGER.info("[CLC/LTAPIFacade] Attempting individual tile rendering via LittleRenderBox...");
                
                // === GEMINI'S DIAGNOSTIC: Check for VirtualRenderWorld/BETiles issues ===
                diagnoseRenderContextIssues(tiles);
            }
              try {
                // Try Gemini's alternative approach first (LittleTile.getRenderingBoxes)
                if (shouldLog()) {
                    LOGGER.info("[CLC/LTAPIFacade] Trying Gemini's recommended LittleTile.getRenderingBoxes approach...");
                }
                renderedSomething = attemptLittleTileGetRenderingBoxesApproach(tiles, poseStack, bufferSource, combinedLight, combinedOverlay, partialTicks, grid);
                
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
     * Alternative approach using LittleTile.getRenderingBoxes(...) as recommended by Gemini
     * This populates a list of LittleRenderBox instances instead of returning a single one
     */
    private static boolean attemptLittleTileGetRenderingBoxesApproach(BlockParentCollection tiles, PoseStack poseStack, 
                                                                     MultiBufferSource bufferSource, int combinedLight, 
                                                                     int combinedOverlay, float partialTicks, LittleGrid grid) {
        boolean renderedSomething = false;
        int tileCount = 0;
        
        if (shouldLogRenderingBoxes()) {
            LOGGER.info("[CLC/LTAPIFacade] === Trying Gemini's Alternative: LittleTile.getRenderingBoxes() ===");
        }
        
        try {
            // Look for the LittleTile.getRenderingBoxes method that takes a layer integer
            Method getRenderingBoxesMethod = null;
            Class<?> chunkLayerMapListClass = null;
            
            // Try to find the ChunkLayerMapList class from CreativeCore
            try {
                chunkLayerMapListClass = Class.forName("team.creative.creativecore.common.util.type.list.ChunkLayerMapList");
                LOGGER.info("[CLC/LTAPIFacade] Found ChunkLayerMapList class: {}", chunkLayerMapListClass.getName());
            } catch (ClassNotFoundException e) {
                LOGGER.warn("[CLC/LTAPIFacade] Could not find ChunkLayerMapList class, trying alternative approaches");
                // Try java.util.List as fallback
                chunkLayerMapListClass = java.util.List.class;
            }
            
            for (var tilePair : tiles.allTiles()) {
                if (tilePair != null && tilePair.value != null) {
                    tileCount++;
                    LittleTile tile = tilePair.value;
                    
                    LOGGER.info("[CLC/LTAPIFacade] Processing tile #{}: {} (class: {})", tileCount, tile, tile.getClass().getName());
                    
                    // Look for getRenderingBoxes method on the tile
                    try {
                        // Try different method signatures that might exist
                        Method[] possibleMethods = {
                            findBufferMethod(tile.getClass(), "getRenderingBoxes", 
                                LittleGrid.class, Object.class, LittleGrid.class, chunkLayerMapListClass, int.class),
                            findBufferMethod(tile.getClass(), "getRenderingBoxes", 
                                LittleGrid.class, Object.class, chunkLayerMapListClass, int.class),
                            findBufferMethod(tile.getClass(), "getRenderingBoxes", 
                                chunkLayerMapListClass, int.class),
                            findBufferMethod(tile.getClass(), "getRenderingBoxes", 
                                java.util.List.class, int.class)
                        };
                        
                        for (Method method : possibleMethods) {
                            if (method != null) {
                                getRenderingBoxesMethod = method;
                                LOGGER.info("[CLC/LTAPIFacade] Found getRenderingBoxes method: {}", method);
                                break;
                            }
                        }
                        
                        if (getRenderingBoxesMethod != null) {
                            // Create a temporary list to collect LittleRenderBox instances
                            java.util.List<Object> renderBoxList = new java.util.ArrayList<>();
                            
                            // Try different layer values (0-3 typically correspond to solid, cutout, cutout_mipped, translucent)
                            for (int layer = 0; layer < 4; layer++) {
                                try {
                                    LOGGER.info("[CLC/LTAPIFacade] Calling getRenderingBoxes for tile #{}, layer {}", tileCount, layer);
                                    
                                    // Call the method based on its parameter count
                                    if (getRenderingBoxesMethod.getParameterCount() == 5) {
                                        // getRenderingBoxes(grid, offset, vanillaGrid, list, layer)
                                        getRenderingBoxesMethod.invoke(tile, grid, new Object(), grid, renderBoxList, layer);
                                    } else if (getRenderingBoxesMethod.getParameterCount() == 4) {
                                        // getRenderingBoxes(grid, offset, list, layer)
                                        getRenderingBoxesMethod.invoke(tile, grid, new Object(), renderBoxList, layer);
                                    } else if (getRenderingBoxesMethod.getParameterCount() == 2) {
                                        // getRenderingBoxes(list, layer)
                                        getRenderingBoxesMethod.invoke(tile, renderBoxList, layer);
                                    }
                                    
                                    LOGGER.info("[CLC/LTAPIFacade] getRenderingBoxes returned {} boxes for layer {}", renderBoxList.size(), layer);
                                    
                                    // Process any LittleRenderBox instances that were added
                                    for (Object renderBox : renderBoxList) {
                                        if (renderBox != null) {
                                            LOGGER.info("[CLC/LTAPIFacade] Processing LittleRenderBox: {} (type: {})", 
                                                renderBox, renderBox.getClass().getName());
                                            
                                            // Convert layer to RenderType
                                            net.minecraft.client.renderer.RenderType renderType = getRenderTypeFromLayer(layer);
                                            
                                            if (renderLittleRenderBox(renderBox, poseStack, bufferSource, combinedLight, combinedOverlay, renderType, tiles, tile)) {
                                                renderedSomething = true;
                                                LOGGER.info("[CLC/LTAPIFacade] Successfully rendered LittleRenderBox for tile #{}, layer {}", tileCount, layer);
                                            }
                                        }
                                    }
                                    
                                    renderBoxList.clear(); // Clear for next layer
                                    
                                } catch (Exception e) {
                                    LOGGER.warn("[CLC/LTAPIFacade] Failed to call getRenderingBoxes for tile #{}, layer {}: {}", tileCount, layer, e.getMessage());
                                }
                            }
                        } else {
                            LOGGER.warn("[CLC/LTAPIFacade] Could not find getRenderingBoxes method on tile class: {}", tile.getClass().getName());
                        }
                        
                    } catch (Exception e) {
                        LOGGER.error("[CLC/LTAPIFacade] Error processing tile #{}: {}", tileCount, e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("[CLC/LTAPIFacade] Error in LittleTile.getRenderingBoxes approach: {}", e.getMessage(), e);
        }
        
        if (shouldLogRenderingBoxes()) {
            LOGGER.info("[CLC/LTAPIFacade] LittleTile.getRenderingBoxes approach processed {} tiles, rendered: {}", tileCount, renderedSomething);
        }
        return renderedSomething;
    }
    
    /**
     * Convert layer integer to RenderType (approximation)
     */
    private static net.minecraft.client.renderer.RenderType getRenderTypeFromLayer(int layer) {
        switch (layer) {
            case 0: return net.minecraft.client.renderer.RenderType.solid();
            case 1: return net.minecraft.client.renderer.RenderType.cutout();
            case 2: return net.minecraft.client.renderer.RenderType.cutoutMipped();
            case 3: return net.minecraft.client.renderer.RenderType.translucent();
            default: return net.minecraft.client.renderer.RenderType.solid();
        }
    }

    /**
     * Diagnoses potential VirtualRenderWorld and BETiles client initialization issues
     * Based on Gemini's analysis of potential null return causes
     */
    private static void diagnoseRenderContextIssues(BlockParentCollection tiles) {
        LOGGER.info("[CLC/LTAPIFacade] === Diagnosing Render Context Issues (Gemini's Analysis) ===");
        
        try {
            // Check if tiles has an owning BETiles and if it's properly client-initialized
            java.lang.reflect.Field beTilesField = null;
            Object owningBETiles = null;
            
            // Try to find the owning BETiles instance
            String[] possibleFieldNames = {"be", "blockEntity", "owner", "parent", "tiles"};
            for (String fieldName : possibleFieldNames) {
                try {
                    beTilesField = tiles.getClass().getDeclaredField(fieldName);
                    beTilesField.setAccessible(true);
                    Object fieldValue = beTilesField.get(tiles);
                    
                    if (fieldValue != null && fieldValue.getClass().getName().contains("BETiles")) {
                        owningBETiles = fieldValue;
                        LOGGER.info("[CLC/LTAPIFacade] Found owning BETiles via field '{}': {} (type: {})", 
                            fieldName, fieldValue, fieldValue.getClass().getName());
                        break;
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // Field doesn't exist or not accessible, continue
                }
            }
            
            if (owningBETiles != null) {
                // Check BETiles client initialization
                try {
                    // Check if BERenderManager exists (indicates client initialization)
                    java.lang.reflect.Field renderField = owningBETiles.getClass().getDeclaredField("render");
                    renderField.setAccessible(true);
                    Object renderManager = renderField.get(owningBETiles);
                    
                    if (renderManager != null) {
                        LOGGER.info("[CLC/LTAPIFacade] BETiles has BERenderManager: {} (type: {})", 
                            renderManager, renderManager.getClass().getName());
                            
                        // Check render manager state
                        try {
                            Method isClientMethod = owningBETiles.getClass().getMethod("isClient");
                            Boolean isClient = (Boolean) isClientMethod.invoke(owningBETiles);
                            LOGGER.info("[CLC/LTAPIFacade] BETiles.isClient(): {}", isClient);
                        } catch (Exception e) {
                            LOGGER.warn("[CLC/LTAPIFacade] Could not check BETiles.isClient(): {}", e.getMessage());
                        }
                        
                        // Check level context
                        try {
                            java.lang.reflect.Field levelField = owningBETiles.getClass().getSuperclass().getDeclaredField("level");
                            levelField.setAccessible(true);
                            Object level = levelField.get(owningBETiles);
                            
                            if (level != null) {
                                Method isClientSideMethod = level.getClass().getMethod("isClientSide");
                                Boolean isClientSide = (Boolean) isClientSideMethod.invoke(level);
                                LOGGER.info("[CLC/LTAPIFacade] BETiles.level.isClientSide(): {}", isClientSide);
                                LOGGER.info("[CLC/LTAPIFacade] Level type: {}", level.getClass().getName());
                            } else {
                                LOGGER.warn("[CLC/LTAPIFacade] BETiles.level is NULL - this is likely the cause of getRenderingBox returning null!");
                            }
                        } catch (Exception e) {
                            LOGGER.warn("[CLC/LTAPIFacade] Could not check BETiles level: {}", e.getMessage());
                        }
                        
                    } else {
                        LOGGER.warn("[CLC/LTAPIFacade] BETiles.render (BERenderManager) is NULL - client not properly initialized!");
                        LOGGER.warn("[CLC/LTAPIFacade] This is likely why getRenderingBox returns null - BERenderManager is required for rendering operations");
                    }
                    
                } catch (Exception e) {
                    LOGGER.warn("[CLC/LTAPIFacade] Could not access BETiles render field: {}", e.getMessage());
                }
                
            } else {
                LOGGER.warn("[CLC/LTAPIFacade] Could not find owning BETiles instance in BlockParentCollection");
                LOGGER.info("[CLC/LTAPIFacade] This might indicate the tiles were loaded outside normal BlockEntity context");
            }
            
        } catch (Exception e) {
            LOGGER.error("[CLC/LTAPIFacade] Error during render context diagnosis: {}", e.getMessage(), e);
        }
        
        LOGGER.info("[CLC/LTAPIFacade] === End Render Context Diagnosis ===");
    }
}
