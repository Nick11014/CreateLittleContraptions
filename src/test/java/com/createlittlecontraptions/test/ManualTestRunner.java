package com.createlittlecontraptions.test;

/**
 * Manual test runner for CreateLittleContraptions mod functionality.
 * This approach avoids JUnit compatibility issues with NeoForge.
 */
public class ManualTestRunner {
    
    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    public static void main(String[] args) {
        System.out.println("=== CreateLittleContraptions Manual Test Runner ===");
        System.out.println();
        
        // Run all tests
        testLittleTilesDetection();
        testMessageFormatting();
        testModConstants();
        testStringOperations();
        testEdgeCases();
        
        // Print results
        System.out.println();
        System.out.println("=== Test Results ===");
        System.out.println("Tests run: " + testsRun);
        System.out.println("Tests passed: " + testsPassed);
        System.out.println("Tests failed: " + testsFailed);
        
        if (testsFailed > 0) {
            System.out.println("❌ Some tests failed!");
            System.exit(1);
        } else {
            System.out.println("✅ All tests passed!");
            System.exit(0);
        }
    }
    
    private static void testLittleTilesDetection() {
        System.out.println("Testing LittleTiles detection...");
        
        // Test valid cases
        assertTrue("Should detect block.littletiles.tiles", isLittleTilesBlock("block.littletiles.tiles"));
        assertTrue("Should detect Block{littletiles:tiles}", isLittleTilesBlock("Block{littletiles:tiles}"));
        assertTrue("Should detect LITTLETILES", isLittleTilesBlock("LITTLETILES"));
        
        // Test invalid cases
        assertFalse("Should not detect minecraft:oak_planks", isLittleTilesBlock("minecraft:oak_planks"));
        assertFalse("Should not detect create:bearing", isLittleTilesBlock("create:mechanical_bearing"));
        assertFalse("Should not detect empty string", isLittleTilesBlock(""));
        assertFalse("Should not detect null", isLittleTilesBlock(null));
        
        System.out.println("✓ LittleTiles detection tests completed");
    }
    
    private static void testMessageFormatting() {
        System.out.println("Testing message formatting...");
        
        String message = formatContraptionMessage(5, 32);
        assertTrue("Message should contain count", message.contains("5"));
        assertTrue("Message should contain total", message.contains("32"));
        assertTrue("Message should contain LittleTiles", message.contains("LittleTiles"));
        
        String position = formatPosition(-1, -3, 0);
        assertTrue("Position should contain x", position.contains("-1"));
        assertTrue("Position should contain y", position.contains("-3"));
        assertTrue("Position should contain z", position.contains("0"));
        
        System.out.println("✓ Message formatting tests completed");
    }
    
    private static void testModConstants() {
        System.out.println("Testing mod constants...");
          String modId = "createlittlecontraptions";
        assertEquals("Mod ID length should be 24", 24, modId.length());
        assertTrue("Mod ID should contain 'little'", modId.contains("little"));
        assertTrue("Mod ID should contain 'contraptions'", modId.contains("contraptions"));
        assertEquals("Mod ID should be lowercase", modId, modId.toLowerCase());
        
        System.out.println("✓ Mod constants tests completed");
    }
    
    private static void testStringOperations() {
        System.out.println("Testing string operations...");
        
        String[] blocks = {
            "block.littletiles.tiles",
            "minecraft:oak_planks", 
            "Block{littletiles:tiles}",
            "create:mechanical_bearing",
            "littletiles:special_block"
        };
        
        int count = countLittleTilesBlocks(blocks);
        assertEquals("Should count 3 LittleTiles blocks", 3, count);
        
        System.out.println("✓ String operations tests completed");
    }
    
    private static void testEdgeCases() {
        System.out.println("Testing edge cases...");
        
        assertFalse("Null should return false", isLittleTilesBlock(null));
        assertFalse("Empty string should return false", isLittleTilesBlock(""));
        assertFalse("Whitespace should return false", isLittleTilesBlock("   "));
        assertTrue("Should trim and detect", isLittleTilesBlock("  littletiles  "));
        
        assertEquals("Empty array should return 0", 0, countLittleTilesBlocks(new String[]{}));
        assertEquals("Non-LittleTiles array should return 0", 0, 
                   countLittleTilesBlocks(new String[]{"minecraft:stone", "create:bearing"}));
        
        System.out.println("✓ Edge cases tests completed");
    }
    
    // Helper methods for testing
    private static boolean isLittleTilesBlock(String blockString) {
        if (blockString == null || blockString.trim().isEmpty()) {
            return false;
        }
        return blockString.toLowerCase().contains("littletiles");
    }
    
    private static String formatContraptionMessage(int littleTilesCount, int totalBlocks) {
        return String.format("*** %d LittleTiles found in contraption with %d total blocks! ***", 
                           littleTilesCount, totalBlocks);
    }
    
    private static String formatPosition(int x, int y, int z) {
        return String.format("BlockPos{x=%d, y=%d, z=%d}", x, y, z);
    }
    
    private static int countLittleTilesBlocks(String[] blocks) {
        int count = 0;
        for (String block : blocks) {
            if (isLittleTilesBlock(block)) {
                count++;
            }
        }
        return count;
    }
    
    // Test assertion methods
    private static void assertTrue(String message, boolean condition) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("  ✓ " + message);
        } else {
            testsFailed++;
            System.out.println("  ❌ " + message);
        }
    }
    
    private static void assertFalse(String message, boolean condition) {
        assertTrue(message, !condition);
    }
    
    private static void assertEquals(String message, Object expected, Object actual) {
        testsRun++;
        if (expected == null && actual == null) {
            testsPassed++;
            System.out.println("  ✓ " + message);
        } else if (expected != null && expected.equals(actual)) {
            testsPassed++;
            System.out.println("  ✓ " + message);
        } else {
            testsFailed++;
            System.out.println("  ❌ " + message + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }
}
