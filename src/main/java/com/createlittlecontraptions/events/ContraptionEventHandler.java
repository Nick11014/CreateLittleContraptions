package com.createlittlecontraptions.events;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.createlittlecontraptions.CreateLittleContraptions;

@EventBusSubscriber(modid = CreateLittleContraptions.MODID)
public class ContraptionEventHandler {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // Detecta quando uma contraption é montada (entidade criada)
        if (event.getEntity() instanceof AbstractContraptionEntity contraptionEntity) {
            CreateLittleContraptions.LOGGER.info("Contraption assembled: {}", contraptionEntity.getUUID());
            
            // TODO: Implementar baking de modelos aqui
            // 1. Obter a contraption: contraptionEntity.getContraption()
            // 2. Iterar sobre os BlockEntities: contraption.getRenderedBEs()
            // 3. Para cada BE do LittleTiles, chamar LittleTilesModelBaker.bake()
            // 4. Armazenar no cache: ContraptionModelCache.cacheModel()
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        // Detecta quando uma contraption é desmontada (entidade removida)
        if (event.getEntity() instanceof AbstractContraptionEntity contraptionEntity) {
            CreateLittleContraptions.LOGGER.info("Contraption disassembled: {}", contraptionEntity.getUUID());
            
            // TODO: Limpar cache de modelos
            // ContraptionModelCache.clearCache(contraptionEntity.getUUID());
        }
    }
}
