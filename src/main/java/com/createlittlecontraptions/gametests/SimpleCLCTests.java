package com.createlittlecontraptions.gametests;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

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
}