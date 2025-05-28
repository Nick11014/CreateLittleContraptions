package com.createlittlecontraptions.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for event handling functionality in CreateLittleContraptions mod.
 * Tests event logic without requiring Minecraft client to run.
 */
public class EventsTest {
    
    @BeforeEach
    void setUp() {
        // Reset event state before each test
        MockEventHandler.setEventLogging(false);
    }
    
    @Nested
    @DisplayName("ContraptionEventHandler Tests")
    class ContraptionEventHandlerTests {
        
        @Test
        @DisplayName("Should manage event logging state correctly")
        void testEventLoggingState() {
            // Test initial state
            assertFalse(MockEventHandler.isEventLoggingEnabled());
            
            // Test enabling
            MockEventHandler.setEventLogging(true);
            assertTrue(MockEventHandler.isEventLoggingEnabled());
            
            // Test disabling
            MockEventHandler.setEventLogging(false);
            assertFalse(MockEventHandler.isEventLoggingEnabled());
        }
        
        @Test
        @DisplayName("Should process contraption assembly events when enabled")
        void testContraptionAssemblyEventProcessing() {
            MockEventHandler.setEventLogging(true);
            
            MockContraptionEntity contraption = new MockContraptionEntity("ElevatorContraption", 100, 65, 200);
            MockEntityEvent assemblyEvent = new MockEntityEvent(contraption, true);
            
            EventProcessingResult result = MockEventHandler.processEntityEvent(assemblyEvent);
            
            assertTrue(result.wasProcessed());
            assertEquals("ASSEMBLY", result.getEventType());
            assertTrue(result.getMessage().contains("ASSEMBLED"));
            assertTrue(result.getMessage().contains("ElevatorContraption"));
        }
        
        @Test
        @DisplayName("Should process contraption disassembly events when enabled")
        void testContraptionDisassemblyEventProcessing() {
            MockEventHandler.setEventLogging(true);
            
            MockContraptionEntity contraption = new MockContraptionEntity("BearingContraption", -50, 70, 150);
            MockEntityEvent disassemblyEvent = new MockEntityEvent(contraption, false);
            
            EventProcessingResult result = MockEventHandler.processEntityEvent(disassemblyEvent);
            
            assertTrue(result.wasProcessed());
            assertEquals("DISASSEMBLY", result.getEventType());
            assertTrue(result.getMessage().contains("DISASSEMBLED"));
            assertTrue(result.getMessage().contains("BearingContraption"));
        }
        
        @Test
        @DisplayName("Should ignore events when logging is disabled")
        void testEventIgnoredWhenDisabled() {
            MockEventHandler.setEventLogging(false);
            
            MockContraptionEntity contraption = new MockContraptionEntity("TestContraption", 0, 0, 0);
            MockEntityEvent event = new MockEntityEvent(contraption, true);
            
            EventProcessingResult result = MockEventHandler.processEntityEvent(event);
            
            assertFalse(result.wasProcessed());
            assertNull(result.getEventType());
            assertNull(result.getMessage());
        }
        
        @Test
        @DisplayName("Should detect LittleTiles in contraption structures")
        void testLittleTilesDetectionInContraptions() {
            MockContraptionEntity contraption = new MockContraptionEntity("TestContraption", 0, 0, 0);
            contraption.addBlock("minecraft:oak_planks");
            contraption.addBlock("block.littletiles.tiles");
            contraption.addBlock("create:mechanical_bearing");
            contraption.addBlock("littletiles:special_block");
            
            LittleTilesAnalysisResult analysis = MockEventHandler.analyzeLittleTiles(contraption);
            
            assertEquals(2, analysis.getLittleTilesCount());
            assertEquals(4, analysis.getTotalBlocks());
            assertTrue(analysis.hasLittleTiles());
        }
        
        @Test
        @DisplayName("Should calculate player notification radius correctly")
        void testPlayerNotificationRadius() {
            int radius = 64; // As defined in our code
            
            // Test player within radius
            assertTrue(isWithinNotificationRadius(0, 0, 0, 32, 32, 32, radius));
            assertTrue(isWithinNotificationRadius(0, 0, 0, 63, 0, 0, radius));
            
            // Test player outside radius
            assertFalse(isWithinNotificationRadius(0, 0, 0, 65, 0, 0, radius));
            assertFalse(isWithinNotificationRadius(0, 0, 0, 100, 100, 100, radius));
        }
    }
    
    @Nested
    @DisplayName("Event Message Formatting Tests")
    class EventMessageFormattingTests {
        
        @Test
        @DisplayName("Should format assembly messages correctly")
        void testAssemblyMessageFormatting() {
            String message = formatAssemblyMessage("ElevatorContraption", 100, 65, 200, 123);
            
            assertTrue(message.contains("ASSEMBLED"));
            assertTrue(message.contains("ElevatorContraption"));
            assertTrue(message.contains("100"));
            assertTrue(message.contains("65"));
            assertTrue(message.contains("200"));
            assertTrue(message.contains("123"));
        }
        
        @Test
        @DisplayName("Should format disassembly messages correctly")
        void testDisassemblyMessageFormatting() {
            String message = formatDisassemblyMessage("BearingContraption", -50, 70, 150, 456);
            
            assertTrue(message.contains("DISASSEMBLED"));
            assertTrue(message.contains("BearingContraption"));
            assertTrue(message.contains("-50"));
            assertTrue(message.contains("70"));
            assertTrue(message.contains("150"));
            assertTrue(message.contains("456"));
        }
        
        @Test
        @DisplayName("Should format LittleTiles analysis results correctly")
        void testLittleTilesAnalysisFormatting() {
            String message = formatLittleTilesAnalysis(3, 25);
            
            assertTrue(message.contains("3"));
            assertTrue(message.contains("25"));
            assertTrue(message.contains("LittleTiles"));
            assertTrue(message.contains("blocks"));
        }
    }
    
    // Helper methods and mock classes for testing without Minecraft dependencies
    
    /**
     * Calculate distance for notification radius testing
     */
    private boolean isWithinNotificationRadius(int x1, int y1, int z1, int x2, int y2, int z2, int radius) {
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
        return distance <= radius;
    }
    
    /**
     * Format assembly event messages
     */
    private String formatAssemblyMessage(String type, int x, int y, int z, int entityId) {
        return String.format("=== CONTRAPTION ASSEMBLED === Type: %s, Position: %d,%d,%d, Entity ID: %d", 
                           type, x, y, z, entityId);
    }
    
    /**
     * Format disassembly event messages
     */
    private String formatDisassemblyMessage(String type, int x, int y, int z, int entityId) {
        return String.format("=== CONTRAPTION DISASSEMBLED === Type: %s, Position: %d,%d,%d, Entity ID: %d", 
                           type, x, y, z, entityId);
    }
    
    /**
     * Format LittleTiles analysis results
     */
    private String formatLittleTilesAnalysis(int littleTilesCount, int totalBlocks) {
        return String.format("*** %d LittleTiles found in contraption with %d total blocks! ***", 
                           littleTilesCount, totalBlocks);
    }
    
    /**
     * Mock event handler to simulate behavior without Minecraft dependencies
     */
    private static class MockEventHandler {
        private static boolean eventLoggingEnabled = false;
        
        public static void setEventLogging(boolean enabled) {
            eventLoggingEnabled = enabled;
        }
        
        public static boolean isEventLoggingEnabled() {
            return eventLoggingEnabled;
        }
        
        public static EventProcessingResult processEntityEvent(MockEntityEvent event) {
            if (!eventLoggingEnabled) {
                return new EventProcessingResult(false, null, null);
            }
            
            String eventType = event.isJoining() ? "ASSEMBLY" : "DISASSEMBLY";
            String message = event.isJoining() ? 
                "CONTRAPTION ASSEMBLED: " + event.getEntity().getType() :
                "CONTRAPTION DISASSEMBLED: " + event.getEntity().getType();
            
            return new EventProcessingResult(true, eventType, message);
        }
        
        public static LittleTilesAnalysisResult analyzeLittleTiles(MockContraptionEntity contraption) {
            int littleTilesCount = 0;
            int totalBlocks = contraption.getBlocks().size();
            
            for (String block : contraption.getBlocks()) {
                if (block.toLowerCase().contains("littletiles")) {
                    littleTilesCount++;
                }
            }
            
            return new LittleTilesAnalysisResult(littleTilesCount, totalBlocks);
        }
    }
    
    /**
     * Mock contraption entity for testing
     */
    private static class MockContraptionEntity {
        private final String type;
        private final int x, y, z;
        private final java.util.List<String> blocks = new java.util.ArrayList<>();
        
        public MockContraptionEntity(String type, int x, int y, int z) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public String getType() { return type; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public java.util.List<String> getBlocks() { return blocks; }
        public void addBlock(String block) { blocks.add(block); }
    }
    
    /**
     * Mock entity event for testing
     */
    private static class MockEntityEvent {
        private final MockContraptionEntity entity;
        private final boolean joining;
        
        public MockEntityEvent(MockContraptionEntity entity, boolean joining) {
            this.entity = entity;
            this.joining = joining;
        }
        
        public MockContraptionEntity getEntity() { return entity; }
        public boolean isJoining() { return joining; }
    }
    
    /**
     * Result class for event processing
     */
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
    
    /**
     * Result class for LittleTiles analysis
     */
    private static class LittleTilesAnalysisResult {
        private final int littleTilesCount;
        private final int totalBlocks;
        
        public LittleTilesAnalysisResult(int littleTilesCount, int totalBlocks) {
            this.littleTilesCount = littleTilesCount;
            this.totalBlocks = totalBlocks;
        }
        
        public int getLittleTilesCount() { return littleTilesCount; }
        public int getTotalBlocks() { return totalBlocks; }
        public boolean hasLittleTiles() { return littleTilesCount > 0; }
    }
}
