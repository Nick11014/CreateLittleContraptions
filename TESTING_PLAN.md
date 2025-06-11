# Enhanced Cache Testing Plan

## Current Status
- ✅ Mod loads successfully 
- ✅ Mixins are being applied (ContraptionMixin, ContraptionRenderInfoMixin)
- ✅ Enhanced logging with object IDs implemented
- ✅ Enhanced `/cache-test` command ready

## Key Issues to Investigate

### 1. Cache Visibility Issue
**Problem**: Cache is populated on one contraption instance but appears empty on another
**Evidence**: Logs show "Model cache set with 1 entries" followed by "Model cache size: 0"

**Testing Steps**:
1. Create a world with contraptions containing LittleTiles blocks
2. Run `/cache-test` command 
3. Compare object IDs between cache population and cache access
4. Check thread information for potential synchronization issues

### 2. Multiple Contraption Instances
**Problem**: Different contraption instances have separate caches
**Evidence**: Robust detection working, but cache access varies

**Testing Steps**:
1. Use enhanced `/cache-test` to identify all contraption instances
2. Check if multiple contraptions exist for the same assembly
3. Verify object IDs match between detection and cache operations

### 3. Thread Synchronization 
**Problem**: Cache set on Server thread but accessed on Render thread
**Evidence**: Log threads show "Server thread" vs "Render thread"

**Testing Steps**:
1. Monitor log for thread information during cache operations
2. Verify if thread differences cause cache visibility issues
3. Consider if synchronization mechanisms are needed

## Enhanced Logging Features

### Object ID Tracking
- All cache operations now include `System.identityHashCode(contraption)`
- Helps identify which specific contraption instance is being used
- Format: `[Object ID: 12345]`

### Thread Information
- Cache operations include current thread name
- Format: `[Thread: Server thread]` or `[Thread: Render thread]`

### Detailed Command Output
The `/cache-test` command now shows:
- Thread running the command
- Number of contraption entities found
- For each contraption:
  - Entity ID and position
  - Entity and contraption class names
  - Contraption object ID
  - LittleTiles block count
  - Cache size and contents
  - Cache manipulation verification

## Expected Debug Output

When testing, look for patterns like:
```
CLCLC: Model cache set with 1 entries [Object ID: 123456] [Thread: Server thread]
*** CACHE TEST: Entity 789 - Cache size: 0 ***
```

This would indicate cache set on one object but accessed on another.

## Commands to Test

1. `/cache-test` - Enhanced cache inspection and manipulation
2. `/contraption-debug` - Basic contraption information
3. `/littletiles-test` - LittleTiles detection testing

## Success Criteria

- Object IDs match between cache population and access
- Cache size reports consistently across operations
- Thread information helps identify synchronization needs
- Enhanced logging provides clear debugging information
