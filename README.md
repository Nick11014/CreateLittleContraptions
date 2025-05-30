# CreateLittleContraptions

A Minecraft mod that provides enhanced debugging and control tools for Create mod contraptions, with special support for LittleTiles integration.

## Features

- **Contraption Debugging**: Detailed analysis of contraption blocks and structure
- **LittleTiles Detection**: Identifies LittleTiles blocks within contraptions
- **Contraption Control**: Basic disable/enable functionality for contraption rendering
- **Event Tracking**: Monitor contraption assembly and disassembly events

## Commands

- `/contraption-debug` - Analyze nearby contraptions and detect LittleTiles blocks
- `/contraption-render` - Control contraption rendering (disable-all/enable-all)
- `/contraption-disassembly` - Monitor and control contraption disassembly

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.172+
- Create mod 6.0.4+
- LittleTiles 1.6.0-pre163+

## Development

This mod uses Gradle for building:

```bash
# Build the mod
./gradlew build

# Run in development
./gradlew runClient
```

## Documentation

Project documentation is located in `docs/`:
- `project-docs/DEVELOPMENT_TIMELINE.md` - Development history and progress
- `project-docs/UNIVERSAL_RENDERING_CONTROL.md` - Rendering system documentation
- `project-docs/Metodos.md` - Methods and implementation details
- `project-docs/LOGS.txt` - Runtime logs and debugging information

## Status

This project has been cleaned up and simplified to focus on core functionality. Complex experimental features have been removed in favor of reliable, basic contraption control and debugging tools.
