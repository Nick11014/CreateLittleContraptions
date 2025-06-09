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
- **AÇÃO:** Modificado `gradle.properties` para incluir `org.gradle.java.home=C:\\Program Files\\Java\\jdk-24` e flag `--enable-native-access=ALL-UNNAMED`.

- **PROBLEMA:** Arquivo `ContraptionEventHandler.java` estava vazio após edição manual.
- **SOLUÇÃO:** Restaurado o conteúdo básico da classe com listeners para assembly/disassembly e TODOs para implementação futura do baking.

- **PROBLEMA:** Classe `SimpleBakedModel` faltando implementação do método `getOverrides()`.
- **SOLUÇÃO:** Adicionado método `getOverrides()` retornando `ItemOverrides.EMPTY` e import necessário.

- **RESULTADO:** ✅ **BUILD SUCCESSFUL** - O projeto agora compila corretamente com Java 24 e todas as classes estão sintaticamente corretas.
