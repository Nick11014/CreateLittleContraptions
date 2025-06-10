# Testing Guide for CreateLittleContraptions Model Baking System

## Overview

This guide explains how to test the Model Baking system that allows LittleTiles blocks to be visually represented in Create contraptions.

## Prerequisites

1. **Minecraft with NeoForge** - Version 1.21.1
2. **Create Mod** - Version 6.0.4 or compatible
3. **LittleTiles Mod** - Version 1.6.0-pre163 or compatible
4. **CreateLittleContraptions** - This mod

## System Architecture

The Model Baking system works through several components:

### 1. Model Baker (`LittleTilesModelBaker`)
- Converts LittleTiles block entities into static `BakedModel`s
- Uses reflection to safely access LittleTiles internal structures
- Falls back to placeholder models if access fails

### 2. Model Cache (`ContraptionModelCache`)
- Stores baked models by contraption UUID and block position
- Thread-safe concurrent access
- Automatic cleanup when contraptions are disassembled

### 3. Rendering Context (`ContraptionRenderingContext`)
- Tracks when we're rendering contraptions vs. regular world blocks
- Provides context for mixins to make informed decisions

### 4. Mixins
- **`ContraptionRenderInfoMixin`**: Provides access to cached models
- **`BlockRenderDispatcherMixin`**: Intercepts model requests during rendering
- **`AbstractContraptionEntityMixin`**: Sets up rendering context

## Testing Steps

### Step 1: Basic Functionality Test

1. **Start Minecraft** with all required mods installed
2. **Create a world** (Creative mode recommended)
3. **Check mod loading**: Look for "CreateLittleContraptions mod initializing..." in logs

### Step 2: Build a Test Contraption

1. **Place some LittleTiles blocks** in your world
   - Use the Little Tiles mod to create some structures
   - Make them part of a contraption-compatible build

2. **Build a Create contraption** that includes the LittleTiles blocks
   - Use Create blocks like Mechanical Bearings, Pistons, or Gantries
   - Ensure the LittleTiles blocks are part of the structure that will move

### Step 3: Assemble and Test

1. **Activate the contraption** (e.g., power a Mechanical Bearing)
2. **Monitor the logs** for model baking activity:
   ```
   [INFO] Contraption assembled: <UUID>
   [INFO] Starting model baking for contraption <UUID> with X rendered block entities
   [INFO] Successfully baked model for LittleTiles block at <position>
   [INFO] Model baking completed for contraption <UUID>: X LittleTiles blocks found, Y models baked
   ```

### Step 4: Use Debug Commands

Use the built-in commands to monitor the system:

```
/modelbaking-test status
```
Shows current cache statistics and system status.

```
/modelbaking-test list-contraptions
```
Lists all active contraptions in the world.

```
/contraption-render list
```
Shows contraptions with rendering information.

## Expected Behavior

### Successful Model Baking
- **Console logs** show successful model baking
- **LittleTiles blocks appear** in moving contraptions (may be placeholder cubes initially)
- **No crashes** during contraption assembly/disassembly

### Current Limitations
- **Visual representation**: Currently creates placeholder cubes, not accurate LittleTiles geometry
- **Performance**: Initial baking may cause brief lag for complex structures
- **Compatibility**: Works with specific versions of Create and LittleTiles

## Troubleshooting

### Problem: No models are being baked
**Symptoms**: Console shows "0 models baked" or no baking logs
**Solutions**:
1. Verify LittleTiles blocks are actually included in the contraption
2. Check if LittleTiles mod is properly loaded
3. Ensure you're using compatible mod versions

### Problem: Contraption assembly fails
**Symptoms**: Contraption won't assemble or crashes during assembly
**Solutions**:
1. Try without LittleTiles blocks first to isolate the issue
2. Check console for error messages
3. Disable the mod temporarily to see if Create works normally

### Problem: LittleTiles blocks are invisible in contraptions
**Symptoms**: Models are baked but blocks don't render
**Solutions**:
1. Check if Create's "Enable Flywheel Engine" is disabled (current requirement)
2. Verify the mixins are loading correctly
3. Check for console errors during rendering

## Development Testing

### For Developers

1. **Enable Debug Logging**: Set log level to DEBUG for detailed information
2. **Test with Simple Structures**: Start with basic LittleTiles blocks
3. **Monitor Memory Usage**: Watch for memory leaks in the cache
4. **Test Edge Cases**: Empty contraptions, very large contraptions, rapid assembly/disassembly

### Key Log Messages to Watch For

- `"Model baking completed for contraption"` - Successful baking
- `"Using cached LittleTiles model for position"` - Model retrieval working
- `"LittleTiles geometry extraction failed"` - Fallback to placeholder
- `"Error during model baking"` - System errors requiring attention

## Future Enhancements

### Planned Improvements
1. **Accurate Geometry Extraction**: Extract actual LittleTiles shapes instead of placeholder cubes
2. **Flywheel Compatibility**: Support for Create's advanced rendering engine
3. **Performance Optimization**: Reduce baking time for complex structures
4. **Visual Polish**: Better textures and lighting for baked models

### Known Issues
1. Currently only works with legacy renderer (Flywheel disabled)
2. Placeholder geometry doesn't match actual LittleTiles shapes
3. Some edge cases in contraption detection may not be handled

## Reporting Issues

When reporting issues, please include:
1. Minecraft version and modpack info
2. Complete mod versions (Create, LittleTiles, CreateLittleContraptions)
3. Console logs (especially from contraption assembly)
4. Steps to reproduce the issue
5. Screenshots or videos if applicable

---

**Note**: This is an experimental integration between two complex mods. Some issues are expected during development and testing phases.
