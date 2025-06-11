package com.createlittlecontraptions.mixins.create;

import com.createlittlecontraptions.duck.IContraptionBakedModelCache;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.render.ContraptionRenderInfo;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

/**
 * Mixin para ContraptionRenderInfo.
 * Intercepta a busca de modelos de blocos durante a construção do buffer de renderização
 * para o renderizador legado (sem Flywheel).
 * Se um modelo "assado" (baked) para um bloco Little Tiles existir em nosso cache,
 * ele é injetado no lugar do modelo original.
 */
@Mixin(value = ContraptionRenderInfo.class, remap = false)
public abstract class ContraptionRenderInfoMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("CreateLittleContraptions.ContraptionRenderInfoMixin");
    
    static {
        LOGGER.info("CLCLC: ContraptionRenderInfoMixin static initializer called - Mixin loaded");
    }
    
    // Usa @Shadow para obter acesso ao campo 'contraption' da classe original.
    @Shadow @Final
    private Contraption contraption;
    
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstructed(CallbackInfo ci) {
        LOGGER.info("CLCLC: ContraptionRenderInfo instance created with contraption: {}", contraption != null ? "present" : "null");
    }    // DISABLED - Method doesn't exist in current Create version
    /*
    @Redirect(
        method = "Lcom/simibubi/create/content/contraptions/render/ContraptionRenderInfo;buildStructureBuffer(Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;getBlockModel(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/BakedModel;"
        ),
        require = 0  // Make this optional in case the method doesn't exist
    )    private BakedModel onGetBlockModel(BlockRenderDispatcher dispatcher, BlockState state) {
        LOGGER.info("CLCLC: ContraptionRenderInfoMixin.onGetBlockModel called for block: {}", state.getBlock().getClass().getSimpleName());
        
        try {
            // Faz o cast da contraption para a nossa "Duck Interface" para acessar o cache.
            IContraptionBakedModelCache duck = (IContraptionBakedModelCache) this.contraption;

            // Tenta obter o cache de modelos da contraption.
            Optional<Map<BlockPos, BakedModel>> cacheOpt = duck.getModelCache();

            if (cacheOpt.isPresent()) {
                LOGGER.info("CLCLC: Model cache is present with {} entries", cacheOpt.get().size());
                // TODO: We would need the BlockPos here to properly check the cache
                // For now, just check if this might be a LittleTiles block
            }

            // Check if this might be a LittleTiles block by checking block name
            String blockName = state.getBlock().getClass().getName().toLowerCase();
            if (blockName.contains("littletiles")) {
                LOGGER.info("CLCLC: LittleTiles block detected: {}", blockName);
            }

        } catch (Exception e) {
            LOGGER.info("CLCLC: Error in ContraptionRenderInfoMixin: {}", e.getMessage());
        }

        // Se não houver modelo no cache para esta posição, chama o método original e prossegue normalmente.
        return dispatcher.getBlockModel(state);
    }
    */
}
