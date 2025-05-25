# Reference Code Snippets

## Purpose
This directory contains code snippets and analysis from Create and LittleTiles mods to facilitate understanding their internal systems.

## Structure

### create-snippets/
Contains important code snippets from Create mod:
- `MovementBehaviour.java` - Base movement behaviour interface
- `ContraptionRenderer.java` - Contraption rendering system
- `Contraption.java` - Core contraption logic

### littletiles-snippets/
Contains important code snippets from LittleTiles mod:
- `LittleTilesBlock.java` - Main block class
- `LittleTilesBlockEntity.java` - Block entity implementation
- `LittleTileRenderer.java` - Tile rendering system

## Usage

These snippets are for analysis and reference only. They help understand:
1. How each mod implements their core systems
2. What methods and interfaces are available for integration
3. How data flows through each system
4. Extension points for compatibility

## Analysis Process

1. **Copy relevant source files** from mod JARs or repositories
2. **Analyze key methods** and data structures
3. **Document integration points** in our compatibility layer
4. **Plan implementation** based on discovered APIs

## Important Notes

- These files are for reference only, not compilation
- They represent specific versions of the mods
- Always verify against actual mod versions during development
- Respect original mod licenses and attribution
