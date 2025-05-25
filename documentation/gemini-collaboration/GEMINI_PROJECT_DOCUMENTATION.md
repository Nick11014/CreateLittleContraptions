# CreateLittleContraptions - Projeto Documentação para Gemini AI

## 🎯 OBJETIVO DO PROJETO
Criar um mod para Minecraft Forge/NeoForge que resolve a incompatibilidade entre os mods **Create** e **LittleTiles**. Especificamente, quando blocos LittleTiles são incluídos em contraptions (estruturas móveis) do Create, eles se tornam invisíveis durante o movimento.

## 📋 STATUS ATUAL
- ✅ **Log spam corrigido**: Rate limiting implementado com sucesso
- ✅ **Mod compila**: Build executado sem erros (31 tasks)
- ✅ **Integração detectada**: Ambos os mods são detectados corretamente
- ❌ **Problema principal**: LittleTiles blocos ainda permanecem invisíveis em contraptions
- ❌ **Rendering não funciona**: "Fixed 0 out of 1 contraptions" no log

## 🔧 ARQUITETURA DO PROJETO

### Estrutura Principal
```
CreateLittleContraptions/
├── src/main/java/com/createlittlecontraptions/
│   ├── CreateLittleContraptions.java          # Classe principal do mod
│   ├── mixins/
│   │   └── ContraptionRendererMixin.java      # Mixin que intercepta rendering
│   ├── compat/
│   │   ├── create/
│   │   │   └── CreateRuntimeIntegration.java  # Integração com Create mod
│   │   └── littletiles/
│   │       └── LittleTilesContraptionRenderer.java # Renderer customizado
│   ├── events/
│   │   └── DebugCommandHandler.java           # Comandos de debug
│   └── dev/
│       └── ContraptionDebugCommand.java       # Comando /contraption-debug
```

### Versões dos Mods
- **Minecraft**: 1.21.1
- **NeoForge**: 21.1.172
- **Create**: 6.0.4
- **LittleTiles**: 1.6.0-pre163
- **CreativeCore**: 2.13.5

## 🚨 PROBLEMA PRINCIPAL

### Sintomas
1. LittleTiles blocos são visíveis quando estáticos (mundo normal)
2. **CRÍTICO**: Quando Create contraption é montada (assembly), LittleTiles blocos desaparecem IMEDIATAMENTE
3. LittleTiles blocos permanecem invisíveis durante todo o período que a contraption existe (parada ou em movimento)
4. LittleTiles blocos só reaparecem quando a contraption é desmontada (disassembly)
5. A contraption é detectada: "Found 1 contraption" 
6. Mas o fix falha: "Fixed 0 out of 1 contraptions"

### Log Evidence (latest.log)
```
[25mai.2025 15:12:24.395] [CHAT] 🎉 Fixed 0 out of 1 contraptions
[25mai.2025 15:12:36.561] [CHAT] Create detected: true
[25mai.2025 15:12:36.561] [CHAT] LittleTiles detected: true
[25mai.2025 15:12:36.561] [CHAT] Integration active: true
```

## 💡 ABORDAGEM ATUAL

### 1. Mixin Implementation (ContraptionRendererMixin.java)
**Target**: `BlockRenderDispatcher.class`
**Method**: `renderSingleBlock`
**Objetivo**: Interceptar rendering de blocos individuais e aplicar renderer customizado para LittleTiles

```java
@Mixin(BlockRenderDispatcher.class)
public class ContraptionRendererMixin {
    @Inject(method = "renderSingleBlock", at = @At("HEAD"))
    private void onBlockRender(BlockState blockState, PoseStack poseStack, 
                              MultiBufferSource bufferSource, int light, 
                              int overlay, CallbackInfo ci) {
        // Custom LittleTiles rendering logic
    }
}
```

### 2. Create Integration (CreateRuntimeIntegration.java)
- Detecta contraptions no mundo
- Analisa blocos dentro das contraptions
- Tenta aplicar rendering customizado para blocos LittleTiles
- **PROBLEMA**: Não consegue efetivamente "fixar" o rendering

### 3. LittleTiles Renderer (LittleTilesContraptionRenderer.java)
- Placeholder implementation
- Deveria conter a lógica específica para renderizar LittleTiles em contraptions
- **PROBLEMA**: Implementação atual é apenas um placeholder

## 🔍 ANÁLISE TÉCNICA DO PROBLEMA

### Pipeline de Rendering do Create
1. Create contraptions usam sistema de rendering próprio
2. **CRÍTICO**: Durante assembly, Create "move" blocos do mundo normal para sistema de contraption
3. Blocos são renderizados através de `ContraptionMatrices` 
4. O sistema não consulta o `BlockRenderDispatcher` normal durante contraption rendering
5. **TEORIA**: Nosso mixin está interceptando o pipeline errado - precisamos interceptar o processo de assembly/contraption rendering

### Pipeline de Rendering do LittleTiles
1. LittleTiles usa rendering customizado complexo
2. Tiles são sub-blocos com propriedades específicas
3. Requer contexto especial para rendering correto
4. **TEORIA**: Precisamos integrar diretamente com o sistema de rendering do LittleTiles

## ❓ PERGUNTAS PARA GEMINI

### 1. Análise de Código Create
**Pergunta**: Analisando o código do Create mod (especificamente `ContraptionMatrices` e sistema de contraption rendering), qual é o ponto correto para interceptar o rendering de blocos dentro de contraptions?

**Contexto**: Nosso mixin atual targetiza `BlockRenderDispatcher.renderSingleBlock`, mas suspeitamos que contraptions usam um pipeline de rendering diferente.

### 2. Análise de Código LittleTiles
**Pergunta**: Analisando o código do LittleTiles, quais classes e métodos são responsáveis pelo rendering de tiles? Como podemos invocar o sistema de rendering do LittleTiles dentro do contexto de uma contraption?

**Contexto**: Precisamos implementar `LittleTilesContraptionRenderer.java` com lógica real.

### 3. Integração de Pipelines
**Pergunta**: Como integrar o sistema de rendering do LittleTiles com o sistema de contraption rendering do Create? Existe um padrão ou abordagem recomendada para este tipo de integração entre mods?

## 📁 ARQUIVOS CHAVE PARA ANÁLISE

### 1. Arquivo Principal com Problema
**Path**: `src/main/java/com/createlittlecontraptions/mixins/ContraptionRendererMixin.java`
**Status**: Implementado mas não funcional
**Linhas**: 191 linhas

### 2. Integração Create
**Path**: `src/main/java/com/createlittlecontraptions/compat/create/CreateRuntimeIntegration.java`
**Status**: Funcional para detecção, falha no fix
**Linhas**: ~900 linhas

### 3. Renderer LittleTiles
**Path**: `src/main/java/com/createlittlecontraptions/compat/littletiles/LittleTilesContraptionRenderer.java`
**Status**: Placeholder implementation
**Linhas**: 317 linhas

### 4. Logs de Debug
**Path**: `runs/client/logs/latest.log`
**Status**: Mostra integração funcionando mas rendering falhando
**Linhas**: 1189 linhas

## 🎯 OBJETIVO ESPECÍFICO PARA GEMINI

Precisamos que o Gemini analise:

1. **Create mod source code**: Identificar o pipeline correto de rendering para contraptions
2. **LittleTiles mod source code**: Identificar como invocar o sistema de rendering
3. **Propor solução**: Código específico para fazer a integração funcionar

### Resultado Esperado
- LittleTiles blocos permanecem visíveis quando a contraption está em movimento
- Log mostra: "Fixed 1 out of 1 contraptions" (ou similar)
- Performance não é impactada significativamente

## 📚 RECURSOS DISPONÍVEIS

### Documentação Existente
- `IMPLEMENTATION_COMPLETE.md`: Status de implementação
- `TESTING_GUIDE.md`: Como testar o mod
- `LOG_SPAM_FIX_SUMMARY.md`: Histórico de correções
- `docs/`: Análises detalhadas dos sistemas

### Comandos de Debug
- `/contraption-debug`: Analisa contraptions no mundo
- Logging detalhado no console

### Build System
- Gradle build funcional
- Ambiente de desenvolvimento configurado
- Mods de teste disponíveis

## 🚀 PRÓXIMOS PASSOS

1. **Gemini Analysis**: Analisar código fonte dos mods Create e LittleTiles
2. **Implementation**: Implementar solução baseada na análise
3. **Testing**: Testar com contraptions reais
4. **Validation**: Confirmar que LittleTiles permanecem visíveis em movimento

---

**Este documento serve como briefing completo para que Gemini AI possa entender o projeto e fornecer uma solução técnica específica para resolver o problema de rendering.**
