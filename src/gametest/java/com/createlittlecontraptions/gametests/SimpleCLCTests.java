package com.createlittlecontraptions.gametests;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("createlittlecontraptions")
public class SimpleCLCTests {    // Um teste básico para Create Little Contraptions
    @GameTest
    public void basicBlockPlacementTest(GameTestHelper helper) {
        // Posição relativa dentro da área de teste
        BlockPos pos = new BlockPos(0, 1, 0);

        // Coloca um bloco de lã branca
        helper.setBlock(pos, Blocks.WHITE_WOOL);

        // Verifica se o bloco foi realmente colocado
        helper.assertBlockPresent(Blocks.WHITE_WOOL, pos);

        // Marca o teste como bem-sucedido
        helper.succeed();
    }

    // Teste para verificar integração básica com Create
    @GameTest(timeoutTicks = 100)
    public void createIntegrationTest(GameTestHelper helper) {
        // Posição relativa
        BlockPos pos = new BlockPos(0, 1, 0);

        helper.setBlock(pos, Blocks.REDSTONE_BLOCK);

        // Espera alguns ticks e então executa verificação
        helper.runAfterDelay(20, () -> {
            helper.assertBlockPresent(Blocks.REDSTONE_BLOCK, pos);
            helper.succeed();
        });
    }}