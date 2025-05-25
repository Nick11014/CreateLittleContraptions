# LittleTiles Rendering System Analysis

## Overview
Analysis of LittleTiles' rendering system to understand how tiles are rendered and how to preserve this during Create contraption movement.

## Key Classes to Analyze

### Rendering Components
- `com.creativemd.littletiles.client.render.tile.LittleTileRenderer`
- `com.creativemd.littletiles.client.render.cache.RenderingCache`
- Tile-to-vertex conversion system

### Data Structures
- `com.creativemd.littletiles.common.structure.LittleStructure`
- `com.creativemd.littletiles.common.tile.LittleTile`
- NBT serialization format

### Block Integration
- `com.creativemd.littletiles.common.block.LittleTilesBlock`
- `com.creativemd.littletiles.common.block.entity.LittleTilesBlockEntity`
- How tiles integrate with Minecraft blocks

## Key Questions

1. How are tiles stored in NBT format?
2. How does the rendering system convert tile data to vertices?
3. What caching mechanisms exist for tile rendering?
4. How are tile textures and materials handled?

## Rendering Pipeline

### Normal Rendering
1. BlockEntity contains tile data
2. Renderer queries tiles from BlockEntity
3. Tiles converted to render quads
4. Quads rendered with appropriate textures

### During Contraption Movement
- BlockEntity data must be preserved in contraption NBT
- Custom rendering logic needed to access tile data
- Vertex generation must work without world access

## Implementation Notes

- Tile NBT data critical for movement preservation
- Custom renderer needed for contraption integration
- Caching system may need adaptation for moving tiles

## Code Snippets

```java
// TODO: Add LittleTiles code snippets after analysis
```
