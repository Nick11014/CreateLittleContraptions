package com.createlittlecontraptions.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ContraptionDebugCommand {
    private static final Logger LOGGER = LogUtils.getLogger();    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("contraption-debug")
            .requires(source -> source.hasPermission(2))
            .executes(ContraptionDebugCommand::execute)
            .then(Commands.literal("classes")
                .executes(ContraptionDebugCommand::executeClassAnalysis))
            .then(Commands.literal("rendering")
                .executes(ContraptionDebugCommand::executeRenderingAnalysis))
            .then(Commands.literal("test-movement")
                .executes(ContraptionDebugCommand::executeMovementTest)));
    }
    
    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getLevel() instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("Command can only be used in a server world"));
            return 0;
        }
          source.sendSystemMessage(Component.literal("=== CONTRAPTION DEBUG REPORT ==="));
        
        List<Entity> contraptionEntities = findContraptionEntities(serverLevel);
        
        if (contraptionEntities.isEmpty()) {
            source.sendSystemMessage(Component.literal("No contraptions found in the world"));
            return 0;
        }
          int contraptionCount = 0;
        int totalBlocks = 0;
        int totalLittleTilesBlocks = 0;
        
        for (Entity entity : contraptionEntities) {
            contraptionCount++;
            final int currentContraptionNumber = contraptionCount; // Make final for lambda
              source.sendSystemMessage(Component.literal(""));
            source.sendSystemMessage(Component.literal("--- Contraption #" + currentContraptionNumber + " ---"));
            source.sendSystemMessage(Component.literal("Type: " + entity.getClass().getSimpleName()));
            source.sendSystemMessage(Component.literal("Position: " + entity.blockPosition()));
            source.sendSystemMessage(Component.literal("Entity ID: " + entity.getId()));
            
            try {
                // Get contraption data using reflection
                Object contraption = getContraptionFromEntity(entity);
                if (contraption != null) {
                    // Get blocks data
                    Object blocksData = getBlocksFromContraption(contraption);
                    Map<?, ?> blockEntitiesData = getBlockEntitiesFromContraption(contraption);
                    
                    int blockCount = getBlockCount(blocksData);
                    int beCount = blockEntitiesData != null ? blockEntitiesData.size() : 0;
                    int littleTilesInThisContraption = countLittleTilesInContraption(entity);                    source.sendSystemMessage(Component.literal("Total Blocks: " + blockCount));
                    source.sendSystemMessage(Component.literal("Total BlockEntities: " + beCount));
                    source.sendSystemMessage(Component.literal("LittleTiles Blocks: " + littleTilesInThisContraption));
                    
                    totalBlocks += blockCount;
                    totalLittleTilesBlocks += littleTilesInThisContraption;
                      // Show ALL blocks in the contraption
                    if (blocksData != null) {
                        source.sendSystemMessage(Component.literal("ALL BLOCKS IN CONTRAPTION:"));
                        showAllBlocks(source, blocksData);
                    }
                      // Show BlockEntity details
                    if (blockEntitiesData != null && !blockEntitiesData.isEmpty()) {
                        source.sendSystemMessage(Component.literal("BlockEntities:"));
                        int beShown = 0;
                        for (Map.Entry<?, ?> entry : blockEntitiesData.entrySet()) {
                            if (beShown >= 20) { // Show more BEs
                                final int remainingCount = beCount - 20;
                                source.sendSystemMessage(Component.literal("  ... and " + remainingCount + " more BlockEntities"));
                                break;
                            }
                            Object pos = entry.getKey();
                            Object nbtData = entry.getValue();
                            String beType = getBlockEntityType(nbtData);
                            boolean isLittleTiles = beType.contains("littletiles");
                            String marker = isLittleTiles ? " *** LITTLETILES ***" : "";
                            source.sendSystemMessage(Component.literal("  " + pos + " -> " + beType + marker));
                            beShown++;
                        }
                    }
                    
                    if (littleTilesInThisContraption > 0) {
                        final int finalLittleTilesCount = littleTilesInThisContraption;
                        source.sendSystemMessage(Component.literal("*** " + finalLittleTilesCount + " LittleTiles found in this contraption! ***"));
                    }
                }
            } catch (Exception e) {
                source.sendSystemMessage(Component.literal("Error analyzing contraption: " + e.getMessage()));
                LOGGER.warn("Error analyzing contraption {}: {}", entity.getId(), e.getMessage());
            }
        }
        
        // Make final copies for lambda usage
        final int finalContraptionCount = contraptionCount;
        final int finalTotalBlocks = totalBlocks;
        final int finalTotalLittleTilesBlocks = totalLittleTilesBlocks;
        
        source.sendSystemMessage(Component.literal(""));
        source.sendSystemMessage(Component.literal("=== SUMMARY ==="));
        source.sendSystemMessage(Component.literal("Total Contraptions: " + finalContraptionCount));
        source.sendSystemMessage(Component.literal("Total Blocks in Contraptions: " + finalTotalBlocks));
        source.sendSystemMessage(Component.literal("Total LittleTiles in Contraptions: " + finalTotalLittleTilesBlocks));
        
        if (totalLittleTilesBlocks > 0) {
            source.sendSystemMessage(Component.literal("*** WARNING: " + finalTotalLittleTilesBlocks + " LittleTiles detected in contraptions! ***"));
        }
        
        return contraptionCount;
    }
    
    private static int executeClassAnalysis(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getLevel() instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("Command can only be used in a server world"));
            return 0;
        }
        
        source.sendSystemMessage(Component.literal("=== CONTRAPTION CLASS & METHOD ANALYSIS ==="));
        
        List<Entity> contraptionEntities = findContraptionEntities(serverLevel);
        
        if (contraptionEntities.isEmpty()) {
            source.sendSystemMessage(Component.literal("No contraptions found in the world"));
            return 0;
        }
        
        Set<Class<?>> analyzedClasses = new HashSet<>();
        
        for (Entity entity : contraptionEntities) {
            source.sendSystemMessage(Component.literal(""));
            source.sendSystemMessage(Component.literal("=== CONTRAPTION ENTITY ANALYSIS ==="));
            source.sendSystemMessage(Component.literal("Entity Position: " + entity.blockPosition()));
            
            try {
                // Analyze the contraption entity class
                analyzeClass(source, entity.getClass(), "ContraptionEntity", analyzedClasses);
                
                // Get and analyze the contraption itself
                Object contraption = getContraptionFromEntity(entity);
                if (contraption != null) {
                    analyzeClass(source, contraption.getClass(), "Contraption", analyzedClasses);
                    
                    // Analyze classes of blocks within the contraption
                    analyzeContraptionInternalClasses(source, contraption, analyzedClasses);
                } else {
                    source.sendSystemMessage(Component.literal("Could not retrieve contraption data"));
                }
                
            } catch (Exception e) {
                source.sendSystemMessage(Component.literal("Error analyzing contraption: " + e.getMessage()));
                LOGGER.warn("Error in class analysis for contraption {}: {}", entity.getId(), e.getMessage());
            }
        }
        
        source.sendSystemMessage(Component.literal(""));
        source.sendSystemMessage(Component.literal("=== ANALYSIS COMPLETE ==="));
        source.sendSystemMessage(Component.literal("Total unique classes analyzed: " + analyzedClasses.size()));
        
        return contraptionEntities.size();
    }
    
    // Helper methods using reflection (based on existing dev command)
    
    private static List<Entity> findContraptionEntities(ServerLevel level) {
        List<Entity> contraptionEntities = new ArrayList<>();
        
        try {
            for (Entity entity : level.getAllEntities()) {
                if (isContraptionEntity(entity)) {
                    contraptionEntities.add(entity);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error finding contraption entities: " + e.getMessage());
        }
        
        return contraptionEntities;
    }
    
    private static boolean isContraptionEntity(Entity entity) {
        return entity instanceof AbstractContraptionEntity;
    }
    
    private static Object getContraptionFromEntity(Entity entity) {
        try {
            if (entity instanceof AbstractContraptionEntity contraptionEntity) {
                return contraptionEntity.getContraption();
            }
        } catch (Exception e) {
            LOGGER.debug("Error getting contraption from entity: " + e.getMessage());
        }
        return null;
    }
    
    private static Object getBlocksFromContraption(Object contraption) {
        try {
            Method getBlocksMethod = contraption.getClass().getMethod("getBlocks");
            return getBlocksMethod.invoke(contraption);
        } catch (Exception e) {
            LOGGER.debug("Error getting blocks from contraption: " + e.getMessage());
        }
        return null;
    }
    
    private static Map<?, ?> getBlockEntitiesFromContraption(Object contraption) {
        try {
            // Try different method names that might exist
            String[] methodNames = {"getBlockEntities", "getStoredBlockData", "getBlockEntityData"};
            
            for (String methodName : methodNames) {
                try {
                    Method method = contraption.getClass().getMethod(methodName);
                    Object result = method.invoke(contraption);
                    if (result instanceof Map<?, ?>) {
                        return (Map<?, ?>) result;
                    }
                } catch (NoSuchMethodException ignored) {
                    // Try next method name
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error getting block entities from contraption: " + e.getMessage());
        }
        return null;
    }
    
    private static int getBlockCount(Object blocksData) {
        try {
            if (blocksData instanceof Map<?, ?> map) {
                return map.size();
            } else if (blocksData instanceof java.util.Collection<?> collection) {
                return collection.size();
            }
        } catch (Exception e) {
            LOGGER.debug("Error getting block count: " + e.getMessage());
        }
        return 0;
    }
    
    private static String getBlockEntityType(Object nbtData) {
        try {
            if (nbtData instanceof CompoundTag nbt) {
                return nbt.getString("id");
            }
            // Try reflection if CompoundTag doesn't work directly
            Method getStringMethod = nbtData.getClass().getMethod("getString", String.class);
            Object result = getStringMethod.invoke(nbtData, "id");
            return result != null ? result.toString() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    private static int countLittleTilesInContraption(Entity contraptionEntity) {
        try {
            Object contraption = getContraptionFromEntity(contraptionEntity);
            if (contraption == null) return 0;
            
            Object blocksData = getBlocksFromContraption(contraption);
            if (blocksData == null) return 0;
            
            int count = 0;
            
            if (blocksData instanceof Map<?, ?> blocksMap) {
                for (Object blockData : blocksMap.values()) {
                    if (isLittleTilesBlock(blockData)) {
                        count++;
                    }
                }
            } else if (blocksData instanceof java.util.Collection<?> blocksCollection) {
                for (Object blockData : blocksCollection) {
                    if (isLittleTilesBlock(blockData)) {
                        count++;
                    }
                }
            }
            
            return count;
            
        } catch (Exception e) {
            LOGGER.debug("Error counting LittleTiles in contraption: " + e.getMessage());
            return 0;
        }
    }
    
    private static boolean isLittleTilesBlock(Object blockData) {
        try {
            // Try to get the block state from the block data
            Object blockState = null;
            
            // Try different field/method names that might contain the BlockState
            String[] accessors = {"state", "getState", "blockState", "getBlockState"};
            
            for (String accessor : accessors) {
                try {
                    if (accessor.startsWith("get")) {
                        Method method = blockData.getClass().getMethod(accessor);
                        blockState = method.invoke(blockData);
                    } else {
                        Field field = blockData.getClass().getDeclaredField(accessor);
                        field.setAccessible(true);
                        blockState = field.get(blockData);
                    }
                    
                    if (blockState != null) break;
                } catch (Exception ignored) {
                    // Try next accessor
                }
            }
            
            if (blockState != null) {
                // Try to get the block from the BlockState
                Object block = null;
                try {
                    Method getBlockMethod = blockState.getClass().getMethod("getBlock");
                    block = getBlockMethod.invoke(blockState);
                } catch (Exception ignored) {}
                
                if (block != null) {
                    String blockName = block.getClass().getName().toLowerCase();
                    return blockName.contains("littletiles");
                }
            }
            
        } catch (Exception e) {
            LOGGER.debug("Error checking if block is LittleTiles: " + e.getMessage());
        }        
        return false;
    }
      private static void showAllBlocks(CommandSourceStack source, Object blocksData) {
        try {
            if (blocksData instanceof Map<?, ?> blocksMap) {
                int blockIndex = 0;
                for (Map.Entry<?, ?> entry : blocksMap.entrySet()) {
                    if (blockIndex >= 50) { // Limit to prevent spam
                        final int remainingBlocks = blocksMap.size() - 50;
                        source.sendSystemMessage(Component.literal("  ... and " + remainingBlocks + " more blocks"));
                        break;
                    }
                    
                    Object pos = entry.getKey();
                    Object blockData = entry.getValue();
                    String blockInfo = getBlockInfo(blockData);
                    
                    final String blockLine = "  [" + (blockIndex + 1) + "] " + pos + " -> " + blockInfo;
                    source.sendSystemMessage(Component.literal(blockLine));
                    blockIndex++;
                }
            } else if (blocksData instanceof java.util.Collection<?> blocksCollection) {
                int blockIndex = 0;
                for (Object blockData : blocksCollection) {
                    if (blockIndex >= 50) { // Limit to prevent spam
                        final int remainingBlocks = blocksCollection.size() - 50;
                        source.sendSystemMessage(Component.literal("  ... and " + remainingBlocks + " more blocks"));
                        break;
                    }
                    
                    String blockInfo = getBlockInfo(blockData);
                    final String blockLine = "  [" + (blockIndex + 1) + "] " + blockInfo;
                    source.sendSystemMessage(Component.literal(blockLine));
                    blockIndex++;
                }
            }
        } catch (Exception e) {
            source.sendSystemMessage(Component.literal("  Error listing blocks: " + e.getMessage()));
            LOGGER.debug("Error showing all blocks: " + e.getMessage());
        }
    }
    
    private static String getBlockInfo(Object blockData) {
        try {
            // Try to get block state information
            Object blockState = null;
            
            // Try different field/method names for block state
            String[] stateAccessors = {"state", "getState", "blockState", "getBlockState"};
            
            for (String accessor : stateAccessors) {
                try {
                    if (accessor.startsWith("get")) {
                        Method method = blockData.getClass().getMethod(accessor);
                        blockState = method.invoke(blockData);
                    } else {
                        Field field = blockData.getClass().getDeclaredField(accessor);
                        field.setAccessible(true);
                        blockState = field.get(blockData);
                    }
                    
                    if (blockState != null) break;
                } catch (Exception ignored) {
                    // Try next accessor
                }
            }
            
            if (blockState != null) {
                // Get block from BlockState
                try {
                    Method getBlockMethod = blockState.getClass().getMethod("getBlock");
                    Object block = getBlockMethod.invoke(blockState);
                    
                    if (block != null) {
                        // Try to get block name
                        try {
                            Method getNameMethod = block.getClass().getMethod("getName");
                            Object nameComponent = getNameMethod.invoke(block);
                            
                            if (nameComponent != null) {
                                Method getStringMethod = nameComponent.getClass().getMethod("getString");
                                String blockName = (String) getStringMethod.invoke(nameComponent);
                                
                                // Check if it's a LittleTiles block
                                boolean isLittleTiles = block.getClass().getName().toLowerCase().contains("littletiles");
                                String marker = isLittleTiles ? " *** LITTLETILES ***" : "";
                                
                                return blockName + marker + " (" + block.getClass().getSimpleName() + ")";
                            }
                        } catch (Exception ignored) {}
                        
                        // Fallback to class name
                        boolean isLittleTiles = block.getClass().getName().toLowerCase().contains("littletiles");
                        String marker = isLittleTiles ? " *** LITTLETILES ***" : "";
                        return block.getClass().getSimpleName() + marker;
                    }
                } catch (Exception ignored) {}
            }
            
            // Final fallback
            return blockData.getClass().getSimpleName() + " (unknown block)";
            
        } catch (Exception e) {
            return "Error getting block info: " + e.getMessage();
        }
    }
    
    private static void analyzeClass(CommandSourceStack source, Class<?> clazz, String classType, Set<Class<?>> analyzedClasses) {
        if (analyzedClasses.contains(clazz)) {
            source.sendSystemMessage(Component.literal("--- " + classType + " Class: " + clazz.getSimpleName() + " (already analyzed) ---"));
            return;
        }
        
        analyzedClasses.add(clazz);
        
        source.sendSystemMessage(Component.literal(""));
        source.sendSystemMessage(Component.literal("--- " + classType + " Class Analysis ---"));
        source.sendSystemMessage(Component.literal("Full Class Name: " + clazz.getName()));
        source.sendSystemMessage(Component.literal("Simple Name: " + clazz.getSimpleName()));
        source.sendSystemMessage(Component.literal("Package: " + (clazz.getPackage() != null ? clazz.getPackage().getName() : "default")));
        
        // Show superclass
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && !superclass.equals(Object.class)) {
            source.sendSystemMessage(Component.literal("Extends: " + superclass.getSimpleName()));
        }
        
        // Show interfaces
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            String interfaceList = Arrays.stream(interfaces)
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", "));
            source.sendSystemMessage(Component.literal("Implements: " + interfaceList));
        }
        
        // Analyze methods
        analyzeMethods(source, clazz);
    }
    
    private static void analyzeMethods(CommandSourceStack source, Class<?> clazz) {
        source.sendSystemMessage(Component.literal(""));
        source.sendSystemMessage(Component.literal("--- Methods Analysis ---"));
        
        Method[] methods = clazz.getDeclaredMethods();
        Method[] inheritedMethods = clazz.getMethods();
        
        source.sendSystemMessage(Component.literal("Declared Methods (" + methods.length + "):"));
        
        int methodCount = 0;
        for (Method method : methods) {
            if (methodCount >= 15) { // Limit to prevent spam
                source.sendSystemMessage(Component.literal("  ... and " + (methods.length - 15) + " more declared methods"));
                break;
            }
            
            String methodSignature = formatMethodSignature(method);
            String accessibility = getMethodAccessibility(method);
            
            source.sendSystemMessage(Component.literal("  [" + accessibility + "] " + methodSignature));
            methodCount++;
        }
        
        // Show inherited methods (filtered)
        List<Method> filteredInherited = Arrays.stream(inheritedMethods)
            .filter(m -> !m.getDeclaringClass().equals(clazz)) // Not declared in this class
            .filter(m -> !m.getDeclaringClass().equals(Object.class)) // Not from Object
            .limit(10) // Limit inherited methods shown
            .collect(Collectors.toList());
        
        if (!filteredInherited.isEmpty()) {
            source.sendSystemMessage(Component.literal(""));
            source.sendSystemMessage(Component.literal("Key Inherited Methods (" + filteredInherited.size() + " shown):"));
            
            for (Method method : filteredInherited) {
                String methodSignature = formatMethodSignature(method);
                String fromClass = method.getDeclaringClass().getSimpleName();
                source.sendSystemMessage(Component.literal("  [inherited from " + fromClass + "] " + methodSignature));
            }
        }
    }
    
    private static String formatMethodSignature(Method method) {
        StringBuilder signature = new StringBuilder();
        
        // Return type
        signature.append(method.getReturnType().getSimpleName()).append(" ");
        
        // Method name
        signature.append(method.getName()).append("(");
        
        // Parameters
        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0) signature.append(", ");
            signature.append(paramTypes[i].getSimpleName());
        }
        
        signature.append(")");
        
        // Exceptions
        Class<?>[] exceptions = method.getExceptionTypes();
        if (exceptions.length > 0) {
            signature.append(" throws ");
            for (int i = 0; i < exceptions.length; i++) {
                if (i > 0) signature.append(", ");
                signature.append(exceptions[i].getSimpleName());
            }
        }
        
        return signature.toString();
    }
    
    private static String getMethodAccessibility(Method method) {
        int modifiers = method.getModifiers();
        
        if (java.lang.reflect.Modifier.isPublic(modifiers)) return "public";
        if (java.lang.reflect.Modifier.isProtected(modifiers)) return "protected";
        if (java.lang.reflect.Modifier.isPrivate(modifiers)) return "private";
        
        return "package"; // package-private
    }
    
    private static void analyzeContraptionInternalClasses(CommandSourceStack source, Object contraption, Set<Class<?>> analyzedClasses) {
        source.sendSystemMessage(Component.literal(""));
        source.sendSystemMessage(Component.literal("=== INTERNAL BLOCK CLASSES ANALYSIS ==="));
        
        try {
            // Get blocks data
            Object blocksData = getBlocksFromContraption(contraption);
            if (blocksData == null) {
                source.sendSystemMessage(Component.literal("No blocks data found in contraption"));
                return;
            }
            
            Set<Class<?>> blockClasses = new HashSet<>();
            
            if (blocksData instanceof Map<?, ?> blocksMap) {
                for (Object blockData : blocksMap.values()) {
                    Class<?> blockClass = getBlockClassFromData(blockData);
                    if (blockClass != null) {
                        blockClasses.add(blockClass);
                    }
                }
            } else if (blocksData instanceof java.util.Collection<?> blocksCollection) {
                for (Object blockData : blocksCollection) {
                    Class<?> blockClass = getBlockClassFromData(blockData);
                    if (blockClass != null) {
                        blockClasses.add(blockClass);
                    }
                }
            }
            
            source.sendSystemMessage(Component.literal("Found " + blockClasses.size() + " unique block classes:"));
            
            for (Class<?> blockClass : blockClasses) {
                String classType = blockClass.getName().toLowerCase().contains("littletiles") 
                    ? "LittleTiles Block" 
                    : "Block";
                
                analyzeClass(source, blockClass, classType, analyzedClasses);
            }
            
            // Analyze BlockEntity classes
            analyzeBlockEntityClasses(source, contraption, analyzedClasses);
            
        } catch (Exception e) {
            source.sendSystemMessage(Component.literal("Error analyzing contraption internal classes: " + e.getMessage()));
            LOGGER.warn("Error analyzing contraption internal classes: {}", e.getMessage());
        }
    }
    
    private static Class<?> getBlockClassFromData(Object blockData) {
        try {
            // Try to get block state from block data
            Object blockState = null;
            
            String[] stateAccessors = {"state", "getState", "blockState", "getBlockState"};
            
            for (String accessor : stateAccessors) {
                try {
                    if (accessor.startsWith("get")) {
                        Method method = blockData.getClass().getMethod(accessor);
                        blockState = method.invoke(blockData);
                    } else {
                        Field field = blockData.getClass().getDeclaredField(accessor);
                        field.setAccessible(true);
                        blockState = field.get(blockData);
                    }
                    
                    if (blockState != null) break;
                } catch (Exception ignored) {
                    // Try next accessor
                }
            }
            
            if (blockState != null) {
                // Get block from BlockState
                try {
                    Method getBlockMethod = blockState.getClass().getMethod("getBlock");
                    Object block = getBlockMethod.invoke(blockState);
                    
                    if (block != null) {
                        return block.getClass();
                    }
                } catch (Exception ignored) {}
            }
            
        } catch (Exception e) {
            LOGGER.debug("Error getting block class from data: " + e.getMessage());
        }
        
        return null;
    }
    
    private static void analyzeBlockEntityClasses(CommandSourceStack source, Object contraption, Set<Class<?>> analyzedClasses) {
        try {
            Map<?, ?> blockEntitiesData = getBlockEntitiesFromContraption(contraption);
            if (blockEntitiesData == null || blockEntitiesData.isEmpty()) {
                source.sendSystemMessage(Component.literal("No BlockEntity data found in contraption"));
                return;
            }
              source.sendSystemMessage(Component.literal(""));
            source.sendSystemMessage(Component.literal("=== BLOCK ENTITY CLASSES ANALYSIS ==="));
            
            Set<Class<?>> beClasses = new HashSet<>();
            
            for (Object nbtData : blockEntitiesData.values()) {
                if (nbtData != null) {
                    beClasses.add(nbtData.getClass());
                }
            }
            
            source.sendSystemMessage(Component.literal("Found " + beClasses.size() + " unique BlockEntity data classes:"));
            
            for (Class<?> beClass : beClasses) {
                String classType = beClass.getName().toLowerCase().contains("littletiles") 
                    ? "LittleTiles BlockEntity Data" 
                    : "BlockEntity Data";
                
                analyzeClass(source, beClass, classType, analyzedClasses);
            }
            
        } catch (Exception e) {            source.sendSystemMessage(Component.literal("Error analyzing BlockEntity classes: " + e.getMessage()));
            LOGGER.warn("Error analyzing BlockEntity classes: {}", e.getMessage());
        }
    }
    
    private static int executeRenderingAnalysis(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getLevel() instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("Command can only be used in a server world"));
            return 0;
        }
        
        source.sendSystemMessage(Component.literal("=== CONTRAPTION RENDERING METHOD ANALYSIS ==="));
        source.sendSystemMessage(Component.literal("§6Focus: Static elevator contraption (no movement complications)"));
        source.sendSystemMessage(Component.literal("§6Objective: Compare rendering behavior - Common blocks vs LittleTiles"));
        
        List<Entity> contraptionEntities = findContraptionEntities(serverLevel);
        
        if (contraptionEntities.isEmpty()) {
            source.sendSystemMessage(Component.literal("No contraptions found in the world"));
            return 0;
        }
        
        int contraptionCount = 0;
        int analyzedBlocks = 0;
        int littleTilesAnalyzed = 0;
        
        for (Entity entity : contraptionEntities) {
            contraptionCount++;
            source.sendSystemMessage(Component.literal(""));
            source.sendSystemMessage(Component.literal("--- Contraption #" + contraptionCount + " Rendering Analysis ---"));
            source.sendSystemMessage(Component.literal("Type: " + entity.getClass().getSimpleName()));
            source.sendSystemMessage(Component.literal("Position: " + entity.blockPosition()));
            
            try {
                // Get contraption data
                Object contraption = getContraptionFromEntity(entity);
                if (contraption != null) {
                    Object blocksData = getBlocksFromContraption(contraption);
                    
                    if (blocksData != null) {
                        source.sendSystemMessage(Component.literal(""));
                        source.sendSystemMessage(Component.literal("§e=== RENDERING METHOD ANALYSIS ==="));
                        
                        // Analyze each block for rendering methods
                        int blocksInContraption = analyzeBlocksRendering(source, blocksData, entity);
                        analyzedBlocks += blocksInContraption;
                        
                        // Count LittleTiles specifically
                        int littleTilesInContraption = countLittleTilesInContraption(entity);
                        littleTilesAnalyzed += littleTilesInContraption;
                        
                        // Analyze contraption entity rendering methods
                        source.sendSystemMessage(Component.literal(""));
                        source.sendSystemMessage(Component.literal("§e=== CONTRAPTION ENTITY RENDERING METHODS ==="));
                        analyzeContraptionEntityRendering(source, entity);
                    }
                }
            } catch (Exception e) {
                source.sendSystemMessage(Component.literal("§cError analyzing contraption rendering: " + e.getMessage()));
                LOGGER.warn("Error analyzing contraption rendering {}: {}", entity.getId(), e.getMessage());
            }
        }
        
        // Summary
        source.sendSystemMessage(Component.literal(""));
        source.sendSystemMessage(Component.literal("=== RENDERING ANALYSIS SUMMARY ==="));
        source.sendSystemMessage(Component.literal("Contraptions Analyzed: " + contraptionCount));
        source.sendSystemMessage(Component.literal("Total Blocks Analyzed: " + analyzedBlocks));
        source.sendSystemMessage(Component.literal("LittleTiles Analyzed: " + littleTilesAnalyzed));
        
        if (littleTilesAnalyzed > 0) {
            source.sendSystemMessage(Component.literal("§c*** " + littleTilesAnalyzed + " LittleTiles detected - Check rendering method differences! ***"));
        }
        
        return contraptionCount;
    }
    
    private static int analyzeBlocksRendering(CommandSourceStack source, Object blocksData, Entity contraptionEntity) {
        try {
            // Get the blocks map
            Map<?, ?> blocksMap = (Map<?, ?>) blocksData;
            int blockCount = 0;
            int littleTilesCount = 0;
            int commonBlocksCount = 0;
            
            source.sendSystemMessage(Component.literal("§6Analyzing " + blocksMap.size() + " blocks for rendering methods..."));
            
            for (Map.Entry<?, ?> entry : blocksMap.entrySet()) {
                Object pos = entry.getKey();
                Object structureBlockInfo = entry.getValue();
                blockCount++;
                
                // Get BlockState from StructureBlockInfo
                Object blockState = getBlockStateFromStructureBlockInfo(structureBlockInfo);
                if (blockState != null) {
                    Object block = getBlockFromBlockState(blockState);
                    
                    if (block != null) {
                        String blockName = block.getClass().getSimpleName();
                        boolean isLittleTiles = isLittleTilesBlock(block);
                          if (isLittleTiles) {
                            littleTilesCount++;
                            source.sendSystemMessage(Component.literal(""));
                            source.sendSystemMessage(Component.literal("§c*** LITTLETILES BLOCK ANALYSIS ***"));
                            source.sendSystemMessage(Component.literal("Position: " + pos));
                            source.sendSystemMessage(Component.literal("Block: " + blockName));
                            analyzeBlockRenderingMethods(source, block, blockState, pos, true);
                        } else {
                            commonBlocksCount++;
                            // Analyze first few common blocks for comparison
                            if (commonBlocksCount <= 3) {
                                source.sendSystemMessage(Component.literal(""));
                                source.sendSystemMessage(Component.literal("§a--- Common Block Analysis ---"));
                                source.sendSystemMessage(Component.literal("Position: " + pos));
                                source.sendSystemMessage(Component.literal("Block: " + blockName));
                                analyzeBlockRenderingMethods(source, block, blockState, pos, false);
                            }
                        }
                    }
                }
                
                // No block count limitation - show all blocks
                blockCount++;
            }
            
            source.sendSystemMessage(Component.literal(""));
            source.sendSystemMessage(Component.literal("§e--- Block Analysis Summary ---"));
            source.sendSystemMessage(Component.literal("Total blocks: " + blockCount));
            source.sendSystemMessage(Component.literal("LittleTiles blocks: " + littleTilesCount));
            source.sendSystemMessage(Component.literal("Common blocks: " + commonBlocksCount));
            
            return blockCount;
            
        } catch (Exception e) {
            source.sendSystemMessage(Component.literal("§cError analyzing blocks rendering: " + e.getMessage()));
            LOGGER.warn("Error analyzing blocks rendering: {}", e.getMessage());
            return 0;
        }
    }
      private static void analyzeBlockRenderingMethods(CommandSourceStack source, Object block, Object blockState, Object pos, boolean isLittleTiles) {
        try {
            Class<?> blockClass = block.getClass();
            Class<?> blockStateClass = blockState.getClass();
            String blockType = isLittleTiles ? "§cLittleTiles" : "§aCommon";
            
            source.sendSystemMessage(Component.literal(blockType + " Block Class: " + blockClass.getName()));
            source.sendSystemMessage(Component.literal(blockType + " BlockState Class: " + blockStateClass.getName()));
            
            // Test critical rendering methods - many are on BlockState, not Block
            testMethod(source, block, blockClass, "supportsExternalFaceHiding", blockState, isLittleTiles);
            testMethod(source, blockState, blockStateClass, "supportsExternalFaceHiding", null, isLittleTiles);
            
            testMethod(source, block, blockClass, "hasDynamicLightEmission", blockState, isLittleTiles);
            testMethod(source, blockState, blockStateClass, "hasDynamicLightEmission", null, isLittleTiles);
            
            testMethod(source, block, blockClass, "useShapeForLightOcclusion", blockState, isLittleTiles);
            testMethod(source, blockState, blockStateClass, "useShapeForLightOcclusion", null, isLittleTiles);
            
            testMethod(source, block, blockClass, "propagatesSkylightDown", blockState, isLittleTiles);
            testMethod(source, blockState, blockStateClass, "propagatesSkylightDown", null, isLittleTiles);
            
            // Test state methods
            testMethod(source, blockState, blockStateClass, "getRenderShape", null, isLittleTiles);
            testMethod(source, block, blockClass, "getRenderShape", blockState, isLittleTiles);
            
            // Show all rendering-related methods in both Block and BlockState
            source.sendSystemMessage(Component.literal(blockType + " ALL Block Methods:"));
            showAllRenderingMethods(source, blockClass, isLittleTiles, "Block");
            
            source.sendSystemMessage(Component.literal(blockType + " ALL BlockState Methods:"));
            showAllRenderingMethods(source, blockStateClass, isLittleTiles, "BlockState");
            
        } catch (Exception e) {
            source.sendSystemMessage(Component.literal("§cError analyzing block rendering methods: " + e.getMessage()));
            LOGGER.warn("Error analyzing block rendering methods: {}", e.getMessage());
        }
    }
    
    private static void testMethod(CommandSourceStack source, Object instance, Class<?> clazz, String methodName, Object parameter, boolean isLittleTiles) {
        try {
            String prefix = isLittleTiles ? "§c  LT" : "§a  CB";
            
            // Try to find the method with different parameter combinations
            Method method = null;
            try {
                if (parameter != null) {
                    method = clazz.getMethod(methodName, parameter.getClass());
                } else {
                    method = clazz.getMethod(methodName);
                }
            } catch (NoSuchMethodException e1) {
                try {
                    if (parameter != null) {
                        method = clazz.getDeclaredMethod(methodName, parameter.getClass());
                    } else {
                        method = clazz.getDeclaredMethod(methodName);
                    }
                } catch (NoSuchMethodException e2) {
                    // Try to find any method with this name
                    for (Method m : clazz.getMethods()) {
                        if (m.getName().equals(methodName)) {
                            method = m;
                            break;
                        }
                    }
                    if (method == null) {
                        source.sendSystemMessage(Component.literal(prefix + " " + methodName + ": §7Method not found"));
                        return;
                    }
                }
            }
            
            if (method != null) {
                method.setAccessible(true);
                Object result;
                if (parameter != null && method.getParameterCount() > 0) {
                    result = method.invoke(instance, parameter);
                } else if (method.getParameterCount() == 0) {
                    result = method.invoke(instance);
                } else {
                    result = "§7Found (params: " + method.getParameterCount() + ")";
                }
                source.sendSystemMessage(Component.literal(prefix + " " + methodName + ": §f" + result));
            }
            
        } catch (Exception e) {
            source.sendSystemMessage(Component.literal((isLittleTiles ? "§c  LT" : "§a  CB") + " " + methodName + ": §7Error - " + e.getMessage()));
        }
    }
    
    private static void showAllRenderingMethods(CommandSourceStack source, Class<?> clazz, boolean isLittleTiles, String classType) {
        String prefix = isLittleTiles ? "§c    " : "§a    ";
        Method[] methods = clazz.getMethods();
        
        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.contains("render") || methodName.contains("Render") || 
                methodName.contains("shape") || methodName.contains("Shape") ||
                methodName.contains("light") || methodName.contains("Light") ||
                methodName.contains("collision") || methodName.contains("Collision") ||
                methodName.contains("support") || methodName.contains("Support") ||
                methodName.contains("occlusion") || methodName.contains("Occlusion") ||
                methodName.contains("face") || methodName.contains("Face") ||
                methodName.contains("dynamic") || methodName.contains("Dynamic") ||
                methodName.contains("skylight") || methodName.contains("Skylight")) {
                source.sendSystemMessage(Component.literal(prefix + classType + "." + methodName + "(" + method.getParameterCount() + " params)"));
            }
        }
    }
      private static void analyzeContraptionEntityRendering(CommandSourceStack source, Entity contraptionEntity) {
        try {
            Class<?> entityClass = contraptionEntity.getClass();
            source.sendSystemMessage(Component.literal("§6Contraption Entity: " + entityClass.getSimpleName()));
            
            // Test position and rotation methods critical for rendering
            testEntityMethod(source, contraptionEntity, "blockPosition");
            testEntityMethod(source, contraptionEntity, "position");
            
            // Test contraption-specific methods if available
            if (contraptionEntity instanceof AbstractContraptionEntity) {
                source.sendSystemMessage(Component.literal("§6Testing AbstractContraptionEntity methods..."));
                testEntityMethod(source, contraptionEntity, "getAngle", float.class);
                testEntityMethod(source, contraptionEntity, "getRotationAxis");
            }
            
            // List key rendering-related methods
            source.sendSystemMessage(Component.literal("§6Key rendering methods available:"));            Method[] methods = entityClass.getMethods();
            for (Method method : methods) {
                String methodName = method.getName();
                if (methodName.contains("render") || methodName.contains("Render") || 
                    methodName.contains("angle") || methodName.contains("Angle") ||
                    methodName.contains("rotation") || methodName.contains("Rotation") ||
                    methodName.contains("position") || methodName.contains("Position") ||
                    methodName.contains("transform") || methodName.contains("Transform") ||
                    methodName.contains("matrix") || methodName.contains("Matrix") ||
                    methodName.contains("lighting") || methodName.contains("Lighting") ||
                    methodName.contains("view") || methodName.contains("View")) {
                    source.sendSystemMessage(Component.literal("  §7- " + methodName + "(" + method.getParameterCount() + " params)"));
                }
            }
            
        } catch (Exception e) {
            source.sendSystemMessage(Component.literal("§cError analyzing contraption entity rendering: " + e.getMessage()));
            LOGGER.warn("Error analyzing contraption entity rendering: {}", e.getMessage());
        }
    }
    
    private static void testEntityMethod(CommandSourceStack source, Entity entity, String methodName, Class<?>... paramTypes) {
        try {
            Method method = entity.getClass().getMethod(methodName, paramTypes);
            method.setAccessible(true);
            
            Object result;
            if (paramTypes.length == 0) {
                result = method.invoke(entity);
            } else if (paramTypes.length == 1 && paramTypes[0] == float.class) {
                // For methods like getAngle(float partialTicks), use 0.0f
                result = method.invoke(entity, 0.0f);
            } else {
                source.sendSystemMessage(Component.literal("  §7" + methodName + ": Complex parameters, skipping"));
                return;
            }
            
            source.sendSystemMessage(Component.literal("  §6" + methodName + ": §f" + result));
            
        } catch (Exception e) {
            source.sendSystemMessage(Component.literal("  §7" + methodName + ": Error or not found - " + e.getMessage()));
        }
    }
    
    // Helper methods for accessing StructureBlockInfo data
    private static Object getBlockStateFromStructureBlockInfo(Object structureBlockInfo) {
        try {
            Field stateField = structureBlockInfo.getClass().getDeclaredField("state");
            stateField.setAccessible(true);
            return stateField.get(structureBlockInfo);
        } catch (Exception e) {
            LOGGER.warn("Could not get BlockState from StructureBlockInfo: {}", e.getMessage());
            return null;
        }
    }
    
    private static Object getBlockFromBlockState(Object blockState) {
        try {
            Method getBlockMethod = blockState.getClass().getMethod("getBlock");
            return getBlockMethod.invoke(blockState);
        } catch (Exception e) {
            LOGGER.warn("Could not get Block from BlockState: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Tests the MovementBehaviour registration and functionality.
     */
    private static int executeMovementTest(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getLevel() instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("Command can only be used in a server world"));
            return 0;
        }
        
        source.sendSystemMessage(Component.literal("=== MOVEMENT BEHAVIOUR TEST ==="));
        
        // Check if Create movement system is available
        try {
            Class<?> allMovementBehaviours = Class.forName("com.simibubi.create.AllMovementBehaviours");
            source.sendSystemMessage(Component.literal("✅ Create AllMovementBehaviours class found"));            // Check for LittleTiles block
            net.minecraft.world.level.block.Block littleTilesBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("littletiles", "tiles")
            );
            
            if (littleTilesBlock != null && !littleTilesBlock.equals(net.minecraft.world.level.block.Blocks.AIR)) {
                source.sendSystemMessage(Component.literal("✅ LittleTiles block found: " + littleTilesBlock));
                
                // Try to access registered behaviours
                try {
                    Method getBehaviourMethod = allMovementBehaviours.getMethod("getBehaviour", net.minecraft.world.level.block.state.BlockState.class);
                    Object behaviour = getBehaviourMethod.invoke(null, littleTilesBlock.defaultBlockState());
                    
                    if (behaviour != null) {
                        source.sendSystemMessage(Component.literal("✅ LittleTilesMovementBehaviour registered: " + behaviour.getClass().getSimpleName()));
                        
                        // Test if it's our implementation
                        if (behaviour instanceof com.createlittlecontraptions.compat.littletiles.LittleTilesMovementBehaviour) {
                            source.sendSystemMessage(Component.literal("✅ MovementBehaviour is our custom implementation"));
                        } else {
                            source.sendSystemMessage(Component.literal("⚠️ MovementBehaviour is different implementation: " + behaviour.getClass().getName()));
                        }
                    } else {
                        source.sendSystemMessage(Component.literal("❌ No MovementBehaviour registered for LittleTiles"));
                    }
                    
                } catch (Exception e) {
                    source.sendSystemMessage(Component.literal("❌ Error checking MovementBehaviour registration: " + e.getMessage()));
                }
                
            } else {
                source.sendSystemMessage(Component.literal("❌ LittleTiles block not found"));
            }
            
        } catch (ClassNotFoundException e) {
            source.sendSystemMessage(Component.literal("❌ Create AllMovementBehaviours not found - Create mod missing?"));
            return 0;
        }
        
        // Test debug settings
        boolean debugEnabled = com.createlittlecontraptions.compat.littletiles.LittleTilesAPIFacade.isDebugEnabled();
        source.sendSystemMessage(Component.literal("Debug logging: " + (debugEnabled ? "§aENABLED" : "§cDISABLED")));
        
        // Enable debug for testing
        if (!debugEnabled) {
            com.createlittlecontraptions.compat.littletiles.LittleTilesAPIFacade.setDebugEnabled(true);
            source.sendSystemMessage(Component.literal("§aEnabled debug logging for testing"));
        }
        
        source.sendSystemMessage(Component.literal("=== MOVEMENT TEST COMPLETED ==="));
        return 1;
    }
}
