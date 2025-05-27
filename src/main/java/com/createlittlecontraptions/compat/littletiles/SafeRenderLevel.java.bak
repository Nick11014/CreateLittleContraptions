package com.createlittlecontraptions.compat.littletiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * A wrapper around VirtualRenderWorld that provides safe implementations for operations
 * that LittleTiles needs but VirtualRenderWorld doesn't support.
 * 
 * This prevents UnsupportedOperationException when BETiles tries to call markDirty().
 */
public class SafeRenderLevel extends Level {
    private static final Logger LOGGER = LoggerFactory.getLogger(SafeRenderLevel.class);
    private final VirtualRenderWorld wrappedWorld;

    public SafeRenderLevel(VirtualRenderWorld wrappedWorld) {
        super(
            wrappedWorld.getWritableLevelData(),
            wrappedWorld.dimension(),
            wrappedWorld.registryAccess(),
            wrappedWorld.dimensionTypeRegistration(),
            wrappedWorld::getProfiler,
            true, // isClientSide - important for LittleTiles
            false, // isDebug
            wrappedWorld.getBiomeManager().biomeZoomSeed,
            1000000 // maxChainedNeighborUpdates
        );
        this.wrappedWorld = wrappedWorld;
        
        LOGGER.debug("SafeRenderLevel created wrapping VirtualRenderWorld");
    }

    // ========== Safe Overrides to Prevent UnsupportedOperationException ==========

    @Override
    public ChunkAccess getChunk(int x, int z) {
        try {
            return wrappedWorld.getChunk(x, z);
        } catch (UnsupportedOperationException e) {
            LOGGER.debug("getChunk({}, {}) called but not supported by VirtualRenderWorld, returning null", x, z);
            return null; // Safe fallback
        }
    }

    @Override
    public ChunkAccess getChunk(int x, int z, net.minecraft.world.level.chunk.status.ChunkStatus requiredStatus, boolean load) {
        try {
            return wrappedWorld.getChunk(x, z, requiredStatus, load);
        } catch (UnsupportedOperationException e) {
            LOGGER.debug("getChunk({}, {}, {}, {}) called but not supported, returning null", x, z, requiredStatus, load);
            return null;
        }
    }

    @Override
    public void blockEntityChanged(BlockPos pos) {
        // This is the method that causes the problem in markDirty()
        // We'll make it a safe no-op for rendering contexts
        LOGGER.debug("blockEntityChanged({}) called - safe no-op for rendering context", pos);
        // Do nothing instead of calling super.blockEntityChanged(pos) which would try to access chunks
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
        // Safe no-op for rendering context
        LOGGER.debug("setBlockEntity({}) called - safe no-op for rendering context", blockEntity.getBlockPos());
    }

    // ========== Delegate Everything Else to VirtualRenderWorld ==========

    @Override
    public ChunkSource getChunkSource() {
        return wrappedWorld.getChunkSource();
    }

    @Override
    protected void playMoodSoundAndCheckLight(int x, int z, ChunkAccess chunk) {
        // Delegate if supported, otherwise no-op
        try {
            super.playMoodSoundAndCheckLight(x, z, chunk);
        } catch (Exception e) {
            LOGGER.debug("playMoodSoundAndCheckLight() failed, ignoring: {}", e.getMessage());
        }
    }

    @Override
    public Holder<Biome> getBiome(BlockPos pos) {
        return wrappedWorld.getBiome(pos);
    }

    @Override
    public boolean isClientSide() {
        return true; // Always client side for rendering
    }

    @Override
    public RegistryAccess registryAccess() {
        return wrappedWorld.registryAccess();
    }

    // Add other delegation methods as needed...
    // For now, the key ones are the chunk and blockEntity methods that cause issues
}
