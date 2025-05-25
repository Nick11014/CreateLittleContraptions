# LittleTiles Block Structure Analysis

## Overview
Analysis of how LittleTiles structures blocks and stores tile data, essential for proper movement and restoration.

## Data Architecture

### Tile Storage
- Multiple tiles per block position
- Hierarchical structure with sub-tiles
- Complex geometry and material data

### NBT Format
```json
{
  "tiles": [
    {
      "box": [x1, y1, z1, x2, y2, z2],
      "block": "minecraft:stone",
      "color": 0xFFFFFF,
      "properties": {...}
    }
  ],
  "structures": [...],
  "connections": [...]
}
```

### Block Entity Integration
- `LittleTilesBlockEntity` manages tile collection
- Lazy loading and caching for performance
- Integration with Minecraft's save/load system

## Movement Challenges

### Data Preservation
- Complete tile data must transfer to contraption
- Relative positions need coordinate transformation
- Connection data between blocks must be maintained

### Performance Considerations
- Large tile collections can be expensive to serialize
- Rendering optimization needed for moving tiles
- Memory usage during contraption operation

## Implementation Strategy

1. **Data Extraction**: Extract complete tile data during contraption assembly
2. **Coordinate Transformation**: Transform tile coordinates for contraption space
3. **Data Restoration**: Restore tiles with proper world coordinates after movement
4. **Connection Maintenance**: Preserve inter-block tile connections

## Code Analysis

```java
// TODO: Add block structure code snippets
```

## Testing Requirements

- Simple single-tile blocks
- Complex multi-tile structures
- Inter-block tile connections
- Large tile collections (performance testing)
