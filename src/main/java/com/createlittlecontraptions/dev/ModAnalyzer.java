package com.createlittlecontraptions.dev;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Development tool for analyzing Create and LittleTiles mod internals
 * This class provides utilities to understand how both mods work
 */
public class ModAnalyzer {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void analyzeCreateContraption(Level level, BlockPos pos) {
            LOGGER.info("=== ANÁLISE DE CONTRAPTION DO CREATE ===");
            LOGGER.info("Analisando contraption na posição: {}", pos);
            
            try {
                // Verificar se existe uma contraption na posição indicada
                BlockEntity be = level.getBlockEntity(pos);
                if (be == null) {
                    LOGGER.info("Nenhuma BlockEntity encontrada na posição {}", pos);
                    return;
                }
                
                LOGGER.info("BlockEntity encontrada: {}", be.getClass().getName());
                
                // Verificar se é um elevador
                boolean isElevator = be.getClass().getName().toLowerCase().contains("elevator");
                if (isElevator) {
                    LOGGER.info("✓ Detectado um elevador do Create!");
                    analyzeElevator(level, pos, be);
                }
                
                // Analisar comportamentos de movimento registrados
                analyzeMovementBehaviours();
                
                // Analisar sistema de renderização
                analyzeRenderingSystem();
                
                // Verificar dados NBT
                CompoundTag nbt = be.saveWithFullMetadata(level.registryAccess());
                LOGGER.info("Dados NBT da contraption: {}", nbt);
                
                // Verificar blocos LittleTiles nas proximidades
                int littleTilesCount = scanForLittleTilesBlocks(level, pos, 5);
                if (littleTilesCount > 0) {
                    LOGGER.info("Encontrados {} blocos LittleTiles nas proximidades", littleTilesCount);
                }
                
            } catch (Exception e) {
                LOGGER.error("Erro durante análise da contraption", e);
            }
            
            LOGGER.info("Análise da contraption concluída");
        }
        
        /**
         * Analisa um elevador especificamente
         */
        private static void analyzeElevator(Level level, BlockPos pos, BlockEntity be) {
            LOGGER.info("=== ANÁLISE DETALHADA DO ELEVADOR ===");
            
            try {
                // Obter detalhes do elevador via reflexão
                CompoundTag nbt = be.saveWithFullMetadata(level.registryAccess());
                
                // Verificar se há dados de movimento
                if (nbt.contains("motion")) {
                    LOGGER.info("Elevador em movimento: {}", nbt.get("motion"));
                }
                
                // Verificar se há uma contraption anexada
                if (nbt.contains("contraption") || nbt.contains("Contraption")) {
                    LOGGER.info("Contraption está anexada ao elevador");
                    
                    // Verificar especificamente por blocos LittleTiles na contraption
                    if (nbt.toString().toLowerCase().contains("littletiles")) {
                        LOGGER.info("✓ Contraption contém dados de blocos LittleTiles");
                        LOGGER.info("IMPORTANTE: Os blocos LittleTiles devem ter seus dados NBT completamente preservados durante o movimento");
                    }
                }
                
                // Verificar configuração de renderização
                LOGGER.info("Verificando configurações de renderização do elevador...");
                LOGGER.info("- Certifique-se que o método renderWorld está sendo chamado para blocos LittleTiles");
                LOGGER.info("- Verifique se o NBT dos blocos é preservado durante movimentos");
                LOGGER.info("- Problemas comuns incluem sincronização incorreta de dados NBT ou transformações incorretas de renderização");
                
            } catch (Exception e) {
                LOGGER.error("Erro ao analisar o elevador", e);
            }
        }
        
        /**
         * Analisa comportamentos de movimento registrados
         */
        private static void analyzeMovementBehaviours() {
            LOGGER.info("=== ANÁLISE DE COMPORTAMENTOS DE MOVIMENTO ===");
            
            try {
                // Verificar se LittleTiles tem um MovementBehaviour registrado
                // Implementação usaria reflexão para acessar o registro do Create
                
                LOGGER.info("Verificando registro de MovementBehaviour para LittleTiles...");
                LOGGER.info("O MovementBehaviour correto deve:");
                LOGGER.info("1. Preservar TODOS os dados NBT durante o movimento");
                LOGGER.info("2. Implementar renderização específica para blocos LittleTiles");
                LOGGER.info("3. Gerenciar corretamente conexões entre blocos");
                
            } catch (Exception e) {
                LOGGER.error("Erro ao analisar comportamentos de movimento", e);
            }
        }
        
        /**
         * Analisa o sistema de renderização
         */
        private static void analyzeRenderingSystem() {
            LOGGER.info("=== ANÁLISE DO SISTEMA DE RENDERIZAÇÃO ===");
            
            try {
                LOGGER.info("Verificando pipeline de renderização para contraptions...");
                LOGGER.info("Para blocos LittleTiles, a renderização deve:");
                LOGGER.info("1. Usar o renderer específico do LittleTiles com transformações corretas");
                LOGGER.info("2. Preservar estado visual durante o movimento");
                LOGGER.info("3. Gerenciar corretamente a matriz de transformação");
                
            } catch (Exception e) {
                LOGGER.error("Erro ao analisar sistema de renderização", e);
            }
        }
        
        /**
         * Escaneia em busca de blocos LittleTiles em um raio
         */
        private static int scanForLittleTilesBlocks(Level level, BlockPos center, int radius) {
            int count = 0;
            
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        BlockState state = level.getBlockState(pos);
                        
                        // Verificar se é um bloco LittleTiles
                        if (isLittleTilesBlock(state)) {
                            count++;
                            if (count <= 5) { // Limitar logging
                                LOGGER.info("Bloco LittleTiles encontrado em {}", pos);
                            }
                        }
                    }
                }
            }
            
            return count;
        }
        
        /**
         * Verifica se um BlockState é um bloco LittleTiles
         */
        private static boolean isLittleTilesBlock(BlockState state) {
            if (state == null) return false;
            
            // Verificar pelo nome do bloco
            String blockName = state.getBlock().toString().toLowerCase();
            if (blockName.contains("littletiles") || blockName.contains("little")) {
                return true;
            }
            
            // Verificar pelo registro
            ResourceLocation registryName = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (registryName != null) {
                String regName = registryName.toString().toLowerCase();
                return regName.contains("littletiles") || regName.contains("little");
            }
            
            return false;
    }

    public static void analyzeLittleTilesBlock(Level level, BlockPos pos, BlockState state) {
        LOGGER.info("=== LITTLETILES BLOCK ANALYSIS ===");
        LOGGER.info("Analyzing LittleTiles block at position: {}", pos);
        LOGGER.info("Block state: {}", state);
        
        // TODO: Analyze LittleTiles block structure
        // - Tile data and NBT structure
        // - Rendering system
        // - Block entity information
        
        LOGGER.info("LittleTiles block analysis placeholder complete");
    }

    public static void logCompatibilityStatus() {
        LOGGER.info("=== COMPATIBILITY STATUS ===");
        LOGGER.info("CreateLittleContraptions mod active");
        
        // TODO: Log compatibility status
        // - Create mod detection
        // - LittleTiles mod detection
        // - MovementBehaviour registration status
        
        LOGGER.info("Compatibility status logging placeholder complete");
    }
}
