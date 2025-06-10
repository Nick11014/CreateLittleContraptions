# Timeline do Projeto: Compatibilidade Visual Create-LittleTiles

## Fase 1: Limpeza e Refatoração

- **AÇÃO:** Início do projeto de refatoração para implementação do Model Baking.
- **MOTIVO:** Implementar sistema de "Model Baking" para permitir que blocos do Little Tiles sejam visualmente representados em contraptions do Create através de modelos estáticos.

- **AÇÃO:** Deletado o arquivo `rendering/ContraptionBlockRenderController.java`.
- **MOTIVO:** Esta classe fazia parte de uma abordagem de controle de renderização em tempo real que está sendo descontinuada em favor da solução de "Model Baking".

- **AÇÃO:** Deletado o arquivo `mixins/ContraptionEntityRendererFilterMixin.java`.
- **MOTIVO:** Mixin obsoleto da abordagem anterior que interceptava renderização em tempo real.

- **AÇÃO:** Deletado o arquivo `mixins/ContraptionEntityRendererMixin.java`.
- **MOTIVO:** Mixin obsoleto da abordagem anterior que interceptava renderização em tempo real.

- **AÇÃO:** Deletado o arquivo `mixins/ContraptionRendererMixin.java`.
- **MOTIVO:** Mixin obsoleto da abordagem anterior que interceptava renderização em tempo real.

- **AÇÃO:** Deletado o arquivo `mixins/ContraptionDisassemblyMixin.java`.
- **MOTIVO:** Mixin obsoleto da abordagem anterior de detecção de desmontagem.

- **AÇÃO:** Deletado o arquivo `mixins/create/AllMovementBehavioursMixin.java`.
- **MOTIVO:** Mixin obsoleto que manipulava comportamentos de movimento, substituído pela nova arquitetura de Model Baking.

- **AÇÃO:** Modificado `CreateLittleContraptions.java` - removido método `registerLittleTilesMovementBehaviour()`.
- **MOTIVO:** A lógica de registro de MovementBehaviour foi completamente removida da classe principal em favor da abordagem Model Baking.

- **AÇÃO:** Completamente reescrito `events/ContraptionEventHandler.java`.
- **MOTIVO:** Removido métodos obsoletos `analyzeLittleTilesInContraption` e `notifyNearbyPlayers`. Mantidos apenas os listeners essenciais para assembly/disassembly, prontos para implementação do Model Baking.

## Fase 2: Implementação Inicial do Sistema de Baking

- **AÇÃO:** Criado `rendering/cache/ContraptionModelCache.java`.
- **MOTIVO:** Implementação do cache estático para armazenar modelos "assados" por UUID de contraption e posição de bloco.

- **AÇÃO:** Criado `rendering/baking/LittleTilesModelBaker.java`.
- **MOTIVO:** Implementação inicial do "baker" que captura geometria de BlockEntityRenderer do LittleTiles e a converte em BakedModel. Versão inicial com placeholder model e VertexConsumer customizado.

- **AÇÃO:** Tentativa de build para verificar se a limpeza não introduziu erros de compilação.
- **RESULTADO:** Build falhou devido à incompatibilidade de versão do Java. O código parece estar correto, mas será necessário ajustar a configuração do Java para prosseguir com os testes.

## Documentação e Referências Técnicas

- **AÇÃO:** Criado o arquivo `IMPLEMENTATION_GUIDE.md` na raiz do projeto.
- **MOTIVO:** Este documento contém o plano técnico completo para a implementação do sistema de "Model Baking", incluindo: conceitos, estrutura de classes, passos de implementação detalhados, e uma referência técnica específica sobre `BakedModel` e `BakedQuad`. Serve como documentação técnica essencial para guiar a implementação das próximas fases do projeto.
- **CONTEÚDO:** O guia inclui explicações sobre criação programática de modelos, captura de geometria com VertexConsumer customizado, e integração com o pipeline de renderização do Create.

## Resolução de Problemas de Build

- **PROBLEMA:** Build falhando com erro "To use the NeoForge plugin, please run Gradle with Java 17 or newer. You are currently running on Java 1 (1.8)."
- **SOLUÇÃO:** Instalado JDK 24 em `C:\Program Files\Java\jdk-24` e configurado `gradle.properties` para usar esta versão.
- **AÇÃO:** Modificado `gradle.properties` para incluir `org.gradle.java.home=C:\\ Program Files\\Java\\jdk-24` e flag `--enable-native-access=ALL-UNNAMED`.

- **PROBLEMA:** Arquivo `ContraptionEventHandler.java` estava vazio após edição manual.
- **SOLUÇÃO:** Restaurado o conteúdo básico da classe com listeners para assembly/disassembly e TODOs para implementação futura do baking.

- **PROBLEMA:** Classe `SimpleBakedModel` faltando implementação do método `getOverrides()`.
- **SOLUÇÃO:** Adicionado método `getOverrides()` retornando `ItemOverrides.EMPTY` e import necessário.

- **RESULTADO:** ✅ **BUILD SUCCESSFUL** - O projeto agora compila corretamente com Java 24 e todas as classes estão sintaticamente corretas.

## Fase 3: Refatoração da Abordagem de Model Baking

- **PROBLEMA:** Erros de compilação no `LittleTilesModelBaker.java` devido a mudanças na API do VertexConsumer.
- **ANÁLISE:** Descoberto que a abordagem original de capturar geometria via VertexConsumer não é a forma correta de implementar BakedModels segundo a documentação do NeoForge.
- **SOLUÇÃO:** Refatoração completa do `LittleTilesModelBaker.java` baseada na documentação oficial do NeoForge sobre BakedModels.

### Mudanças Implementadas:

- **AÇÃO:** Removida completamente a implementação de VertexConsumer customizado.
- **MOTIVO:** A interface VertexConsumer mudou entre versões do Minecraft/NeoForge, causando erros de compilação. Além disso, a documentação do NeoForge indica que BakedModels devem funcionar através do método `getQuads()` que retorna `BakedQuad`s diretamente.

- **AÇÃO:** Implementada abordagem correta baseada em `BakedQuad`s diretos.
- **MOTIVO:** BakedModels funcionam retornando listas de `BakedQuad`s através do método `getQuads()`, que são processados diretamente pelo sistema de renderização do Minecraft.

- **AÇÃO:** Criado sistema de placeholder com geometria de cubo simples.
- **MOTIVO:** Implementação inicial funcional que pode ser expandida no futuro para extrair geometria real do LittleTiles.

- **AÇÃO:** Implementada classe `LittleTilesBakedModel` que extende `BakedModel`.
- **MOTIVO:** Implementação completa de todos os métodos necessários do BakedModel, incluindo métodos específicos do NeoForge como `getRenderTypes()` e `getModelData()`.

- **AÇÃO:** Corrigido erro de sintaxe (chave extra no final do arquivo).
- **MOTIVO:** Erro de sintaxe que impedia compilação após a refatoração.

### Técnicas Utilizadas:

- **Vertex Format:** Uso do `DefaultVertexFormat.BLOCK` para criar dados de vértice corretos.
- **Quad Generation:** Criação programática de `BakedQuad`s com dados de posição, cor, textura e normais.
- **Face Organization:** Separação de quads por faces (Direction) para renderização eficiente.
- **Texture Mapping:** Uso de sprites de textura do atlas do Minecraft.

- **RESULTADO:** ✅ **BUILD SUCCESSFUL** - O `LittleTilesModelBaker.java` agora compila corretamente e segue as práticas recomendadas do NeoForge.
- **WARNING:** Apenas um warning sobre mapeamento de obfuscação no `ContraptionRenderInfoMixin.java`, que é normal em desenvolvimento.

### Próximos Passos Identificados:

1. **Integração com LittleTiles:** Expandir o sistema para acessar a estrutura interna do LittleTiles e extrair geometria real.
2. **Otimização de Cache:** Implementar cache inteligente de modelos para melhor performance.
3. **Testes Funcionais:** Testar o sistema em contraptions reais do Create.
