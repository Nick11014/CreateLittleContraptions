package com.createlittlecontraptions.gametests;

import com.createlittlecontraptions.events.ContraptionEventHandler;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * GameTests for testing ContraptionEventHandler functionality automatically
 */
@PrefixGameTestTemplate(false)
public class ContraptionEventsGameTests {

    /**
     * Test that event logging can be enabled/disabled without errors
     */
    @GameTest(template = "createlittlecontraptions:elevator_unassembled")
    public static void contraptionEventLoggingToggleTest(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                // Test enabling event logging
                boolean originalState = ContraptionEventHandler.isEventLoggingEnabled();
                ContraptionEventHandler.setEventLogging(true);
                
                if (!ContraptionEventHandler.isEventLoggingEnabled()) {
                    helper.fail("Failed to enable event logging");
                }
                
                // Test disabling event logging
                ContraptionEventHandler.setEventLogging(false);
                
                if (ContraptionEventHandler.isEventLoggingEnabled()) {
                    helper.fail("Failed to disable event logging");
                }
                
                // Restore original state
                ContraptionEventHandler.setEventLogging(originalState);
                
                helper.succeed();
            });
    }

    /**
     * Test that we can access the event handler without crashes
     */
    @GameTest(template = "createlittlecontraptions:elevator_unassembled") 
    public static void contraptionEventHandlerAccessTest(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                try {
                    // Test basic access to event handler
                    boolean currentState = ContraptionEventHandler.isEventLoggingEnabled();
                    
                    // Toggle state twice to test functionality                    ContraptionEventHandler.setEventLogging(!currentState);
                    ContraptionEventHandler.setEventLogging(currentState);
                    
                    // If we get here without exception, the test passes
                    helper.succeed();
                } catch (Exception e) {
                    helper.fail("ContraptionEventHandler access failed: " + e.getMessage());
                }
            });
    }

    /**
     * Test basic structure setup without creating actual contraptions
     * This ensures our test environment is working correctly
     */
    @GameTest(template = "createlittlecontraptions:elevator_unassembled")
    public static void basicStructureTest(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> {
                // Verify test structure loaded correctly
                BlockPos center = new BlockPos(1, 1, 1);
                
                // Check that we have access to the level
                if (helper.getLevel() == null) {
                    helper.fail("Test level is null");
                }
                
                // Check we can access blocks in the structure
                var blockState = helper.getBlockState(center);
                
                // The test passes if we can access basic game functionality
                helper.succeed();
            });
    }
}
