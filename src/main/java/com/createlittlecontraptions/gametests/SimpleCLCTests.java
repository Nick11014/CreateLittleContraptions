package com.createlittlecontraptions.gametests;

import com.createlittlecontraptions.commands.ContraptionDebugCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import com.mojang.brigadier.CommandDispatcher;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

@GameTestHolder("createlittlecontraptions")
public class SimpleCLCTests {
      // Um teste básico para Create Little Contraptions usando estrutura de elevador
    @PrefixGameTestTemplate(false)
    @GameTest(template = "elevator_unassembled")
    public static void elevatorUnassembledTest(GameTestHelper helper) {
        // Posição relativa dentro da área de teste
        BlockPos pos = new BlockPos(0, 1, 0);

        // Coloca um bloco de lã branca
        helper.setBlock(pos, Blocks.WHITE_WOOL);

        // Verifica se o bloco foi realmente colocado
        helper.assertBlockPresent(Blocks.WHITE_WOOL, pos);

        // Marca o teste como bem-sucedido
        helper.succeed();
    }

    // Teste para verificar se o comando contraption-debug classes não gera exceções
    @PrefixGameTestTemplate(false)
    @GameTest(template = "elevator_unassembled")
    public static void contraptionDebugClassesRobustnessTest(GameTestHelper helper) {
        try {
            // Get the server and command dispatcher
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) {
                helper.fail("Could not get server instance");
                return;
            }
            
            CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
            
            // Register our command to ensure it's available
            ContraptionDebugCommand.register(dispatcher);
              // Create a test command source
            ServerLevel level = helper.getLevel();
            BlockPos testPos = helper.absolutePos(new BlockPos(0, 1, 0));
            CommandSourceStack testSource = server.createCommandSourceStack()
                .withLevel(level)
                .withPosition(Vec3.atCenterOf(testPos));
              // Test 1: Execute basic contraption-debug command
            try {
                dispatcher.execute("contraption-debug", testSource);
                helper.getLevel().getServer().getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.literal("Basic contraption-debug executed successfully"), 
                    false
                );
            } catch (Exception e) {
                helper.fail("Basic contraption-debug command failed: " + e.getMessage());
                return;
            }
            
            // Test 2: Execute contraption-debug classes command (reflection analysis)
            try {
                dispatcher.execute("contraption-debug classes", testSource);
                helper.getLevel().getServer().getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.literal("Class analysis contraption-debug executed successfully"), 
                    false
                );
            } catch (Exception e) {
                helper.fail("Contraption-debug classes command failed: " + e.getMessage());
                return;
            }
            
            // If we reach here, both commands executed without exceptions
            helper.succeed();
            
        } catch (Exception e) {
            helper.fail("Test setup failed: " + e.getMessage());
        }
    }
}