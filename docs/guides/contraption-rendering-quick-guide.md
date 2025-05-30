# 🎮 Contraption Rendering Control - Quick Guide

## Basic Usage

### 1. List All Contraptions
```
/contraption-render list
```
Shows all contraptions in the world with their IDs and positions.

### 2. Select a Contraption
```
/contraption-render select <id>
```
Select contraption by ID to control its rendering.

### 3. Hide Individual Blocks
```
/contraption-render disable <x> <y> <z>
```
Hide a specific block at the given coordinates in the selected contraption.

### 4. Hide Entire Contraption  
```
/contraption-render disable-all
```
Hide all blocks in the selected contraption.

### 5. Show Everything Again
```
/contraption-render enable-all
```
Re-enable rendering for all blocks in the selected contraption.

## Advanced Controls

### Global Controls
```
/contraption-render global disable    # Hide ALL contraptions
/contraption-render global enable     # Show ALL contraptions
```

### Block Type Filtering
```
/contraption-render type littletiles disable    # Hide LittleTiles in ALL contraptions
/contraption-render type littletiles enable     # Show LittleTiles in ALL contraptions
```

### Status and Information
```
/contraption-render info                # Info about selected contraption
/contraption-render status             # Overall system status
/contraption-render controller-status  # Detailed controller state
```

## Use Cases

### 🔧 **Building and Testing**
Hide specific blocks that are interfering with your view while testing contraption movement.

### 🎬 **Screenshots and Videos** 
Hide unwanted elements for clean screenshots or video recordings.

### 🚀 **Performance**
Disable rendering of complex contraptions that cause lag when moving.

### 🎯 **LittleTiles Integration**
Selectively hide LittleTiles structures that conflict with Create contraptions.

## Control Priority

The system uses a hierarchy of controls:

1. **Global** - If disabled, nothing renders
2. **Contraption** - If disabled, entire contraption is hidden  
3. **Block** - Individual block overrides
4. **Type** - Block type filters (e.g., LittleTiles)

Higher priority settings override lower ones.

## Examples

### Hide a Noisy Engine
```
/contraption-render list
/contraption-render select 0
/contraption-render disable ~ ~1 ~
```

### Clean Up for Screenshots
```
/contraption-render type littletiles disable
/contraption-render select 0
/contraption-render disable ~ ~ ~1
```

### Performance Mode
```
/contraption-render select 1
/contraption-render disable-all
```

### Debug Mode
```
/contraption-render controller-status
/contraption-render info
```

## Tips

- Use `~ ~ ~` for your current position in disable/enable commands
- Check `controller-status` to see what's currently being filtered
- Use `info` to see how many blocks are disabled in the selected contraption
- Global and type controls affect ALL contraptions immediately
- Individual block controls only affect the selected contraption

## Troubleshooting

**Nothing is hiding?**
- Make sure you have OP permissions (level 2+)
- Check if global rendering is enabled: `/contraption-render controller-status`

**Can't see any contraptions?**
- Check if global rendering is disabled: `/contraption-render global enable`

**Commands not working?**
- Make sure you're in a world with Create contraptions
- Verify you have the correct permission level

**Blocks still showing?**
- Higher priority settings may be overriding your commands
- Check the full controller status for conflicts
