package com.createlittlecontraptions.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("createlittlecontraptions")
public class CreateLittleContraptionsGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "elevator_unassembled")
    public static void testElevatorStructure(GameTestHelper helper) {
        // Teste básico - verifica se a estrutura carregou
        helper.succeed();
    }
}
