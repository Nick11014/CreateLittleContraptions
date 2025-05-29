# Projeto de Teste: Create & LittleTiles Contraption - Plano Técnico

*Objetivo Geral:* Implementar a identificação, o rastreamento do ciclo de vida e a renderização de blocos LittleTiles dentro de contraptions ativas do mod Create.

---

## Etapa 1: Identificar Contraptions Ativas no Mundo (via Comando de Chat) ✅ COMPLETO

*Objetivo:* Criar um comando de chat que liste as contraptions ativas do Create, seus tipos, posições e os blocos que as compõem.

* *Checklist de Tarefas Técnicas:*
    * \[x] *Criar Classe de Comando:*
        * \[x] Criar arquivo `ContraptionDebugCommand.java` (implementado como `/contraption-debug`).
        * \[x] Implementar a estrutura básica de um comando registrável no NeoForge.
    * \[x] *Registrar Comando:*
        * \[x] Adicionar lógica para registrar o comando durante a inicialização do mod.
    * \[x] **Implementar Lógica do Comando (execute):**
        * \[x] Obter `ServerLevel` a partir de `context.getSource().getLevel()`.
        * \[x] *Acessar Contraptions Ativas:* Detectar contraptions próximas ao jogador.
        * \[x] *Iterar e Coletar Dados:* Análise completa de blocos na contraption.
        * \[x] *Formatar e Enviar Resposta:* Output detalhado com identificação de LittleTiles.
    * \[x] *Testar Comando:* Validado em produção com elevator contraption (33 blocos, 2 LittleTiles detectados).

### Etapa 1.5: Análise Avançada de Contraptions (via Java Reflection) ✅ COMPLETO

*Objetivo:* Expandir comando com análise detalhada de classes, métodos e hierarquia usando reflection.

* *Checklist de Tarefas Técnicas:*
    * \[x] **Implementar Subcomando `classes`:**
        * \[x] Adicionar `/contraption-debug classes` ao comando existente.
        * \[x] Implementar análise via Java Reflection de todas as classes envolvidas.
    * \[x] **Análise Detalhada de Classes:**
        * \[x] Análise de `ControlledContraptionEntity` (23 métodos identificados).
        * \[x] Análise de contraption interna (`ElevatorContraption`, 15 métodos).
        * \[x] Análise de classes de blocos (`BlockTile` com 78 métodos, hierarquia completa).
        * \[x] Detecção de herança e interfaces implementadas.
    * \[x] **GameTests de Validação:**
        * \[x] Teste `contraptionDebugClassesRobustnessTest()` implementado e funcionando.
        * \[x] Validação de execução sem exceções.
    * \[x] **Validação Manual:** Testado em produção, ambos comandos funcionando perfeitamente.

---

## Etapa 2: Identificar Assembly e Disassembly (via Comando de Chat e Notificações) ✅ COMPLETO

*Objetivo:* Criar um comando para alternar o logging de eventos de assembly/disassembly e exibir notificações no chat quando esses eventos ocorrerem.

* *Checklist de Tarefas Técnicas:*
    * \[x] *Criar Variável de Estado Global:*
        * \[x] Definir `public static boolean enableContraptionEventLogging = false;` em `ContraptionEventHandler.java`.
    * \[x] *Criar Classe de Comando de Alternância:*
        * \[x] Criar arquivo `ContraptionEventsCommand.java`.
        * \[x] Implementar e registrar o comando `/contraption-events`.
        * \[x] No método execute: Toggle de logging com feedback colorido (§a ENABLED, §c DISABLED).
    * \[x] *Criar Classe de Handler de Eventos:*
        * \[x] Criar arquivo `ContraptionEventHandler.java`.
        * \[x] Registrar esta classe no event bus do NeoForge.
    * \[x] **Implementar Listener de Assembly Events:**
        * \[x] Método `onContraptionAssembled()` com detecção via `EntityJoinLevelEvent`.
        * \[x] Análise automática de LittleTiles usando reflection.
        * \[x] Notificações para jogadores num raio de 64 blocos.
        * \[x] Logs estruturados com informações detalhadas.
    * \[x] **Implementar Listener de Disassembly Events:**
        * \[x] Método `onContraptionDisassembled()` com detecção via `EntityLeaveLevelEvent`.
        * \[x] Notificações de desmontagem no chat.
    * \[x] *Testar no Jogo:* Validado em produção - assembly e disassembly detectados corretamente.

---

## Etapa 2.5: Investigação Detalhada dos Métodos de Renderização (LittleTiles vs Blocos Comuns)

*Objetivo:* Investigar e documentar as diferenças entre renderização de blocos comuns e LittleTiles em contraptions estáticas, identificando métodos que podem estar impedindo a renderização correta.

*Contexto:* Todos os testes serão realizados com elevator contraption **parado** para isolar problemas de renderização básica, sem complicações de movimento.

* *Checklist de Tarefas Técnicas:*
    * \[ ] **Expandir Comando `/contraption-debug` com Análise de Renderização:**
        * \[ ] Adicionar subcomando `rendering` ao `ContraptionDebugCommand.java`.
        * \[ ] Implementar método `analyzeRenderingMethods()` que:
            * \[ ] Para cada bloco LittleTiles na contraption:
                * \[ ] Testar `VoxelShape getBlockSupportShape(BlockState, BlockGetter, BlockPos)` e documentar resultado.
                * \[ ] Testar `boolean supportsExternalFaceHiding(BlockState)` e comparar com blocos comuns.
                * \[ ] Testar `boolean hasDynamicLightEmission(BlockState)` e verificar diferenças.
                * \[ ] Testar `boolean useShapeForLightOcclusion(BlockState)` e analisar impacto.
                * \[ ] **CRÍTICO:** Testar `BlockState getStateAtViewpoint(BlockState, BlockGetter, BlockPos, Vec3)` com diferentes viewpoints.
                * \[ ] Testar `boolean propagatesSkylightDown(BlockState, BlockGetter, BlockPos)` e verificar propagação.
            * \[ ] Para contraption entity:
                * \[ ] Analisar `Map<BlockPos, StructureBlockInfo> getBlocks()` e verificar integridade dos dados LittleTiles.
                * \[ ] Investigar transformações de posição via `Vec3 applyRotation(Vec3, float)` (mesmo parado).
                * \[ ] Verificar `float getAngle(float)` para contraption estática.
        * \[ ] Formatar saída com comparação lado-a-lado: bloco comum vs LittleTiles.
    * \[ ] **Implementar GameTests para Comparação Automatizada:**
        * \[ ] Criar `RenderingComparisonGameTest.java`.
        * \[ ] Implementar teste `compareBlockRenderingBehavior()`:
            * \[ ] Criar contraption com bloco comum (ex: stone).
            * \[ ] Criar contraption com LittleTiles.
            * \[ ] Executar análise de renderização em ambas.
            * \[ ] Comparar resultados automaticamente.
            * \[ ] Documentar diferenças encontradas.
        * \[ ] Implementar teste `validateLittleTilesDataIntegrity()`:
            * \[ ] Verificar se `StructureBlockInfo` preserva dados específicos do LittleTiles.
            * \[ ] Validar presença de `BlockEntity` data.
            * \[ ] Confirmar integridade de `CompoundTag` do LittleTiles.
    * \[ ] **Execução de Testes Manuais Focados:**
        * \[ ] Criar mundo de teste com elevator contraption **parado** contendo:
            * \[ ] 1 bloco comum (stone/wood) para referência.
            * \[ ] 1 bloco LittleTiles simples.
            * \[ ] 1 bloco LittleTiles complexo (múltiplas tiles).
        * \[ ] Executar `/contraption-debug rendering` e documentar output completo.
        * \[ ] Fazer screenshots comparativos de renderização visual.
        * \[ ] Testar em diferentes condições de iluminação (dia/noite/subterrâneo).
    * \[ ] **Validação das 5 Hipóteses Principais:**
        * \[ ] **Hipótese 1 - Problemas de VoxelShape:**
            * \[ ] Comparar `VoxelShape` retornado por LittleTiles vs bloco comum.
            * \[ ] Verificar se shapes complexos são preservados na contraption.
        * \[ ] **Hipótese 2 - Conflitos de Iluminação:**
            * \[ ] Analisar `hasDynamicLightEmission()` e `useShapeForLightOcclusion()`.
            * \[ ] Testar renderização com diferentes níveis de luz.
        * \[ ] **Hipótese 3 - Problemas de Viewpoint (CRÍTICA):**
            * \[ ] Testar `getStateAtViewpoint()` com viewpoints de diferentes ângulos.
            * \[ ] Verificar se viewpoint é calculado corretamente para blocos na contraption.
        * \[ ] **Hipótese 4 - Perda de BlockEntity:**
            * \[ ] Verificar presença de `BlockEntity` data em `StructureBlockInfo`.
            * \[ ] Confirmar se `createBlockEntity()` é chamado adequadamente.
        * \[ ] **Hipótese 5 - Problemas de Assembly:**
            * \[ ] Analisar `StructureBlockInfo` antes e depois do assembly.
            * \[ ] Verificar integridade de `CompoundTag` específico do LittleTiles.
    * \[ ] **Documentação e Análise de Resultados:**
        * \[ ] Atualizar `docs/contraption-analysis/rendering-methods-research.md` com dados coletados.
        * \[ ] Criar tabela comparativa completa: bloco comum vs LittleTiles.
        * \[ ] Identificar **métodos específicos** que retornam valores problemáticos.
        * \[ ] Documentar **pontos exatos** onde renderização falha.
        * \[ ] Formular **estratégia de correção** baseada nos achados.
    * \[ ] **Preparação para Etapa 3:**
        * \[ ] Listar métodos que precisam ser interceptados/modificados.
        * \[ ] Identificar transformações de coordenadas necessárias.
        * \[ ] Documentar dados específicos do LittleTiles que devem ser preservados.
        * \[ ] Criar plano de implementação de Mixins/hooks necessários.

---

## Etapa 3: Realizar Render e Unrender de Blocos Específicos da Contraption (LittleTiles)

*Objetivo Técnico:* Renderizar blocos littletiles:tiles dentro de uma contraption em movimento, usando uma instância virtual de BETiles e seu BERenderManager, e garantir que a renderização cesse na desmontagem.

* *Checklist de Tarefas Técnicas:*
    * \[ ] *Setup Inicial (Já deve estar parcialmente feito):*
        * \[ ] Confirmar que LittleTilesMovementBehaviour está registrado para littletiles:tiles.
        * \[ ] Confirmar que LittleTilesMovementBehaviour.renderInContraption chama LittleTilesContraptionRenderer.renderMovementBehaviourTile.
    * \[ ] **No LittleTilesContraptionRenderer.renderMovementBehaviourTile:**
        * \[ ] *Obter Parâmetros Essenciais:*
            * \[ ] MovementContext context
            * \[ ] VirtualRenderWorld renderWorld
            * \[ ] ContraptionMatrices matrices
            * \[ ] MultiBufferSource buffer
            * \[ ] float partialTicks (passado como parâmetro a partir do MovementBehaviour)
        * \[ ] **Obter HolderLookup.Provider:**
            * \[ ] HolderLookup.Provider provider = Minecraft.getInstance().level.registryAccess(); (ou a lógica de fallback aprimorada).
        * \[ ] **Inicializar Instância Virtual de BETiles (Via Reflexão no LittleTilesAPIFacade ou diretamente no Renderer):**
            * \[ ] Chamar construtor de team.creative.littletiles.common.block.entity.BETiles(BlockPos, BlockState) usando context.localPos e context.state.
            * \[ ] Chamar virtualBETiles.setLevel(renderWorld).
                * *Verificação Crítica:* Assegurar que renderWorld.isClientSide() seja true para que virtualBETiles.initClient() (chamado por setLevel) crie o BERenderManager.
            * \[ ] Chamar virtualBETiles.loadAdditional(context.blockEntityData, provider).
        * \[ ] **Acessar BERenderManager:**
            * \[ ] Via reflexão, obter o campo render (tipo BERenderManager) da virtualBETilesInstance.
            * \[ ] Se BERenderManager for null, logar erro crítico e abortar renderização para este bloco.
        * \[ ] **Preparar RenderingBlockContext:**
            * \[ ] Instanciar team.creative.littletiles.client.render.cache.build.RenderingBlockContext via reflexão.
                * Parâmetros: renderWorld, context.localPos (posição da BE na contraption), context.state, renderWorld.getRandom(), ModelData.EMPTY, RenderType[] typesToRender (ex: RenderType.chunkLayers()).
        * \[ ] *Obter Coleção de `LittleRenderBox`es:*
            * \[ ] Chamar BERenderManager.getRenderingBoxes(RenderingBlockContext) via reflexão.
            * \[ ] O resultado será Int2ObjectMap<ChunkLayerMapList<LittleRenderBox>>.
        * \[ ] *Loop de Renderização Principal:*
            * \[ ] Para cada renderTypeKey (int) e ChunkLayerMapList<LittleRenderBox> layerBoxes no mapa retornado:
                * \[ ] Obter o RenderType correspondente ao renderTypeKey (pode ser necessário mapear ou usar os typesToRender do RenderingBlockContext).
                * \[ ] Obter VertexConsumer vc = buffer.getBuffer(actualRenderType);.
                * \[ ] Iterar sobre cada LittleRenderBox renderBox em layerBoxes (pode ser necessário entender como iterar ChunkLayerMapList).
                    * \[ ] **Configurar PoseStack para o LittleRenderBox Individual:**
                        * \[ ] poseStack.pushPose(); (começando do PoseStack já transladado para context.localPos).
                        * \[ ] Obter as coordenadas minX, minY, minZ do renderBox (que são relativas ao BETiles).
                        * \[ ] poseStack.translate(renderBox.minX, renderBox.minY, renderBox.minZ); (ou o centro do box, dependendo de como RenderBox.getBakedQuad espera as coordenadas).
                        * \[ ] *Nota:* RenderBox.getBakedQuad pode já lidar com o offset do box, verifique sua implementação. O QuadGeneratorContext é crucial aqui.
                    * \[ ] **Obter BlockState e BakedModel do renderBox:**
                        * \[ ] BlockState boxState = renderBox.state;
                        * \[ ] int boxColor = renderBox.color;
                        * \[ ] BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(boxState);
                    * \[ ] *Obter Luz (Placeholder ou Avançado):*
                        * \[ ] Iniciar com int packedLight = LevelRenderer.getLightColor((BlockAndTintGetter)renderWorld, context.localPos);. Investigar se a luz deve ser obtida para a posição exata do LittleRenderBox se a escala for grande.
                    * \[ ] *Gerar e Bufferizar Quads:*
                        * \[ ] Criar/reutilizar um QuadGeneratorContext.
                        * \[ ] Para cada Facing (UP, DOWN, etc.):
                            * \[ ] Se renderBox.shouldRenderFace(facing):
                                * \[ ] Chamar List<BakedQuad> quads = renderBox.getBakedQuad(contextoDoGeradorDeQuad, renderWorld, context.localPos, BlockPos.ZERO (ou offset apropriado), boxState, model, ModelData.EMPTY, facing, actualRenderType, renderWorld.getRandom(), boxColor != -1, boxColor);
                                * \[ ] Para cada BakedQuad bq em quads:
                                    * \[ ] Ajustar r, g, b, a com base no boxColor se não for -1 e se bq.isTinted().
                                    * \[ ] vc.putBulkData(poseStack.last(), bq, r, g, b, a, packedLight, OverlayTexture.NO_OVERLAY, true);
                    * \[ ] poseStack.popPose(); (para o LittleRenderBox individual).
            * \[ ] poseStack.popPose(); (para o BETiles inteiro, correspondendo ao pushPose no início da renderização do BETiles).
    * \[ ] *Lógica de "Unrender":*
        * \[ ] Se você está recriando a instância virtual de BETiles a cada frame em renderMovementBehaviourTile, não há "unrender" explícito necessário além da contraption deixar de chamar o método de render.
        * \[ ] Se você armazenar o virtualBETilesInstance ou seu BERenderManager em algum lugar (ex: em um mapa associado à contraption), limpe essa referência quando o evento de desmontagem da Etapa 2 for recebido.
    * \[ ] *Testes Finais:*
        * \[ ] Verificar se os LittleTiles são renderizados corretamente, com texturas e formas.
        * \[ ] Testar a iluminação (mesmo que inicialmente com FULL_BRIGHT e depois com a tentativa de LevelRenderer.getLightColor).
        * \[ ] Testar a suavidade do movimento com partialTicks.
        * \[ ] Testar a montagem e desmontagem, garantindo que os blocos apareçam e desapareçam corretamente.
        * \[ ] Monitorar logs para quaisquer erros.

Este plano mais técnico deve te dar um roteiro claro para cada etapa. Boa codificação!