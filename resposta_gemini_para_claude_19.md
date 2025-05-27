Okay, Claude 4, you're doing an outstanding job! The `MovementBehaviour` is registered and firing correctly, and `renderInContraption` is being called with the necessary NBT data. This confirms the core integration with Create is solid.

The `UnsupportedOperationException` from `VirtualRenderWorld.getChunk()` when `BETiles.handleUpdate()` (or `loadAdditional` -> `onLoadLevel`) is called is indeed the crux of the problem, as we anticipated. `VirtualRenderWorld` is a lightweight representation and doesn't support all operations of a full `Level`, especially those that might try to load or interact with chunks or neighboring blocks in a complex way, which `BETiles` seems to do during its full initialization or update cycle.

**You are on the right track: the solution is to bypass the full `BETiles` lifecycle within `renderInContraption` and instead directly parse the necessary rendering data from the `CompoundTag nbt = context.blockEntityData;` and use LittleTiles' rendering logic more directly.**

Let's call this approach "Direct Structure Rendering."

## New Strategy: Direct Structure Rendering from NBT

The goal is to extract the core visual information (the "structures" or "tile groups") from the NBT and render them without needing a fully initialized `BETiles` instance that interacts with a `Level` in unsupported ways.

### 1. Understanding `BETiles` NBT Structure (Key Task)

You've provided `BETiles.java`. This is where we need to look for how it saves its "structures" or "tiles."

**Action for You, Claude 4 (or provide the relevant snippets):**

*   **Locate `saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)` (or a similar save method like `saveData`, `writeNbt`):**
    *   How does it store the list of tiles or structures? Look for NBT list tags (e.g., `nbt.putList("structures", listTag)` or `nbt.put("tiles", ...)`).
    *   How is each individual tile/structure within that list serialized? What fields are saved for each (e.g., position, state, color, custom NBT for the tile itself)?
*   **Locate `loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)` (or `loadData`, `readNbt`):**
    *   How does it read this list and reconstruct the internal representation of tiles/structures? This will show us the classes LittleTiles uses to represent these structures in memory (e.g., `LittleStructure`, `LittleTile`, `LittleGroup`).
*   **Identify the "Main Group" or Root Structure Class:**
    *   In your previous logs, you mentioned `be.mainGroup.render(...)`. This `mainGroup` (or whatever it's called in the current LT version) is likely the root object we need to reconstruct from NBT. What is its class type? How is it serialized and deserialized?

**Example NBT Structure (Hypothetical - verify with `BETiles.java`):**

Let's imagine `BETiles` saves a list of `LittleStructure` objects:
```nbt
{
  // ... other BETiles data ...
  "structures": [ // This is a ListTag
    { /* NBT for LittleStructure 1 */ 
      "posX": 0, "posY": 0, "posZ": 0, // Relative position within the block
      "stateId": 123, // ID of the block state it represents
      "color": -1, // Color tint
      // ... other structure-specific data ...
    },
    { /* NBT for LittleStructure 2 */ }
  ],
  "mainGroupName": "some_identifier" // Or however the main group is referenced or stored
}
```
We need to find out the *actual* NBT keys and structure used by `team.creative.littletiles.common.block.entity.BETiles`.

### 2. Implementing `LittleTilesAPIFacade.java` (or similar helper)

This class will contain static methods to help us.

```java
// Path: com/createlittlecontraptions/compat/littletiles/LittleTilesAPIFacade.java
package com.createlittlecontraptions.compat.littletiles;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

// Import the actual LittleTiles classes for structures, groups, rendering, etc.
// Example: import team.creative.littletiles.common.structure.LittleStructure;
// Example: import team.creative.littletiles.common.structure.LittleGroup;
// Example: import team.creative.littletiles.common.block.little.tile.LittleTile;
// Example: import team.creative.littletiles.client.render.tile.LittleTileRenderHelper; // Or whatever LT uses for rendering

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LittleTilesAPIFacade {

    private static final Logger LOGGER = LogManager.getLogger("CreateLittleContraptions/LTAPIFacade");

    // Represents the data needed to render a single LittleTiles "structure" or "tile"
    // This is a placeholder; you'll define it based on LittleTiles' actual structure classes.
    // public static class ParsedLittleStructure {
    //     // Example fields:
    //     // BlockPos relativePos;
    //     // BlockState emulatedState;
    //     // int color;
    //     // CompoundTag structureNBT; // Or the actual deserialized LittleStructure object
    //     // LittleStructure structureInstance; // Ideal if you can create it from NBT

    //     public ParsedLittleStructure(/*...*/) { /*...*/ }
    // }

    /**
     * Parses the essential rendering information from BETiles NBT.
     * This method AVOIDS calling any Level-dependent methods.
     * @param tileNBT The CompoundTag from MovementContext.blockEntityData
     * @return A list of objects representing the renderable structures, or perhaps a single root "group" object.
     */
    public static /* List<ParsedLittleStructure> OR ActualLittleTilesGroupClass */ Object parseStructuresFromNBT(CompoundTag tileNBT, BlockState containerState, BlockPos containerPos) {
        if (tileNBT == null) {
            LOGGER.warn("parseStructuresFromNBT: tileNBT is null.");
            return null; // Or an empty list/default group
        }
        LOGGER.debug("parseStructuresFromNBT for state: {}, pos: {}", containerState, containerPos);

        // TODO: Based on your investigation of BETiles.java's save/load methods:
        // 1. Identify the NBT key for the list of structures/tiles (e.g., "structures", "tiles", "groupData").
        // 2. If it's a ListTag, iterate through it.
        // 3. For each CompoundTag in the ListTag, deserialize it into an instance of LittleTiles'
        //    actual structure/tile class (e.g., LittleStructure, LittleTile).
        //    LittleTiles classes might have static factory methods like `LittleStructure.load(CompoundTag)`
        //    or constructors that take a CompoundTag. PRIORITIZE using these.
        //
        // Example (HIGHLY HYPOTHETICAL - ADAPT TO ACTUAL LITTLETILES API):
        // if (tileNBT.contains("mainGroupData", Tag.TAG_COMPOUND)) {
        //     CompoundTag groupNBT = tileNBT.getCompound("mainGroupData");
        //     try {
        //         // Assuming LittleGroup has a constructor or static method to load from NBT
        //         // team.creative.littletiles.common.structure.LittleGroup mainGroup = 
        //         //     new team.creative.littletiles.common.structure.LittleGroup(groupNBT); 
        //         // OR
        //         // team.creative.littletiles.common.structure.LittleGroup mainGroup = 
        //         //     team.creative.littletiles.common.structure.LittleGroup.fromNBT(groupNBT, some_context_if_needed);
        //         
        //         // LOGGER.info("Successfully parsed main group from NBT.");
        //         // return mainGroup; // Return the actual, deserialized root object
        //     } catch (Exception e) {
        //         LOGGER.error("Error deserializing LittleTiles main group from NBT", e);
        //         return null;
        //     }
        // } else {
        //     LOGGER.warn("NBT for {} at {} does not contain 'mainGroupData'", containerState, containerPos);
        //     return null;
        // }

        // Placeholder:
        LOGGER.error("parseStructuresFromNBT: NOT IMPLEMENTED YET. Need to analyze BETiles NBT structure.");
        return null; 
    }

    /**
     * Renders the parsed LittleTiles structures/group.
     * @param parsedData The data returned by parseStructuresFromNBT (e.g., a LittleGroup instance)
     * @param poseStack The PoseStack, already transformed to the block's local space in the contraption.
     * @param bufferSource The MultiBufferSource.
     * @param combinedLight Combined light value.
     * @param combinedOverlay Combined overlay value.
     * @param partialTicks Partial ticks for animation.
     */
    public static void renderDirectly(Object parsedData, PoseStack poseStack, MultiBufferSource bufferSource, 
                                      int combinedLight, int combinedOverlay, float partialTicks) {
        if (parsedData == null) {
            return;
        }
        LOGGER.debug("renderDirectly called with data: {}", parsedData.getClass().getSimpleName());

        // TODO: Implement rendering using LittleTiles' own rendering logic.
        // This will heavily depend on the type of 'parsedData'.
        // If 'parsedData' is an instance of LittleTiles' main renderable group/structure class:
        // Example (HIGHLY HYPOTHETICAL):
        // if (parsedData instanceof team.creative.littletiles.common.structure.LittleGroup) {
        //    team.creative.littletiles.common.structure.LittleGroup mainGroup = (team.creative.littletiles.common.structure.LittleGroup) parsedData;
        //
        //    // LittleTiles might have a dedicated renderer or the group itself might have a render method.
        //    // Look for methods like:
        //    // mainGroup.render(poseStack, bufferSource, combinedLight, combinedOverlay, partialTicks);
        //    // OR
        //    // team.creative.littletiles.client.render.tile.LittleTileRenderHelper.renderGroup(
        //    //     mainGroup, poseStack, bufferSource, combinedLight, combinedOverlay, partialTicks
        //    // );
        //    
        //    // The method signature might differ. It needs the current PoseStack (which should already be
        //    // translated to the local position of this block within the contraption by MovementBehaviour),
        //    // buffers, light, overlay, and partialTicks.
        //    LOGGER.info("Attempting to render LittleGroup directly...");
        //    // mainGroup.renderTick(poseStack, bufferSource, combinedLight, combinedOverlay, partialTicks, null); // renderTick is often used
        // } else {
        //    LOGGER.warn("renderDirectly: parsedData is not of the expected LittleTiles group type. Is: {}", parsedData.getClass().getName());
        // }
        
        // Placeholder:
        LOGGER.error("renderDirectly: NOT IMPLEMENTED YET. Need to use LittleTiles rendering API.");
    }
}
```

### 3. Updating `LittleTilesContraptionRenderer.renderMovementBehaviourTile`

This method will now use the `LittleTilesAPIFacade`.

```java
// In LittleTilesContraptionRenderer.java
public static boolean renderMovementBehaviourTile(MovementContext context, VirtualRenderWorld renderWorld,
                                                ContraptionMatrices matrices, MultiBufferSource buffer) {
    boolean hasNBT = context.blockEntityData != null && !context.blockEntityData.isEmpty();
    LOGGER.info("🎨 [CLC Renderer] Iniciando renderMovementBehaviourTile para: {} com NBT (existe? {})", 
               context.localPos, hasNBT);

    if (!hasNBT) {
        LOGGER.warn("⚠️ [CLC Renderer] Nenhum dado NBT encontrado para: {}", context.localPos);
        return false; // No data to render
    }

    try {
        Object parsedStructures = LittleTilesAPIFacade.parseStructuresFromNBT(
            context.blockEntityData, context.state, context.localPos
        );

        if (parsedStructures == null) {
            LOGGER.warn("⚠️ [CLC Renderer] Failed to parse structures from NBT for {}. Aborting render.", context.localPos);
            return false;
        }
        LOGGER.debug("[CLC Renderer] Successfully parsed NBT structures for {}", context.localPos);

        // Prepare for rendering
        PoseStack poseStack = matrices.getViewProjection(); // Or getModelViewProjection() - check Create API
                                                          // This PoseStack should be for the contraption as a whole.
                                                          // Individual block transformations within the contraption
                                                          // are usually handled by translating this stack.
        poseStack.pushPose();
        // Apply local translation for this specific block within the contraption
        poseStack.translate(context.localPos.getX(), context.localPos.getY(), context.localPos.getZ());

        // Determine light and overlay. Create's ContraptionMatrices might have helper methods,
        // or you might need to calculate it based on renderWorld and context.localPos if LT's renderer needs it per-block.
        // For now, let's assume FULL_BRIGHT as a starting point if specific light isn't easily available
        // or if LittleTiles' internal renderer handles light sampling.
        // Create's ContraptionMatrices usually provides light via matrices.getLight() related to the entire contraption's transformed position.
        // The light calculation might be complex within VirtualRenderWorld.
        // Your `BlockEntityRenderHelper` Mixin was capturing light. Can we get it from MovementContext or matrices?
        // MovementContext doesn't directly provide light. ContraptionMatrices often has light information
        // for the *entire* contraption's current position, not per-block.
        // This is a tricky part. LittleTiles rendering methods will expect a light value.
        // Let's use a placeholder and investigate lighting specific to MovementBehaviour.
        
        // For now, let's try to get light as Create does for normal BEs in contraptions
        // This is complex and might not be directly available in MovementContext.
        // For initial testing, FULL_BRIGHT might be okay, or a fixed value.
        // The `renderWorld.getCombinedLight(context.localPos, 0)` might work if getChunk is not called.
        int packedLight = LightTexture.FULL_BRIGHT; // Placeholder - NEEDS PROPER IMPLEMENTATION
        int packedOverlay = OverlayTexture.NO_OVERLAY;
        float partialTicks = AnimationTickHolder.getPartialTicks(); // From Create's AnimationTickHolder

        LOGGER.debug("[CLC Renderer] Attempting direct render for {} with light {} pose {}", context.localPos, packedLight, poseStack.last().pose());

        LittleTilesAPIFacade.renderDirectly(
            parsedStructures,
            poseStack,
            buffer,
            packedLight,
            packedOverlay,
            partialTicks
        );

        poseStack.popPose();
        LOGGER.info("✅ [CLC Renderer] Direct rendering attempted for: {}", context.localPos);
        return true; // Indicate rendering was attempted

    } catch (Exception e) {
        LOGGER.error("❌ [CLC Renderer] Erro inesperado em renderMovementBehaviourTile para {}: {}", context.localPos, e.getMessage(), e);
        return false;
    }
}
```

**Collision Shape in `LittleTilesMovementBehaviour.getCollisionShapeInContraption`:**

This will follow a similar pattern: parse the NBT using `LittleTilesAPIFacade` to get the structure(s), then call a method on the parsed structure(s) to get its collision shape. LittleTiles likely has a method like `mainGroup.getCollisionShape(CollisionContext)`.

```java
// In LittleTilesMovementBehaviour.java
@Override
public VoxelShape getCollisionShapeInContraption(MovementContext context) {
    LOGGER.debug("getCollisionShapeInContraption called for pos: {}", context.localPos);
    CompoundTag nbt = context.blockEntityData;

    if (nbt == null || nbt.isEmpty()) {
        LOGGER.warn("getCollisionShapeInContraption: NBT data is null for pos: {}. State: {}", context.localPos, context.state);
        return Shapes.block();
    }

    try {
        Object parsedStructures = LittleTilesAPIFacade.parseStructuresFromNBT(nbt, context.state, context.localPos);
        if (parsedStructures != null) {
            // TODO: Call a method on parsedStructures to get its VoxelShape
            // Example (HIGHLY HYPOTHETICAL):
            // if (parsedStructures instanceof team.creative.littletiles.common.structure.LittleGroup) {
            //    team.creative.littletiles.common.structure.LittleGroup mainGroup = (team.creative.littletiles.common.structure.LittleGroup) parsedStructures;
            //    VoxelShape shape = mainGroup.getCollisionShape(CollisionContext.empty()); // Or appropriate context
            //    LOGGER.debug("getCollisionShapeInContraption: Got shape {} for {}", shape, context.localPos);
            //    return shape;
            // }
            LOGGER.warn("getCollisionShapeInContraption: parsedStructures not of expected type or getCollisionShape not implemented in Facade.");
        }
    } catch (Exception e) {
        LOGGER.error("Error getting LittleTile collision shape in contraption at " + context.localPos, e);
    }
    return Shapes.block(); // Fallback
}
```

### 4. Prioritized Research and Implementation Steps for You:

1.  **Deep Dive into `BETiles.java` `saveAdditional` / `loadAdditional` (or equivalent):**
    *   **Goal**: Understand exactly how `mainGroup` or the primary list of structures is serialized to and deserialized from NBT.
    *   **Identify**:
        *   The NBT key(s) used (e.g., `"group"`, `"tiles"`, `"structures"`).
        *   The class type of `mainGroup` (e.g., `team.creative.littletiles.common.structure.LittleGroup`).
        *   Does this class have a constructor or static factory method that takes a `CompoundTag` and reconstructs the object *without needing a `Level`*? This is critical. (e.g., `new LittleGroup(CompoundTag nbt)` or `LittleGroup.fromNBT(CompoundTag nbt)`).
        *   If it *does* require a `Level` or other complex context for deserialization, we need to see if there's a more "raw" representation we can get or if we can supply a dummy/minimal context.

2.  **Investigate `mainGroup.render(...)` and `mainGroup.getCollisionShape(...)` (or equivalents):**
    *   **Goal**: Find the methods on the deserialized `mainGroup` (or equivalent structure object) that perform rendering and provide collision shapes.
    *   **Identify**:
        *   The exact signature for its render method. What parameters does it need (PoseStack, MultiBufferSource, light, overlay, partialTicks)?
        *   The exact signature for its collision shape method.

3.  **Implement `LittleTilesAPIFacade.parseStructuresFromNBT`**:
    *   Based on findings from (1), implement the logic to deserialize the `mainGroup` or structure list from the `tileNBT`. The method should return the deserialized root renderable object (e.g., the `LittleGroup` instance).

4.  **Implement `LittleTilesAPIFacade.renderDirectly`**:
    *   Based on findings from (2), call the appropriate render method on the `parsedData` (which is your deserialized `LittleGroup` or similar).

5.  **Implement Collision Logic**: Update `LittleTilesMovementBehaviour.getCollisionShapeInContraption` to use the facade to get the parsed structures and then call their collision shape method.

6.  **Address Lighting in `renderMovementBehaviourTile`**:
    *   This is a tricky point. The `MovementContext` doesn't directly provide per-block lighting information in the same way `BlockEntityRenderer`s receive it.
    *   `ContraptionMatrices` might have lighting information relevant to the overall contraption's transformed position in the world. Look for methods like `matrices.getLightEmission(localPos)` or how light is passed to `SuperByteBuffer.renderInto`.
    *   Alternatively, `renderWorld.getBrightness(LightLayer.BLOCK, context.localPos.offset(contraption.getAnchor()))` (if `getChunk` isn't called) might work, but VirtualRenderWorld is limited.
    *   For a first pass, `LightTexture.FULL_BRIGHT` or a hardcoded bright value can be used to confirm rendering, then refine lighting. LittleTiles' own renderer might also do its own light sampling based on the `PoseStack`.

This direct NBT-to-renderable-structure approach is the most robust way to handle `VirtualRenderWorld`'s limitations. It requires a good understanding of LittleTiles' internal data representation and rendering API.

You're very close to a solution! Focus on how `BETiles` turns its state into NBT and back into a renderable/collidable object graph *without* relying heavily on `Level` access for the core structure data.