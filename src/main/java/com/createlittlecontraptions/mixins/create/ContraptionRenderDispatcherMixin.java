package com.createlittlecontraptions.mixins.create;

import com.createlittlecontraptions.duck.IContraptionBakedModelCache;
import com.createlittlecontraptions.util.LittleTilesDetector;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

/**
 * Mixin para ContraptionRenderDispatcher.
 * Intercepta a construção do buffer de estrutura para renderizador legado (sem Flywheel).
 * Se detectarmos LittleTiles na contraption e tivermos modelos "assados" em cache,
 * podemos potencialmente modificar a renderização.
 * 
 * Baseado na abordagem comprovada do mod create_interactive.
 */
@Mixin(value = ContraptionRenderDispatcher.class, remap = false)
public class ContraptionRenderDispatcherMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("CreateLittleContraptions.ContraptionRenderDispatcherMixin");
    
    static {
        LOGGER.info("CLCLC: ContraptionRenderDispatcherMixin static initializer called - Mixin loaded");
    }
    
    /**
     * Intercepta o método buildStructureBuffer para detectar e potencialmente
     * modificar a renderização de contraptions que contêm blocos LittleTiles.
     * 
     * Por enquanto, apenas detectamos e logamos para verificar se o hook está funcionando.
     * No futuro, podemos implementar modificação do SuperByteBuffer aqui.
     */
    @Inject(
        method = "buildStructureBuffer", 
        at = @At("HEAD"),
        require = 0  // Make this optional in case the method signature changes
    )
    private static void preBuildStructureBuffer(
        VirtualRenderWorld renderWorld, 
        Contraption contraption, 
        RenderType layer, 
        CallbackInfoReturnable<SuperByteBuffer> cir
    ) {
        try {
            // Verificar se esta contraption tem cache de modelos
            IContraptionBakedModelCache duck = (IContraptionBakedModelCache) contraption;
            Optional<Map<BlockPos, BakedModel>> cacheOpt = duck.getModelCache();
            
            if (cacheOpt.isPresent() && !cacheOpt.get().isEmpty()) {
                Map<BlockPos, BakedModel> cache = cacheOpt.get();
                int contraptionId = System.identityHashCode(contraption);
                
                LOGGER.info("CLCLC: buildStructureBuffer intercepted for contraption with {} cached models [ID: {}] [Layer: {}]", 
                           cache.size(), contraptionId, layer.toString());
                
                // Contar quantos são modelos LittleTiles (não placeholders)
                long littleTilesModels = cache.values().stream()
                    .filter(model -> !model.getClass().getSimpleName().equals("PlaceholderBakedModel"))
                    .count();
                
                if (littleTilesModels > 0) {
                    LOGGER.info("CLCLC: *** DETECTED LITTLETILES RENDERING - {} custom models will be used ***", littleTilesModels);
                }
                
                // Por enquanto, não cancelamos nem modificamos a renderização
                // Apenas detectamos e logamos para confirmar que o hook está funcionando
                // Em implementações futuras, podemos modificar o SuperByteBuffer aqui
            }
            
        } catch (Exception e) {
            LOGGER.error("CLCLC: Error in ContraptionRenderDispatcherMixin.preBuildStructureBuffer: {}", e.getMessage(), e);
        }
    }
}
