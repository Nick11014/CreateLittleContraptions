package com.createlittlecontraptions.mixins.create;

import com.createlittlecontraptions.duck.IContraptionBakedModelCache;
import com.createlittlecontraptions.rendering.ClientCacheController;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.render.ContraptionRenderInfo;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

/**
 * Mixin crucial para ContraptionRenderInfo.
 * Este é o ponto-chave que implementa a estratégia de "Model Swapping".
 * Intercepta o momento em que o Create pede um modelo de bloco e substitui
 * por nosso modelo "assado" se existir no cache.
 */
@Mixin(value = ContraptionRenderInfo.class, remap = false)
public abstract class ContraptionRenderInfoMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("CreateLittleContraptions.ContraptionRenderInfoMixin");
    
    static {
        LOGGER.info("CLCLC: ContraptionRenderInfoMixin static initializer called - Mixin loaded");
    }
    
    @Shadow @Final
    private Contraption contraption;
    
    @Unique
    private boolean clclc$cachePopulated = false;    
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstructed(CallbackInfo ci) {
        LOGGER.info("CLCLC: ContraptionRenderInfo instance created with contraption: {}", contraption != null ? "present" : "null");
    }
    
    /**
     * O hook principal que implementa a estratégia de Model Swapping.
     * Este método intercepta dispatcher.getBlockModel(state) e substitui pelo nosso modelo "assado" se disponível.
     */
    @Redirect(
        method = "buildStructureBuffer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;getBlockModel(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/BakedModel;"
        ),
        require = 0  // Make this optional in case the method signature changes
    )
    private BakedModel onGetBlockModel(BlockRenderDispatcher dispatcher, BlockState state) {
        try {
            // Primeiro, certifica-se de que o cache foi populado
            if (!clclc$cachePopulated) {
                LOGGER.info("CLCLC: Cache not populated yet, calling ClientCacheController.populateCacheFor");
                ClientCacheController.populateCacheFor(this.contraption);
                clclc$cachePopulated = true;
            }
            
            // Acessa o cache através da Duck Interface
            IContraptionBakedModelCache duck = (IContraptionBakedModelCache) this.contraption;
            Optional<Map<BlockPos, BakedModel>> cacheOpt = duck.getModelCache();
            
            if (cacheOpt.isPresent()) {
                Map<BlockPos, BakedModel> cache = cacheOpt.get();
                
                // TODO: Precisaríamos da BlockPos atual para fazer a lookup correta
                // Por enquanto, verificamos se é um bloco LittleTiles
                String blockName = state.getBlock().getClass().getName().toLowerCase();
                if (blockName.contains("littletiles")) {
                    LOGGER.info("CLCLC: *** LITTLETILES BLOCK DETECTED - LOOKING FOR BAKED MODEL ***");
                    
                    // Como não temos a BlockPos exata aqui, vamos tentar encontrar qualquer modelo Little Tiles no cache
                    for (Map.Entry<BlockPos, BakedModel> entry : cache.entrySet()) {
                        BakedModel cachedModel = entry.getValue();
                        if (cachedModel != null && !cachedModel.getClass().getSimpleName().equals("PlaceholderBakedModel")) {
                            LOGGER.info("CLCLC: *** USING BAKED MODEL FROM CACHE FOR LITTLETILES BLOCK ***");
                            return cachedModel; // Retorna nosso modelo "assado"
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("CLCLC: Error in Model Swapping hook: {}", e.getMessage(), e);
        }
        
        // Se não houver modelo no cache, chama o método original
        return dispatcher.getBlockModel(state);
    }
}
