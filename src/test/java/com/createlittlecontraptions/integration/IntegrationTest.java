package com.createlittlecontraptions.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CreateLittleContraptions mod.
 * Tests interaction between different components without requiring full Minecraft environment.
 */
public class IntegrationTest {
    
    @BeforeEach
    void setUp() {
        // Reset all states before each test
        MockSystemState.reset();
    }
    
    @Nested
    @DisplayName("Command and Event System Integration Tests")
    class CommandEventIntegrationTests {
        
        @Test
        @DisplayName("Should integrate debug command with event detection")
        void testDebugCommandWithEventDetection() {
            // Simulate a contraption being created
            MockContraption contraption = new MockContraption("ElevatorContraption");
            contraption.addBlock("minecraft:oak_planks");
            contraption.addBlock("block.littletiles.tiles");
            contraption.addBlock("create:mechanical_bearing");
            contraption.addBlock("littletiles:special_block");
            
            // Register the contraption in the system
            MockSystemState.addContraption(contraption);
            
            // Execute debug command
            DebugCommandResult debugResult = MockDebugCommand.execute();
            
            assertTrue(debugResult.isSuccess());
            assertEquals(1, debugResult.getContraptionsFound());
            assertEquals(2, debugResult.getLittleTilesFound());
            assertEquals(4, debugResult.getTotalBlocks());
        }
        
        @Test
        @DisplayName("Should integrate event logging with command toggles")
        void testEventCommandIntegration() {
            // Start with events disabled
            assertFalse(MockSystemState.isEventLoggingEnabled());
            
            // Enable events via command
            EventCommandResult enableResult = MockEventsCommand.execute("enable");
            assertTrue(enableResult.isSuccess());
            assertTrue(MockSystemState.isEventLoggingEnabled());
            assertTrue(enableResult.getMessage().contains("ENABLED"));
            
            // Simulate contraption assembly event
            MockContraption contraption = new MockContraption("TestContraption");
            contraption.addBlock("block.littletiles.tiles");
            
            EventProcessingResult eventResult = MockEventSystem.processAssembly(contraption);
            assertTrue(eventResult.wasProcessed());
            assertEquals("ASSEMBLY", eventResult.getEventType());
            
            // Disable events via command
            EventCommandResult disableResult = MockEventsCommand.execute("disable");
            assertTrue(disableResult.isSuccess());
            assertFalse(MockSystemState.isEventLoggingEnabled());
            
            // Verify events are no longer processed
            EventProcessingResult ignoredEvent = MockEventSystem.processAssembly(contraption);
            assertFalse(ignoredEvent.wasProcessed());
        }
    }
    
    @Nested
    @DisplayName("Full Workflow Integration Tests")
    class FullWorkflowIntegrationTests {
        
        @Test
        @DisplayName("Should handle complete contraption lifecycle")
        void testCompleteContraptionLifecycle() {
            // Step 1: Enable event logging
            MockEventsCommand.execute("enable");
            assertTrue(MockSystemState.isEventLoggingEnabled());
            
            // Step 2: Create contraption with LittleTiles
            MockContraption contraption = new MockContraption("ElevatorContraption");
            contraption.addBlock("minecraft:oak_planks");
            contraption.addBlock("block.littletiles.tiles");
            contraption.addBlock("create:mechanical_bearing");
            contraption.addBlock("block.littletiles.tiles"); // Another LittleTiles block
            contraption.addBlock("minecraft:stone");
            
            // Step 3: Process assembly event
            EventProcessingResult assemblyResult = MockEventSystem.processAssembly(contraption);
            assertTrue(assemblyResult.wasProcessed());
            assertEquals("ASSEMBLY", assemblyResult.getEventType());
            
            // Step 4: Register contraption in system
            MockSystemState.addContraption(contraption);
            
            // Step 5: Debug command should detect the contraption
            DebugCommandResult debugResult = MockDebugCommand.execute();
            assertTrue(debugResult.isSuccess());
            assertEquals(1, debugResult.getContraptionsFound());
            assertEquals(2, debugResult.getLittleTilesFound()); // 2 LittleTiles blocks
            assertEquals(5, debugResult.getTotalBlocks());
            
            // Step 6: Process disassembly event
            EventProcessingResult disassemblyResult = MockEventSystem.processDisassembly(contraption);
            assertTrue(disassemblyResult.wasProcessed());
            assertEquals("DISASSEMBLY", disassemblyResult.getEventType());
            
            // Step 7: Remove contraption from system
            MockSystemState.removeContraption(contraption);
            
            // Step 8: Debug command should find no contraptions
            DebugCommandResult finalDebugResult = MockDebugCommand.execute();
            assertTrue(finalDebugResult.isSuccess());
            assertEquals(0, finalDebugResult.getContraptionsFound());
            assertEquals(0, finalDebugResult.getLittleTilesFound());
        }
        
        @Test
        @DisplayName("Should handle multiple contraptions with varying LittleTiles content")
        void testMultipleContraptions() {
            // Create multiple contraptions
            MockContraption elevator = new MockContraption("ElevatorContraption");
            elevator.addBlock("block.littletiles.tiles");
            elevator.addBlock("minecraft:oak_planks");
            
            MockContraption bearing = new MockContraption("BearingContraption");
            bearing.addBlock("create:mechanical_bearing");
            bearing.addBlock("minecraft:stone");
            
            MockContraption complex = new MockContraption("ComplexContraption");
            complex.addBlock("block.littletiles.tiles");
            complex.addBlock("littletiles:special_block");
            complex.addBlock("littletiles:another_block");
            complex.addBlock("minecraft:iron_block");
            
            // Register all contraptions
            MockSystemState.addContraption(elevator);
            MockSystemState.addContraption(bearing);
            MockSystemState.addContraption(complex);
            
            // Execute debug command
            DebugCommandResult result = MockDebugCommand.execute();
            
            assertTrue(result.isSuccess());
            assertEquals(3, result.getContraptionsFound());
            assertEquals(4, result.getLittleTilesFound()); // 1 + 0 + 3 = 4
            assertEquals(8, result.getTotalBlocks()); // 2 + 2 + 4 = 8
            
            // Verify specific contraption analysis
            assertTrue(result.getAnalysisDetails().contains("ElevatorContraption: 1 LittleTiles"));
            assertTrue(result.getAnalysisDetails().contains("BearingContraption: 0 LittleTiles"));
            assertTrue(result.getAnalysisDetails().contains("ComplexContraption: 3 LittleTiles"));
        }
    }
    
    @Nested
    @DisplayName("Error Handling Integration Tests")
    class ErrorHandlingIntegrationTests {
        
        @Test
        @DisplayName("Should handle empty contraptions gracefully")
        void testEmptyContraptions() {
            MockContraption emptyContraption = new MockContraption("EmptyContraption");
            MockSystemState.addContraption(emptyContraption);
            
            DebugCommandResult result = MockDebugCommand.execute();
            
            assertTrue(result.isSuccess());
            assertEquals(1, result.getContraptionsFound());
            assertEquals(0, result.getLittleTilesFound());
            assertEquals(0, result.getTotalBlocks());
        }
        
        @Test
        @DisplayName("Should handle systems with no contraptions")
        void testNoContraptions() {
            // Ensure system is empty
            MockSystemState.reset();
            
            DebugCommandResult result = MockDebugCommand.execute();
            
            assertTrue(result.isSuccess());
            assertEquals(0, result.getContraptionsFound());
            assertEquals(0, result.getLittleTilesFound());
            assertEquals(0, result.getTotalBlocks());
            assertTrue(result.getMessage().contains("No contraptions found"));
        }
        
        @Test
        @DisplayName("Should handle invalid command parameters")
        void testInvalidCommandParameters() {
            EventCommandResult invalidResult = MockEventsCommand.execute("invalid");
            assertFalse(invalidResult.isSuccess());
            assertTrue(invalidResult.getMessage().contains("Invalid"));
        }
    }
    
    // Mock classes and helper methods for integration testing
    
    /**
     * Mock system state to simulate mod state without Minecraft
     */
    private static class MockSystemState {
        private static boolean eventLoggingEnabled = false;
        private static final java.util.List<MockContraption> contraptions = new java.util.ArrayList<>();
        
        public static void reset() {
            eventLoggingEnabled = false;
            contraptions.clear();
        }
        
        public static boolean isEventLoggingEnabled() { return eventLoggingEnabled; }
        public static void setEventLogging(boolean enabled) { eventLoggingEnabled = enabled; }
        
        public static void addContraption(MockContraption contraption) { contraptions.add(contraption); }
        public static void removeContraption(MockContraption contraption) { contraptions.remove(contraption); }
        public static java.util.List<MockContraption> getContraptions() { return new java.util.ArrayList<>(contraptions); }
    }
    
    /**
     * Mock contraption for testing
     */
    private static class MockContraption {
        private final String type;
        private final java.util.List<String> blocks = new java.util.ArrayList<>();
        
        public MockContraption(String type) { this.type = type; }
        
        public String getType() { return type; }
        public java.util.List<String> getBlocks() { return blocks; }
        public void addBlock(String block) { blocks.add(block); }
        
        public int getLittleTilesCount() {
            return (int) blocks.stream()
                .filter(block -> block.toLowerCase().contains("littletiles"))
                .count();
        }
    }
    
    /**
     * Mock debug command
     */
    private static class MockDebugCommand {
        public static DebugCommandResult execute() {
            java.util.List<MockContraption> contraptions = MockSystemState.getContraptions();
            
            if (contraptions.isEmpty()) {
                return new DebugCommandResult(true, 0, 0, 0, "No contraptions found in the world.", "");
            }
            
            int totalLittleTiles = 0;
            int totalBlocks = 0;
            StringBuilder analysisDetails = new StringBuilder();
            
            for (MockContraption contraption : contraptions) {
                int littleTilesCount = contraption.getLittleTilesCount();
                int blockCount = contraption.getBlocks().size();
                
                totalLittleTiles += littleTilesCount;
                totalBlocks += blockCount;
                
                analysisDetails.append(String.format("%s: %d LittleTiles out of %d blocks; ", 
                    contraption.getType(), littleTilesCount, blockCount));
            }
            
            String message = String.format("Found %d contraptions with %d LittleTiles total (%d blocks total)", 
                contraptions.size(), totalLittleTiles, totalBlocks);
            
            return new DebugCommandResult(true, contraptions.size(), totalLittleTiles, totalBlocks, message, analysisDetails.toString());
        }
    }
    
    /**
     * Mock events command
     */
    private static class MockEventsCommand {
        public static EventCommandResult execute(String action) {
            switch (action.toLowerCase()) {
                case "enable":
                    MockSystemState.setEventLogging(true);
                    return new EventCommandResult(true, "Contraption event logging ENABLED");
                case "disable":
                    MockSystemState.setEventLogging(false);
                    return new EventCommandResult(true, "Contraption event logging DISABLED");
                default:
                    return new EventCommandResult(false, "Invalid action. Use 'enable' or 'disable'");
            }
        }
    }
    
    /**
     * Mock event system
     */
    private static class MockEventSystem {
        public static EventProcessingResult processAssembly(MockContraption contraption) {
            if (!MockSystemState.isEventLoggingEnabled()) {
                return new EventProcessingResult(false, null, null);
            }
            
            String message = String.format("CONTRAPTION ASSEMBLED: %s with %d blocks (%d LittleTiles)", 
                contraption.getType(), contraption.getBlocks().size(), contraption.getLittleTilesCount());
            
            return new EventProcessingResult(true, "ASSEMBLY", message);
        }
        
        public static EventProcessingResult processDisassembly(MockContraption contraption) {
            if (!MockSystemState.isEventLoggingEnabled()) {
                return new EventProcessingResult(false, null, null);
            }
            
            String message = String.format("CONTRAPTION DISASSEMBLED: %s with %d blocks (%d LittleTiles)", 
                contraption.getType(), contraption.getBlocks().size(), contraption.getLittleTilesCount());
            
            return new EventProcessingResult(true, "DISASSEMBLY", message);
        }
    }
    
    // Result classes
    private static class DebugCommandResult {
        private final boolean success;
        private final int contraptionsFound;
        private final int littleTilesFound;
        private final int totalBlocks;
        private final String message;
        private final String analysisDetails;
        
        public DebugCommandResult(boolean success, int contraptionsFound, int littleTilesFound, int totalBlocks, String message, String analysisDetails) {
            this.success = success;
            this.contraptionsFound = contraptionsFound;
            this.littleTilesFound = littleTilesFound;
            this.totalBlocks = totalBlocks;
            this.message = message;
            this.analysisDetails = analysisDetails;
        }
        
        public boolean isSuccess() { return success; }
        public int getContraptionsFound() { return contraptionsFound; }
        public int getLittleTilesFound() { return littleTilesFound; }
        public int getTotalBlocks() { return totalBlocks; }
        public String getMessage() { return message; }
        public String getAnalysisDetails() { return analysisDetails; }
    }
    
    private static class EventCommandResult {
        private final boolean success;
        private final String message;
        
        public EventCommandResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
    
    private static class EventProcessingResult {
        private final boolean wasProcessed;
        private final String eventType;
        private final String message;
        
        public EventProcessingResult(boolean wasProcessed, String eventType, String message) {
            this.wasProcessed = wasProcessed;
            this.eventType = eventType;
            this.message = message;
        }
        
        public boolean wasProcessed() { return wasProcessed; }
        public String getEventType() { return eventType; }
        public String getMessage() { return message; }
    }
}
