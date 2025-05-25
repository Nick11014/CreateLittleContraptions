package com.createlittlecontraptions.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Debug command to help diagnose and fix LittleTiles rendering issues in Create contraptions.
 */
public class ContraptionDebugCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
      public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("clc-debug")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("contraptions")
                .executes(ContraptionDebugCommand::debugContraptions))
            .then(Commands.literal("littletiles")
                .executes(ContraptionDebugCommand::debugLittleTiles))
            .then(Commands.literal("rendering")
                .executes(ContraptionDebugCommand::debugRendering))
            .then(Commands.literal("integration")
                .executes(ContraptionDebugCommand::debugIntegration))
            .then(Commands.literal("fix")
                .executes(ContraptionDebugCommand::forceRenderingFix))
        );
    }
    
    /**
     * Debug contraptions in the world.
     */
    private static int debugContraptions(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            Level level = source.getLevel();
            
            source.sendSuccess(() -> Component.literal("🔍 Scanning for Create contraptions..."), false);
            
            List<Entity> contraptionEntities = findContraptionEntities(level);
            
            if (contraptionEntities.isEmpty()) {
                source.sendSuccess(() -> Component.literal("❌ No Create contraptions found in the world"), false);
            } else {
                source.sendSuccess(() -> Component.literal("✅ Found " + contraptionEntities.size() + " contraption(s):"), false);
                
                for (int i = 0; i < contraptionEntities.size(); i++) {
                    Entity entity = contraptionEntities.get(i);
                    String info = String.format("[%d] %s at (%.1f, %.1f, %.1f)", 
                        i + 1,
                        entity.getClass().getSimpleName(),
                        entity.getX(), entity.getY(), entity.getZ()
                    );
                    source.sendSuccess(() -> Component.literal("  " + info), false);
                }
            }
            
            return contraptionEntities.size();
            
        } catch (Exception e) {
            LOGGER.error("Error debugging contraptions", e);
            context.getSource().sendFailure(Component.literal("❌ Error scanning contraptions: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * Debug LittleTiles detection.
     */
    private static int debugLittleTiles(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            
            source.sendSuccess(() -> Component.literal("🔍 Testing LittleTiles detection..."), false);
            
            // Test LittleTiles mod detection
            boolean littleTilesFound = testLittleTilesDetection();
            
            if (littleTilesFound) {
                source.sendSuccess(() -> Component.literal("✅ LittleTiles mod detected successfully!"), false);
            } else {
                source.sendSuccess(() -> Component.literal("❌ LittleTiles mod NOT detected"), false);
            }
            
            // Test LittleTiles blocks in contraptions
            Level level = source.getLevel();
            int littleTilesBlocksFound = scanForLittleTilesInContraptions(level, source);
            
            source.sendSuccess(() -> Component.literal("📊 Total LittleTiles blocks in contraptions: " + littleTilesBlocksFound), false);
            
            return littleTilesBlocksFound;
            
        } catch (Exception e) {
            LOGGER.error("Error debugging LittleTiles", e);
            context.getSource().sendFailure(Component.literal("❌ Error testing LittleTiles: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * Debug rendering system.
     */
    private static int debugRendering(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            
            source.sendSuccess(() -> Component.literal("🔍 Testing rendering system..."), false);
            
            // Test Create renderer access
            boolean createRenderingAccessible = testCreateRenderingAccess();
            source.sendSuccess(() -> Component.literal("Create rendering: " + (createRenderingAccessible ? "✅ Accessible" : "❌ Not accessible")), false);
            
            // Test LittleTiles renderer access
            boolean littleTilesRenderingAccessible = testLittleTilesRenderingAccess();
            source.sendSuccess(() -> Component.literal("LittleTiles rendering: " + (littleTilesRenderingAccessible ? "✅ Accessible" : "❌ Not accessible")), false);
            
            // Test integration status
            boolean integrationActive = isIntegrationActive();
            source.sendSuccess(() -> Component.literal("Integration status: " + (integrationActive ? "✅ Active" : "❌ Inactive")), false);
            
            return integrationActive ? 1 : 0;
            
        } catch (Exception e) {
            LOGGER.error("Error debugging rendering", e);
            context.getSource().sendFailure(Component.literal("❌ Error testing rendering: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * Debug the integration system status.
     */
    private static int debugIntegration(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            
            source.sendSuccess(() -> Component.literal("🔍 Testing CreateRuntimeIntegration status..."), false);
            
            // Get debug status from the integration system
            try {
                Class<?> integrationClass = Class.forName("com.createlittlecontraptions.compat.create.CreateRuntimeIntegration");
                Method getDebugStatusMethod = integrationClass.getMethod("getDebugStatus");
                String debugStatus = (String) getDebugStatusMethod.invoke(null);
                
                // Send each line as a separate message for better formatting
                String[] lines = debugStatus.split("\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        final String finalLine = line;
                        source.sendSuccess(() -> Component.literal("📋 " + finalLine), false);
                    }
                }
                
                // Force a manual rendering check
                source.sendSuccess(() -> Component.literal("🔧 Forcing rendering check..."), false);
                Method forceRenderingCheckMethod = integrationClass.getMethod("forceRenderingCheck");
                forceRenderingCheckMethod.invoke(null);
                source.sendSuccess(() -> Component.literal("✅ Forced rendering check completed - check console logs"), false);
                
            } catch (ClassNotFoundException e) {
                source.sendSuccess(() -> Component.literal("❌ CreateRuntimeIntegration class not found!"), false);
                return 0;
            } catch (Exception e) {
                source.sendSuccess(() -> Component.literal("❌ Error accessing integration: " + e.getMessage()), false);
                LOGGER.error("Error in debug integration", e);
                return 0;
            }
            
            source.sendSuccess(() -> Component.literal("🎉 Integration debug completed!"), false);
            return 1;
            
        } catch (Exception e) {
            LOGGER.error("Error debugging integration", e);
            context.getSource().sendFailure(Component.literal("❌ Error debugging integration: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * Force rendering fix for all contraptions.
     */
    private static int forceRenderingFix(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            Level level = source.getLevel();
              source.sendSuccess(() -> Component.literal("🔧 Applying rendering fix to all contraptions..."), false);
            
            List<Entity> contraptionEntities = findContraptionEntities(level);
            final int[] fixedCount = {0}; // Use array to make it effectively final
            
            for (Entity entity : contraptionEntities) {
                try {
                    boolean fixed = applyRenderingFixToContraption(entity);
                    if (fixed) {
                        fixedCount[0]++;
                        source.sendSuccess(() -> Component.literal("✅ Fixed contraption: " + entity.getClass().getSimpleName()), false);
                    } else {
                        source.sendSuccess(() -> Component.literal("⚠️ Could not fix contraption: " + entity.getClass().getSimpleName()), false);
                    }
                } catch (Exception e) {
                    source.sendSuccess(() -> Component.literal("❌ Error fixing contraption: " + e.getMessage()), false);
                }
            }
            
            final int totalFixed = fixedCount[0];
            final int totalContraptions = contraptionEntities.size();
            source.sendSuccess(() -> Component.literal("🎉 Fixed " + totalFixed + " out of " + totalContraptions + " contraptions"), false);
            
            return fixedCount[0];
            
        } catch (Exception e) {
            LOGGER.error("Error forcing rendering fix", e);
            context.getSource().sendFailure(Component.literal("❌ Error applying fix: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * Find all contraption entities in the level.
     */
    private static List<Entity> findContraptionEntities(Level level) {
        List<Entity> contraptionEntities = new ArrayList<>();
        
        try {
            if (level instanceof ServerLevel serverLevel) {
                for (Entity entity : serverLevel.getAllEntities()) {
                    if (isContraptionEntity(entity)) {
                        contraptionEntities.add(entity);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error finding contraption entities: " + e.getMessage());
        }
        
        return contraptionEntities;
    }
    
    /**
     * Check if an entity is a Create contraption entity.
     */
    private static boolean isContraptionEntity(Entity entity) {
        String className = entity.getClass().getName().toLowerCase();
        return className.contains("contraption") || 
               className.contains("create") && className.contains("entity");
    }
    
    /**
     * Test LittleTiles mod detection.
     */
    private static boolean testLittleTilesDetection() {
        try {
            // Try NeoForge ModList first
            Class<?> modListClass = Class.forName("net.neoforged.fml.ModList");
            Object modListInstance = modListClass.getMethod("get").invoke(null);
            return (Boolean) modListClass.getMethod("isLoaded", String.class).invoke(modListInstance, "littletiles");
        } catch (Exception e) {
            // Try class detection
            String[] classes = {
                "team.creative.littletiles.LittleTiles",
                "team.creative.littletiles.LittleTiles"
            };
            
            for (String className : classes) {
                try {
                    Class.forName(className);
                    return true;
                } catch (ClassNotFoundException ignored) {}
            }
        }
        
        return false;
    }
    
    /**
     * Scan for LittleTiles blocks in contraptions.
     */
    private static int scanForLittleTilesInContraptions(Level level, CommandSourceStack source) {
        int count = 0;
        
        try {
            List<Entity> contraptionEntities = findContraptionEntities(level);
            
            for (Entity entity : contraptionEntities) {
                int blocksInThisContraption = countLittleTilesInContraption(entity);
                if (blocksInThisContraption > 0) {
                    count += blocksInThisContraption;
                    source.sendSuccess(() -> Component.literal("  🧱 " + entity.getClass().getSimpleName() + " contains " + blocksInThisContraption + " LittleTiles blocks"), false);
                }
            }
            
        } catch (Exception e) {
            LOGGER.debug("Error scanning for LittleTiles in contraptions: " + e.getMessage());
        }
        
        return count;
    }
    
    /**
     * Count LittleTiles blocks in a specific contraption.
     */
    private static int countLittleTilesInContraption(Entity contraptionEntity) {
        try {
            // Get contraption data
            Object contraption = getContraptionFromEntity(contraptionEntity);
            if (contraption == null) return 0;
            
            // Get blocks from contraption
            Object blocksData = getBlocksFromContraption(contraption);
            if (blocksData == null) return 0;
            
            // Count LittleTiles blocks
            int count = 0;
            
            if (blocksData instanceof java.util.Map<?, ?> blocksMap) {
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
    
    /**
     * Test Create rendering access.
     */
    private static boolean testCreateRenderingAccess() {
        String[] rendererClasses = {
            "com.simibubi.create.content.contraptions.render.ContraptionRenderDispatcher",
            "com.simibubi.create.content.contraptions.ContraptionRenderer"
        };
        
        for (String className : rendererClasses) {
            try {
                Class.forName(className);
                return true;
            } catch (ClassNotFoundException ignored) {}
        }
        
        return false;
    }
    
    /**
     * Test LittleTiles rendering access.
     */
    private static boolean testLittleTilesRenderingAccess() {
        String[] rendererClasses = {
            "team.creative.littletiles.client.render.tile.LittleRenderBox",
            "team.creative.littletiles.client.render.tile.LittleRenderBox"
        };
        
        for (String className : rendererClasses) {
            try {
                Class.forName(className);
                return true;
            } catch (ClassNotFoundException ignored) {}
        }
        
        return false;
    }
    
    /**
     * Check if integration is active.
     */
    private static boolean isIntegrationActive() {
        try {
            Class<?> integrationClass = Class.forName("com.createlittlecontraptions.compat.create.CreateRuntimeIntegration");
            Method isActiveMethod = integrationClass.getMethod("isIntegrationActive");
            return (Boolean) isActiveMethod.invoke(null);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Apply rendering fix to a specific contraption.
     */
    private static boolean applyRenderingFixToContraption(Entity contraptionEntity) {
        try {
            LOGGER.info("🔧 Applying rendering fix to contraption: " + contraptionEntity.getClass().getSimpleName());
            
            // Force the contraption to refresh its rendering data
            Object contraption = getContraptionFromEntity(contraptionEntity);
            if (contraption != null) {
                // Try to invalidate rendering cache
                try {
                    Method invalidateMethod = contraption.getClass().getMethod("invalidateRenderingCache");
                    invalidateMethod.invoke(contraption);
                    return true;
                } catch (Exception e) {
                    // Try alternative methods
                    try {
                        Method refreshMethod = contraption.getClass().getMethod("refreshRendering");
                        refreshMethod.invoke(contraption);
                        return true;
                    } catch (Exception e2) {
                        LOGGER.debug("Could not find refresh methods for contraption");
                    }
                }
            }
            
            return false;
            
        } catch (Exception e) {
            LOGGER.debug("Error applying rendering fix: " + e.getMessage());
            return false;
        }
    }
    
    // Helper methods (simplified versions of the integration methods)
    private static Object getContraptionFromEntity(Entity contraptionEntity) {
        try {
            Class<?> entityClass = contraptionEntity.getClass();
            try {
                Method getContraptionMethod = entityClass.getMethod("getContraption");
                return getContraptionMethod.invoke(contraptionEntity);
            } catch (Exception e) {
                try {
                    var field = entityClass.getField("contraption");
                    return field.get(contraptionEntity);
                } catch (Exception e2) {
                    return null;
                }
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    private static Object getBlocksFromContraption(Object contraption) {
        try {
            Class<?> contraptionClass = contraption.getClass();
            try {
                Method getBlocksMethod = contraptionClass.getMethod("getBlocks");
                return getBlocksMethod.invoke(contraption);
            } catch (Exception e) {
                try {
                    var field = contraptionClass.getField("blocks");
                    return field.get(contraption);
                } catch (Exception e2) {
                    return null;
                }
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    private static boolean isLittleTilesBlock(Object blockData) {
        if (blockData == null) return false;
            
            try {
                // Verificar pelo nome da classe
                String className = blockData.getClass().getName().toLowerCase();
                if (className.contains("littletiles") || className.contains("little")) {
                    return true;
                }
                
                // Verificar se é um BlockState e obter o bloco
                if (blockData instanceof BlockState) {
                    BlockState state = (BlockState) blockData;
                    Block block = state.getBlock();
                    
                    // Verificar pelo nome do registro do bloco
                    ResourceLocation registryName = BuiltInRegistries.BLOCK.getKey(block);
                    if (registryName != null && 
                        (registryName.toString().contains("littletiles") || registryName.toString().contains("little"))) {
                        return true;
                    }
                    
                    // Verificar se o bloco tem uma BlockEntity específica do LittleTiles
                    if (block instanceof EntityBlock) {
                        String entityClassName = block.getClass().getName().toLowerCase();
                        return entityClassName.contains("littletiles") || entityClassName.contains("little");
                    }
                }
                
                // Verificar pelo toString() que pode conter informações do registro
                String stringRepresentation = blockData.toString().toLowerCase();
                return stringRepresentation.contains("littletiles") || stringRepresentation.contains("little");
                
            } catch (Exception e) {
                // Em caso de erro, usar a verificação mais básica
                String blockName = blockData.getClass().getName().toLowerCase();
                return blockName.contains("littletiles") || blockName.contains("little");
            }
    }
}
