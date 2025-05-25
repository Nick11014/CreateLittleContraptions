package com.createlittlecontraptions.events;

import com.createlittlecontraptions.compat.create.ContraptionRenderingFix;
import com.createlittlecontraptions.compat.create.CreateRuntimeIntegration;
import com.createlittlecontraptions.compat.create.LittleTilesContraptionFix;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.api.distmarker.Dist;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Client-side command handler for debugging the mod functionality.
 * Provides in-game commands to check mod status and trigger debug actions.
 */
@EventBusSubscriber(modid = "createlittlecontraptions", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class DebugCommandHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
      @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        String message = event.getMessage();
        var player = Minecraft.getInstance().player;
        
        if (player == null) return;
        
        // Handle debug commands - use a different prefix that works
        if (message.startsWith("!clc")) {
            event.setCanceled(true); // Prevent the message from being sent to chat
            
            String[] parts = message.split(" ");
            if (parts.length == 1) {
                // Show help
                showDebugHelp(player);
            } else {
                String subCommand = parts[1].toLowerCase();
                switch (subCommand) {
                    case "status":
                        showStatus(player);
                        break;
                    case "full":
                        showFullDebugInfo(player);
                        break;
                    case "scan":
                        triggerScan(player);
                        break;                    case "test":
                        runCompatibilityTest(player);
                        break;
                    case "littletiles":
                    case "lt":
                        testLittleTilesDetection(player);
                        break;
                    default:
                        showDebugHelp(player);
                        break;
                }
            }
        }
    }
      private static void showDebugHelp(net.minecraft.world.entity.player.Player player) {
        player.sendSystemMessage(Component.literal("§6=== CreateLittleContraptions Debug Commands ==="));
        player.sendSystemMessage(Component.literal("§e/clc-debug status §7- Show basic status"));
        player.sendSystemMessage(Component.literal("§e/clc-debug full §7- Show detailed debug info"));
        player.sendSystemMessage(Component.literal("§e/clc-debug scan §7- Trigger area scan"));
        player.sendSystemMessage(Component.literal("§e/clc-debug test §7- Run compatibility test"));
        player.sendSystemMessage(Component.literal("§e/clc-debug littletiles §7- Test LittleTiles detection"));
        player.sendSystemMessage(Component.literal("§6========================================="));
    }
    
    private static void showStatus(net.minecraft.world.entity.player.Player player) {
        player.sendSystemMessage(Component.literal("§6=== CreateLittleContraptions Status ==="));
        
        // Check ContraptionRenderingFix
        if (ContraptionRenderingFix.isActive()) {
            player.sendSystemMessage(Component.literal("§a✓ ContraptionRenderingFix: ACTIVE"));
            player.sendSystemMessage(Component.literal("§7  " + ContraptionRenderingFix.getStatus()));
        } else {
            player.sendSystemMessage(Component.literal("§c✗ ContraptionRenderingFix: INACTIVE"));
        }
        
        // Check CreateRuntimeIntegration
        if (CreateRuntimeIntegration.isIntegrationActive()) {
            player.sendSystemMessage(Component.literal("§a✓ RuntimeIntegration: ACTIVE"));
            player.sendSystemMessage(Component.literal("§7  " + CreateRuntimeIntegration.getIntegrationStatus()));
        } else {
            player.sendSystemMessage(Component.literal("§c✗ RuntimeIntegration: INACTIVE"));
        }
        
        // Check LittleTilesContraptionFix
        if (LittleTilesContraptionFix.isFixActive()) {
            player.sendSystemMessage(Component.literal("§a✓ LittleTilesFix: ACTIVE"));
            player.sendSystemMessage(Component.literal("§7  " + LittleTilesContraptionFix.getFixStatus()));
        } else {
            player.sendSystemMessage(Component.literal("§c✗ LittleTilesFix: INACTIVE"));
        }
        
        player.sendSystemMessage(Component.literal("§6====================================="));
    }
    
    private static void showFullDebugInfo(net.minecraft.world.entity.player.Player player) {
        player.sendSystemMessage(Component.literal("§6=== Full Debug Information ==="));
        
        // Log to console and show in chat
        ContraptionRenderingFix.logDebugInfo();
        LOGGER.info("Runtime Integration Status: {}", CreateRuntimeIntegration.getIntegrationStatus());
        LOGGER.info("LittleTiles Fix Status: {}", LittleTilesContraptionFix.getFixStatus());
        
        // Show key info in chat
        player.sendSystemMessage(Component.literal("§eDetailed debug info logged to console"));
        player.sendSystemMessage(Component.literal("§eCheck latest.log for full details"));
        
        // Show immediate status
        showStatus(player);
    }
      private static void triggerScan(net.minecraft.world.entity.player.Player player) {
        player.sendSystemMessage(Component.literal("§6Triggering LittleTiles block detection scan..."));
        
        try {
            // Get player position
            var playerPos = player.blockPosition();
            var level = player.level();
            
            player.sendSystemMessage(Component.literal("§eScan starting at position: " + playerPos));
            
            // Manual scan for LittleTiles blocks in a smaller area around player
            int scanRadius = 16;
            int blocksFound = 0;
            int totalBlocks = 0;
            
            for (int x = -scanRadius; x <= scanRadius; x += 4) {
                for (int y = -scanRadius; y <= scanRadius; y += 4) {
                    for (int z = -scanRadius; z <= scanRadius; z += 4) {
                        var pos = playerPos.offset(x, y, z);
                        var state = level.getBlockState(pos);
                        totalBlocks++;
                        
                        if (!state.isAir()) {
                            String blockName = state.getBlock().getClass().getName();
                            String registryName = "";
                            
                            try {
                                var registryId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
                                if (registryId != null) {
                                    registryName = registryId.toString();
                                }
                            } catch (Exception ignored) {}
                            
                            // Check for LittleTiles blocks manually
                            if (blockName.contains("littletiles") || 
                                blockName.contains("LittleTile") ||
                                blockName.contains("Little") ||
                                blockName.contains("team.creative") ||
                                registryName.startsWith("littletiles:")) {
                                
                                blocksFound++;
                                player.sendSystemMessage(Component.literal(
                                    "§a✓ Found LittleTiles block at " + pos + ": " + registryName));
                                LOGGER.info("Manual scan found LittleTiles block: {} ({}) at {}", 
                                    blockName, registryName, pos);
                            }
                        }
                    }
                }
            }
            
            player.sendSystemMessage(Component.literal(
                "§6Scan complete: Found " + blocksFound + " LittleTiles blocks out of " + totalBlocks + " positions"));
            
            // Also trigger the existing debug
            ContraptionRenderingFix.logDebugInfo();
            
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("§cScan failed: " + e.getMessage()));
            LOGGER.error("Scan failed", e);
        }
    }
    
    private static void runCompatibilityTest(net.minecraft.world.entity.player.Player player) {
        player.sendSystemMessage(Component.literal("§6Running compatibility test..."));
        
        try {
            // Test Create mod detection
            try {
                Class.forName("com.simibubi.create.Create");
                player.sendSystemMessage(Component.literal("§a✓ Create mod: FOUND"));
            } catch (ClassNotFoundException e) {
                player.sendSystemMessage(Component.literal("§c✗ Create mod: NOT FOUND"));
            }
            
            // Test LittleTiles detection
            try {
                Class.forName("team.creative.littletiles.LittleTiles");
                player.sendSystemMessage(Component.literal("§a✓ LittleTiles mod: FOUND"));
            } catch (ClassNotFoundException e) {
                player.sendSystemMessage(Component.literal("§c✗ LittleTiles mod: NOT FOUND"));
            }
              // Test CreativeCore detection
            try {
                Class.forName("team.creative.creativecore.CreativeCore");
                player.sendSystemMessage(Component.literal("§a✓ CreativeCore: FOUND"));
            } catch (ClassNotFoundException e) {
                player.sendSystemMessage(Component.literal("§c✗ CreativeCore: NOT FOUND"));
            }
            
            // Test specific classes
            try {
                Class.forName("com.simibubi.create.content.contraptions.Contraption");
                player.sendSystemMessage(Component.literal("§a✓ Create Contraption class: FOUND"));
            } catch (ClassNotFoundException e) {
                player.sendSystemMessage(Component.literal("§c✗ Create Contraption class: NOT FOUND"));
            }
            
            // Test LittleTiles block classes
            String[] littleTilesClasses = {                "team.creative.littletiles.common.block.little.tile.LittleTileBlock",
                "team.creative.littletiles.common.block.LittleTilesBlock",
                "team.creative.littletiles.LittleTileBlock"
            };
            
            boolean foundLittleTilesBlock = false;
            for (String className : littleTilesClasses) {
                try {
                    Class.forName(className);
                    player.sendSystemMessage(Component.literal("§a✓ LittleTiles block class: " + className));
                    foundLittleTilesBlock = true;
                    break;
                } catch (ClassNotFoundException ignored) {
                    // Continue trying
                }
            }
            
            if (!foundLittleTilesBlock) {
                player.sendSystemMessage(Component.literal("§c✗ LittleTiles block class: NOT FOUND"));
            }
            
            player.sendSystemMessage(Component.literal("§6Compatibility test completed"));
            
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("§cCompatibility test failed: " + e.getMessage()));
            LOGGER.error("Compatibility test failed", e);
        }
    }
    
    private static void testLittleTilesDetection(net.minecraft.world.entity.player.Player player) {
        player.sendSystemMessage(Component.literal("§6=== LittleTiles Detection Test ==="));
        
        try {
            // Test 1: Check if LittleTiles is available via ModList
            boolean modDetected = net.neoforged.fml.ModList.get().isLoaded("littletiles");
            player.sendSystemMessage(Component.literal(
                modDetected ? "§a✓ LittleTiles mod: DETECTED via ModList" : "§c✗ LittleTiles mod: NOT DETECTED"));
            
            // Test 2: Check various class names
            String[] classesToTest = {
                "team.creative.littletiles.common.block.LittleBlock",
                "team.creative.littletiles.common.block.LittleTileBlock", 
                "team.creative.littletiles.common.block.little.LittleBlock",
                "team.creative.littletiles.LittleBlock",
                "team.creative.littletiles.common.block.little.tile.LittleTileBlock"
            };
            
            player.sendSystemMessage(Component.literal("§eTesting class detection:"));
            for (String className : classesToTest) {
                try {
                    Class.forName(className);
                    player.sendSystemMessage(Component.literal("§a✓ Found: " + className));
                    LOGGER.info("Successfully found LittleTiles class: {}", className);
                } catch (ClassNotFoundException e) {
                    player.sendSystemMessage(Component.literal("§c✗ Missing: " + className));
                }
            }
            
            // Test 3: Check blocks around player
            var playerPos = player.blockPosition();
            var level = player.level();
            int radius = 8;
            int foundBlocks = 0;
            
            player.sendSystemMessage(Component.literal("§eScanning blocks in " + radius + " block radius..."));
            
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        var pos = playerPos.offset(x, y, z);
                        var state = level.getBlockState(pos);
                        
                        if (!state.isAir()) {
                            String blockName = state.getBlock().getClass().getName();
                            String registryName = "";
                            
                            try {
                                var registryId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
                                if (registryId != null) {
                                    registryName = registryId.toString();
                                }
                            } catch (Exception ignored) {}
                            
                            // Test our detection logic
                            if (registryName.startsWith("littletiles:") || 
                                blockName.toLowerCase().contains("little") ||
                                blockName.contains("team.creative")) {
                                
                                foundBlocks++;
                                player.sendSystemMessage(Component.literal(
                                    "§d➤ Block at " + pos + ": " + registryName + " (class: " + blockName + ")"));
                            }
                        }
                    }
                }
            }
            
            player.sendSystemMessage(Component.literal("§6Detection test complete: Found " + foundBlocks + " potential LittleTiles blocks"));
            
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("§cTest failed: " + e.getMessage()));
            LOGGER.error("LittleTiles detection test failed", e);
        }
    }
}
