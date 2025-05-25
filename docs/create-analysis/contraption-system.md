# Create Contraption System Analysis

## Overview
This document analyzes Create's contraption system to understand how blocks are moved and rendered during contraption operation.

## Key Classes to Analyze

### MovementBehaviour System
- `com.simibubi.create.content.contraptions.behaviour.MovementBehaviour`
- `com.simibubi.create.content.contraptions.behaviour.MovementContext`
- Registration system for custom movement behaviours

### Contraption Rendering
- `com.simibubi.create.content.contraptions.render.ContraptionRenderer`
- `com.simibubi.create.content.contraptions.render.ContraptionRenderDispatcher`
- How blocks are rendered while moving

### Block Movement Mechanics
- `com.simibubi.create.content.contraptions.Contraption`
- `com.simibubi.create.content.contraptions.AssemblyException`
- Block assembly and disassembly process

## Key Questions to Answer

1. How does Create register MovementBehaviours for specific blocks?
2. How are block states and NBT data preserved during movement?
3. How does the rendering system handle moving blocks?
4. What hooks exist for custom block movement handling?

## Implementation Notes

- LittleTiles blocks need custom MovementBehaviour
- Rendering pipeline must preserve tile data during movement
- NBT data serialization/deserialization critical for tile preservation

## Code Snippets

```java
// TODO: Add Create code snippets here after analysis
```
