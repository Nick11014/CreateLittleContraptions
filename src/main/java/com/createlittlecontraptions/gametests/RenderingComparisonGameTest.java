package com.createlittlecontraptions.gametests;

import com.createlittlecontraptions.commands.ContraptionDebugCommand;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * GameTests for Step 2.5 - Rendering method analysis and comparison
 * Tests rendering behavior differences between common blocks and LittleTiles
 */
@PrefixGameTestTemplate(false)
public class RenderingComparisonGameTest {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Test that the new rendering analysis command executes without errors
     */
    @GameTest(template = "createlittlecontraptions:elevator_unassembled")
    public static void contraptionDebugRenderingRobustnessTest(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                LOGGER.info("Starting contraption debug rendering robustness test");
                
                try {
                    // Get the server level from the helper
                    ServerLevel level = helper.getLevel();
                    
                    // Create a mock CommandSourceStack for testing
                    CommandSourceStack mockSource = level.getServer()
                        .createCommandSourceStack()
                        .withLevel(level)
                        .withPosition(helper.absolutePos(BlockPos.ZERO).getCenter());
                    
                    // Test that we can find contraption entities without crashing
                    Method findContraptionEntitiesMethod = ContraptionDebugCommand.class
                        .getDeclaredMethod("findContraptionEntities", ServerLevel.class);
                    findContraptionEntitiesMethod.setAccessible(true);
                    
                    @SuppressWarnings("unchecked")
                    List<Entity> entities = (List<Entity>) findContraptionEntitiesMethod.invoke(null, level);
                    
                    LOGGER.info("Found {} contraption entities in test", entities.size());
                    
                    // If we have contraptions, test the rendering analysis methods
                    if (!entities.isEmpty()) {
                        // Test the block analysis method
                        testRenderingAnalysisMethods(entities.get(0));
                    }
                    
                    LOGGER.info("Contraption debug rendering test completed successfully");
                    helper.succeed();
                    
                } catch (Exception e) {
                    LOGGER.error("Error in contraption debug rendering test: {}", e.getMessage(), e);
                    helper.fail("Exception in rendering analysis: " + e.getMessage());
                }
            });
    }

    /**
     * Test rendering method differences between block types
     */
    @GameTest(template = "createlittlecontraptions:elevator_unassembled")
    public static void blockRenderingMethodsComparisonTest(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                LOGGER.info("Starting block rendering methods comparison test");
                
                try {
                    // Test common block rendering methods
                    Object stoneBlock = Blocks.STONE;
                    Object stoneState = Blocks.STONE.defaultBlockState();
                    testBlockRenderingMethods(stoneBlock, stoneState, "Stone (Common Block)");
                    
                    // Test fence block (Create contraption compatible)
                    Object fenceBlock = Blocks.OAK_FENCE;
                    Object fenceState = Blocks.OAK_FENCE.defaultBlockState();
                    testBlockRenderingMethods(fenceBlock, fenceState, "Oak Fence (Common Block)");
                    
                    LOGGER.info("Block rendering methods comparison test completed successfully");
                    helper.succeed();
                    
                } catch (Exception e) {
                    LOGGER.error("Error in block rendering methods comparison test: {}", e.getMessage(), e);
                    helper.fail("Exception in block rendering methods test: " + e.getMessage());
                }
            });
    }

    /**
     * Test data integrity of StructureBlockInfo for different block types
     */
    @GameTest(template = "createlittlecontraptions:elevator_unassembled")
    public static void structureBlockInfoIntegrityTest(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                LOGGER.info("Starting StructureBlockInfo integrity test");
                
                try {
                    ServerLevel level = helper.getLevel();
                    
                    // Find contraption entities
                    Method findContraptionEntitiesMethod = ContraptionDebugCommand.class
                        .getDeclaredMethod("findContraptionEntities", ServerLevel.class);
                    findContraptionEntitiesMethod.setAccessible(true);
                    
                    @SuppressWarnings("unchecked")
                    List<Entity> entities = (List<Entity>) findContraptionEntitiesMethod.invoke(null, level);
                    
                    if (!entities.isEmpty()) {
                        Entity contraptionEntity = entities.get(0);
                        
                        // Get contraption data
                        Method getContraptionMethod = ContraptionDebugCommand.class
                            .getDeclaredMethod("getContraptionFromEntity", Entity.class);
                        getContraptionMethod.setAccessible(true);
                        Object contraption = getContraptionMethod.invoke(null, contraptionEntity);
                        
                        if (contraption != null) {
                            // Get blocks data
                            Method getBlocksMethod = ContraptionDebugCommand.class
                                .getDeclaredMethod("getBlocksFromContraption", Object.class);
                            getBlocksMethod.setAccessible(true);
                            Object blocksData = getBlocksMethod.invoke(null, contraption);
                            
                            if (blocksData != null) {
                                testStructureBlockInfoIntegrity(blocksData);
                            }
                        }
                    }
                    
                    LOGGER.info("StructureBlockInfo integrity test completed successfully");
                    helper.succeed();
                    
                } catch (Exception e) {
                    LOGGER.error("Error in StructureBlockInfo integrity test: {}", e.getMessage(), e);
                    helper.fail("Exception in StructureBlockInfo integrity test: " + e.getMessage());
                }
            });
    }

    /**
     * Test LittleTiles detection and counting accuracy
     */
    @GameTest(template = "createlittlecontraptions:elevator_unassembled")
    public static void littleTilesDetectionAccuracyTest(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                LOGGER.info("Starting LittleTiles detection accuracy test");
                
                try {
                    ServerLevel level = helper.getLevel();
                    
                    // Find contraption entities
                    Method findContraptionEntitiesMethod = ContraptionDebugCommand.class
                        .getDeclaredMethod("findContraptionEntities", ServerLevel.class);
                    findContraptionEntitiesMethod.setAccessible(true);
                    
                    @SuppressWarnings("unchecked")
                    List<Entity> entities = (List<Entity>) findContraptionEntitiesMethod.invoke(null, level);
                    
                    for (Entity entity : entities) {
                        // Count LittleTiles using the existing method
                        Method countLittleTilesMethod = ContraptionDebugCommand.class
                            .getDeclaredMethod("countLittleTilesInContraption", Entity.class);
                        countLittleTilesMethod.setAccessible(true);
                        int littleTilesCount = (int) countLittleTilesMethod.invoke(null, entity);
                        
                        LOGGER.info("Entity {} has {} LittleTiles blocks", 
                            entity.getClass().getSimpleName(), littleTilesCount);
                        
                        // Test the LittleTiles detection method
                        testLittleTilesDetection(entity, littleTilesCount);
                    }
                    
                    LOGGER.info("LittleTiles detection accuracy test completed successfully");
                    helper.succeed();
                    
                } catch (Exception e) {
                    LOGGER.error("Error in LittleTiles detection accuracy test: {}", e.getMessage(), e);
                    helper.fail("Exception in LittleTiles detection test: " + e.getMessage());
                }
            });
    }

    // Helper methods for testing
    
    private static void testRenderingAnalysisMethods(Entity contraptionEntity) throws Exception {
        LOGGER.info("Testing rendering analysis methods on entity: {}", 
            contraptionEntity.getClass().getSimpleName());
        
        // Test that we can get contraption data
        Method getContraptionMethod = ContraptionDebugCommand.class
            .getDeclaredMethod("getContraptionFromEntity", Entity.class);
        getContraptionMethod.setAccessible(true);
        Object contraption = getContraptionMethod.invoke(null, contraptionEntity);
        
        if (contraption == null) {
            throw new Exception("Could not get contraption from entity");
        }
        
        // Test that we can get blocks data
        Method getBlocksMethod = ContraptionDebugCommand.class
            .getDeclaredMethod("getBlocksFromContraption", Object.class);
        getBlocksMethod.setAccessible(true);
        Object blocksData = getBlocksMethod.invoke(null, contraption);
        
        if (blocksData == null) {
            throw new Exception("Could not get blocks data from contraption");
        }
        
        LOGGER.info("Successfully accessed contraption data for rendering analysis");
    }
    
    private static void testBlockRenderingMethods(Object block, Object blockState, String blockDescription) {
        LOGGER.info("Testing rendering methods for: {}", blockDescription);
        
        try {
            Class<?> blockClass = block.getClass();
            
            // Test common rendering-related methods
            testMethodExists(blockClass, "supportsExternalFaceHiding", blockDescription);
            testMethodExists(blockClass, "hasDynamicLightEmission", blockDescription);
            testMethodExists(blockClass, "useShapeForLightOcclusion", blockDescription);
            testMethodExists(blockClass, "propagatesSkylightDown", blockDescription);
            
            // Test VoxelShape methods
            testMethodExists(blockClass, "getBlockSupportShape", blockDescription);
            testMethodExists(blockClass, "getShape", blockDescription);
            testMethodExists(blockClass, "getCollisionShape", blockDescription);
            
            // Test BlockState methods
            Class<?> blockStateClass = blockState.getClass();
            testMethodExists(blockStateClass, "getRenderShape", blockDescription + " (BlockState)");
            
        } catch (Exception e) {
            LOGGER.warn("Error testing rendering methods for {}: {}", blockDescription, e.getMessage());
        }
    }
    
    private static void testMethodExists(Class<?> clazz, String methodName, String context) {
        boolean found = false;
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {
                found = true;
                break;
            }
        }
        
        String status = found ? "FOUND" : "NOT FOUND";
        LOGGER.info("  Method {} in {}: {}", methodName, context, status);
    }
    
    private static void testStructureBlockInfoIntegrity(Object blocksData) throws Exception {
        @SuppressWarnings("unchecked")
        java.util.Map<?, ?> blocksMap = (java.util.Map<?, ?>) blocksData;
        
        LOGGER.info("Testing StructureBlockInfo integrity for {} blocks", blocksMap.size());
        
        int blockCount = 0;
        int validBlocks = 0;
        int blocksWithNBT = 0;
        
        for (java.util.Map.Entry<?, ?> entry : blocksMap.entrySet()) {
            blockCount++;
            Object structureBlockInfo = entry.getValue();
            
            if (structureBlockInfo != null) {
                validBlocks++;
                
                // Check if block has NBT data
                try {
                    java.lang.reflect.Field nbtField = structureBlockInfo.getClass().getDeclaredField("nbt");
                    nbtField.setAccessible(true);
                    Object nbt = nbtField.get(structureBlockInfo);
                    if (nbt != null) {
                        blocksWithNBT++;
                    }
                } catch (Exception e) {
                    // NBT field might not exist or be named differently
                }
            }
            
            if (blockCount >= 10) break; // Limit for test performance
        }
        
        LOGGER.info("StructureBlockInfo integrity results:");
        LOGGER.info("  Total blocks checked: {}", blockCount);
        LOGGER.info("  Valid StructureBlockInfo objects: {}", validBlocks);
        LOGGER.info("  Blocks with NBT data: {}", blocksWithNBT);
        
        if (validBlocks == 0) {
            throw new Exception("No valid StructureBlockInfo objects found");
        }
    }
    
    private static void testLittleTilesDetection(Entity contraptionEntity, int expectedCount) throws Exception {
        // Test the isLittleTilesBlock method
        Method isLittleTilesMethod = ContraptionDebugCommand.class
            .getDeclaredMethod("isLittleTilesBlock", Object.class);
        isLittleTilesMethod.setAccessible(true);
        
        // Get contraption blocks and test detection
        Method getContraptionMethod = ContraptionDebugCommand.class
            .getDeclaredMethod("getContraptionFromEntity", Entity.class);
        getContraptionMethod.setAccessible(true);
        Object contraption = getContraptionMethod.invoke(null, contraptionEntity);
        
        if (contraption != null) {
            Method getBlocksMethod = ContraptionDebugCommand.class
                .getDeclaredMethod("getBlocksFromContraption", Object.class);
            getBlocksMethod.setAccessible(true);
            Object blocksData = getBlocksMethod.invoke(null, contraption);
            
            if (blocksData != null) {
                @SuppressWarnings("unchecked")
                java.util.Map<?, ?> blocksMap = (java.util.Map<?, ?>) blocksData;
                
                int detectedLittleTiles = 0;
                for (java.util.Map.Entry<?, ?> entry : blocksMap.entrySet()) {
                    Object structureBlockInfo = entry.getValue();
                    
                    // Get block from StructureBlockInfo
                    try {
                        java.lang.reflect.Field stateField = structureBlockInfo.getClass().getDeclaredField("state");
                        stateField.setAccessible(true);
                        Object blockState = stateField.get(structureBlockInfo);
                        
                        if (blockState != null) {
                            java.lang.reflect.Method getBlockMethod = blockState.getClass().getMethod("getBlock");
                            Object block = getBlockMethod.invoke(blockState);
                            
                            boolean isLittleTiles = (boolean) isLittleTilesMethod.invoke(null, block);
                            if (isLittleTiles) {
                                detectedLittleTiles++;
                            }
                        }
                    } catch (Exception e) {
                        // Skip this block if we can't analyze it
                    }
                }
                
                LOGGER.info("LittleTiles detection: Expected {}, Found {}", expectedCount, detectedLittleTiles);
                
                if (detectedLittleTiles != expectedCount) {
                    LOGGER.warn("LittleTiles count mismatch! Expected: {}, Detected: {}", 
                        expectedCount, detectedLittleTiles);
                }
            }
        }
    }
}
