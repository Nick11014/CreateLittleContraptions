package com.createlittlecontraptions.mixins;

//import com.createlittlecontraptions.commands.ContraptionDisassemblyCommand;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para capturar métodos chamados durante disassembly de contraptions
 */
@Mixin(AbstractContraptionEntity.class)
public class ContraptionDisassemblyMixin {
      @Inject(method = "disassemble", at = @At("HEAD"), remap = false)
    public void onDisassembleStart(CallbackInfo ci) {
        //ContraptionDisassemblyCommand.logMethodCall("AbstractContraptionEntity", "disassemble() - START");
    }
    
    @Inject(method = "disassemble", at = @At("RETURN"), remap = false)
    public void onDisassembleEnd(CallbackInfo ci) {
        //ContraptionDisassemblyCommand.logMethodCall("AbstractContraptionEntity", "disassemble() - END");
    }
    
    @Inject(method = "remove", at = @At("HEAD"), remap = false)
    public void onRemoveStart(CallbackInfo ci) {
        //ContraptionDisassemblyCommand.logMethodCall("AbstractContraptionEntity", "remove() - START");
    }
      @Inject(method = "remove", at = @At("RETURN"), remap = false)
    public void onRemoveEnd(CallbackInfo ci) {
        //ContraptionDisassemblyCommand.logMethodCall("AbstractContraptionEntity", "remove() - END");
    }
    
    @Inject(method = "onRemovedFromLevel", at = @At("HEAD"), remap = false)
    public void onRemovedFromLevelStart(CallbackInfo ci) {
        //ContraptionDisassemblyCommand.logMethodCall("AbstractContraptionEntity", "onRemovedFromLevel() - START");
    }
    
    @Inject(method = "onRemovedFromLevel", at = @At("RETURN"), remap = false)
    public void onRemovedFromLevelEnd(CallbackInfo ci) {
        //ContraptionDisassemblyCommand.logMethodCall("AbstractContraptionEntity", "onRemovedFromLevel() - END");
    }
    
    @Inject(method = "tick", at = @At("HEAD"), remap = false)
    public void onTickStart(CallbackInfo ci) {
        // Só log durante disassembly para não spam
        if (isLoggingActive()) {
            //ContraptionDisassemblyCommand.logMethodCall("AbstractContraptionEntity", "tick() - called");
        }
    }
    
    private boolean isLoggingActive() {
        // Método simples para verificar se logging está ativo
        // Podemos implementar uma verificação mais sofisticada depois
        return true; // Por enquanto sempre ativo quando o Mixin é chamado
    }
}
