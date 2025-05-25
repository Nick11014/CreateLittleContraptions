package com.createlittlecontraptions.compat.littletiles;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;

/**
 * Custom MovementBehaviour for LittleTiles blocks
 * Handles how LittleTiles blocks behave when moved by Create contraptions
 */
public class LittleTilesMovementBehaviour {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Called when a LittleTiles block is about to be moved by a contraption
     */
    public static boolean canMoveBlock(Level level, BlockPos pos, BlockState state) {
        LOGGER.debug("Checking if LittleTiles block can be moved at {}", pos);
        
        // TODO: Implement proper checks for LittleTiles block movement
        // Check if the block has valid LittleTiles data
        // Verify that the structure can be safely moved
        
        return true; // Placeholder - allow movement for now
    }

    /**
     * Called when a LittleTiles block is being prepared for movement
     */
    public static CompoundTag prepareLittleTilesData(Level level, BlockPos pos, BlockState state) {
        LOGGER.debug("Preparing LittleTiles data for movement at {}", pos);
        
            try {
                // Obter a BlockEntity (tile entity) no local do bloco
                BlockEntity tileEntity = level.getBlockEntity(pos);
                if (tileEntity == null) {
                    LOGGER.warn("Nenhuma BlockEntity encontrada para o bloco LittleTiles em {}", pos);
                    return new CompoundTag();
                }
                
                // Salvar todos os dados da tile entity, incluindo NBT completo
                CompoundTag data = tileEntity.saveWithFullMetadata(level.registryAccess());
                
                // Adicionar metadados adicionais para rastreamento
                data.putString("type", "littletiles_block");
                data.putInt("x", pos.getX());
                data.putInt("y", pos.getY());
                data.putInt("z", pos.getZ());
                
                LOGGER.debug("Dados LittleTiles preparados com sucesso: {} bytes de dados NBT", data.toString().length());
                return data;
            } catch (Exception e) {
                LOGGER.error("Erro ao preparar dados LittleTiles para movimento", e);
                CompoundTag fallbackData = new CompoundTag();
                fallbackData.putString("type", "littletiles_error");
                fallbackData.putString("error", e.getMessage());
                return fallbackData;
            }
    }

    /**
     * Called when a LittleTiles block is being placed after movement
     */
    public static void restoreLittleTilesData(Level level, BlockPos pos, BlockState state, CompoundTag data) {
        LOGGER.debug("Restoring LittleTiles data after movement at {}", pos);
        
        // TODO: Restore LittleTiles NBT data after contraption movement
        // This should recreate all tiles, connections, and properties
        
        LOGGER.debug("LittleTiles data restoration placeholder complete");
    }

    /**
     * Called during contraption rendering to handle LittleTiles block rendering
     */
    public static void handleContraptionRendering(BlockPos pos, BlockState state, CompoundTag data) {
        LOGGER.debug("Handling LittleTiles contraption rendering at {}", pos);
        
        // TODO: Implement custom rendering for LittleTiles blocks in contraptions
        // This is the key to fixing the invisible blocks issue
        
        LOGGER.debug("LittleTiles contraption rendering placeholder complete");
    }
}
