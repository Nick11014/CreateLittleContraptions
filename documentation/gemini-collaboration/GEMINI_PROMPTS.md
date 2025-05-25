# GEMINI AI PROMPT - CreateLittleContraptions Integration

## 🎯 PROMPT FOR GEMINI AI

```
Hello Gemini! I'm Claude 4 working as an AI agent to create a compatibility mod between Create and LittleTiles mods for Minecraft.

PROBLEM SUMMARY:
The main issue is that when you create a Create elevator contraption containing LittleTiles blocks, those blocks completely disappear (no rendering AND no collision). The blocks only reappear when you disassemble the contraption. However, if you move the elevator to another floor and then disassemble, the LittleTiles blocks have moved perfectly with the elevator - they were "there" but invisible/non-solid.

CURRENT STATUS:
- ✅ Mod compiles successfully (NeoForge 1.21.1)
- ✅ Both Create and LittleTiles mods are detected correctly
- ✅ Integration framework is working (contraptions are found and analyzed)
- ❌ LittleTiles blocks disappear immediately upon contraption assembly
- ❌ Current mixin approach (@Mixin(BlockRenderDispatcher.class)) is not working
- ❌ Log shows "Fixed 0 out of 1 contraptions"

TECHNICAL ANALYSIS NEEDED:
1. How does Create's contraption assembly process work? (specifically how blocks are "captured" into contraptions)
2. What happens to LittleTiles block data during this assembly process?
3. How should LittleTiles blocks be properly rendered within Create contraption context?
4. What is the correct integration point to preserve LittleTiles functionality in contraptions?

GOAL:
LittleTiles blocks should remain visible and functional (collision, interaction) throughout the contraption lifecycle: assembly → movement → disassembly.

ATTACHED FILES:
- Complete project documentation and technical analysis
- Current code implementation details  
- Latest test logs showing the problem

Please analyze the Create and LittleTiles mod source code and provide a specific technical solution with working code implementation.

Thank you for your assistance!
```

---

## � FILES TO ATTACH:
1. GEMINI_PROJECT_DOCUMENTATION.md
2. GEMINI_CODE_ANALYSIS.md  
3. runs/client/logs/latest.log
4. Any specific source files if requested

---

**This prompt is optimized for the context you provided, where you'll be sending the files directly and explaining that Claude 4 is working on the mod integration.**
