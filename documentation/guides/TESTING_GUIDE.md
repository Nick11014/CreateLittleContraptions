# CreateLittleContraptions - Testing Guide

## 🧪 **COMPREHENSIVE TESTING PROTOCOL**

### **PREREQUISITE VERIFICATION**

Before testing, ensure all mods are properly installed:

**Required Mods:**
- ✅ Minecraft 1.21.1
- ✅ NeoForge 21.1.172
- ✅ Create 6.0.4
- ✅ LittleTiles 1.6.0-pre163
- ✅ CreativeCore 2.13.5
- ✅ CreateLittleContraptions 1.0.0

**Installation Check:**
1. Start Minecraft and check mod list
2. Look for log message: "CreateLittleContraptions mod initializing..."
3. Verify integration status messages show all mods detected

---

## 📋 **TESTING PHASE 1: Basic Functionality**

### **Test 1.1: Mod Detection**
```
Command: /clc-debug littletiles
Expected: Message showing LittleTiles mod detected and version info
```

### **Test 1.2: Rendering System Status**
```
Command: /clc-debug rendering
Expected: Status of rendering integration (should show "Active")
```

### **Test 1.3: Log Verification**
Check latest.log for these entries:
- "✓ Create mod detected!"
- "✓ LittleTiles mod detected!" 
- "✓ CreativeCore detected!"

---

## 🔧 **TESTING PHASE 2: Basic Contraption Testing**

### **Test 2.1: Simple Contraption Setup**
1. **Create a basic contraption:**
   - Place a mechanical bearing
   - Attach a platform (3x3 blocks minimum)
   - Add power source (windmill/water wheel)

2. **Verify contraption detection:**
   ```
   Command: /clc-debug contraptions
   Expected: List showing your contraption entity
   ```

### **Test 2.2: LittleTiles Block Placement**
1. **Place LittleTiles blocks on the contraption:**
   - Use LittleTiles wrench to create custom blocks
   - Try different LittleTiles structures
   - Ensure blocks are properly attached to contraption

2. **Test without movement first:**
   - Verify LittleTiles blocks render correctly when stationary
   - Check block data is preserved

### **Test 2.3: Movement Test**
1. **Activate contraption:**
   - Start rotation/movement
   - Observe LittleTiles blocks during movement
   - **EXPECTED RESULT**: Blocks should remain visible and move with contraption

2. **If blocks become invisible:**
   ```
   Command: /clc-debug fix
   Expected: Force rendering fix on all contraptions
   ```

---

## 🚀 **TESTING PHASE 3: Advanced Scenarios**

### **Test 3.1: Complex Contraptions**
1. **Multi-axis movement:**
   - Create contraption with both rotation and translation
   - Test with multiple mechanical bearings
   - Verify LittleTiles blocks maintain visibility

2. **Large contraptions:**
   - Build contraption with 50+ blocks
   - Include multiple LittleTiles structures
   - Monitor performance during movement

### **Test 3.2: Different LittleTiles Types**
Test with various LittleTiles features:
- ✅ Simple colored blocks
- ✅ Complex structures
- ✅ Animated LittleTiles
- ✅ LittleTiles doors/mechanisms

### **Test 3.3: Edge Cases**
1. **Contraption collisions**
2. **Rapid start/stop movement**
3. **Multiple contraptions in same area**
4. **World reload while contraption is moving**

---

## 🔍 **DEBUGGING AND TROUBLESHOOTING**

### **If LittleTiles blocks are still invisible:**

1. **Check debug output:**
   ```
   /clc-debug littletiles
   /clc-debug rendering
   /clc-debug contraptions
   ```

2. **Force rendering fix:**
   ```
   /clc-debug fix
   ```

3. **Check logs for errors:**
   - Look for `ERROR` or `WARN` messages
   - Check for reflection failures
   - Verify mod compatibility

### **Common Issues and Solutions:**

**Issue**: "LittleTiles mod NOT found!"
- **Solution**: Verify LittleTiles is installed correctly
- Check for version compatibility

**Issue**: Contraptions not detected
- **Solution**: Ensure contraption is properly assembled
- Check if Create mod mechanics are working

**Issue**: Rendering fix doesn't work
- **Solution**: Restart world or relog to game
- Check for mod conflicts

---

## 📊 **EXPECTED BEHAVIOR SUMMARY**

### **✅ SUCCESS CRITERIA:**
1. **All debug commands work** without errors
2. **LittleTiles blocks remain visible** during contraption movement
3. **No performance degradation** with solution active
4. **Blocks maintain correct position/rotation** relative to contraption
5. **No crashes or errors** in logs

### **❌ FAILURE INDICATORS:**
1. LittleTiles blocks become invisible during movement
2. Contraptions not detected by debug commands
3. Error messages in logs related to our mod
4. Game crashes when using contraptions with LittleTiles

---

## 🎮 **QUICK TEST SCENARIO**

**5-Minute Verification Test:**

1. **Setup**: Create bearing + platform + power
2. **Place**: Add 2-3 LittleTiles blocks to platform
3. **Command**: `/clc-debug littletiles` (should show detection)
4. **Activate**: Start contraption rotation
5. **Observe**: LittleTiles blocks should remain visible
6. **Success**: If blocks stay visible, solution is working!

---

## 🛠️ **REPORTING ISSUES**

If you encounter problems, please provide:

1. **Complete log file** (latest.log)
2. **Mod versions** used
3. **Step-by-step reproduction** of the issue
4. **Output of all debug commands**:
   ```
   /clc-debug littletiles
   /clc-debug rendering  
   /clc-debug contraptions
   ```
5. **Screenshots** showing the problem

---

## 🎯 **SUCCESS CONFIRMATION**

**The solution is working correctly when:**
- ✅ LittleTiles blocks remain visible during contraption movement
- ✅ Blocks move smoothly with the contraption
- ✅ No visual glitches or rendering artifacts
- ✅ Performance remains stable
- ✅ Debug commands confirm integration is active

**This resolves the original issue of LittleTiles blocks becoming invisible when part of Create mod contraptions!**
