# Projeto de Teste: Create & LittleTiles Contraption - Plano Técnico

*Objetivo Geral:* Implementar a identificação, o rastreamento do ciclo de vida e a renderização de blocos LittleTiles dentro de contraptions ativas do mod Create.

---

## Etapa 1: Identificar Contraptions Ativas no Mundo (via Comando de Chat)

*Objetivo:* Criar um comando de chat que liste as contraptions ativas do Create, seus tipos, posições e os blocos que as compõem.

* *Checklist de Tarefas Técnicas:*
    * \[ ] *Criar Classe de Comando:*
        * \[ ] Criar arquivo InspectContraptionsCommand.java.
        * \[ ] Implementar a estrutura básica de um comando registrável no NeoForge/Forge.
    * \[ ] *Registrar Comando:*
        * \[ ] Adicionar lógica para registrar o comando /inspectcontraptions durante a inicialização do mod.
    * \[ ] **Implementar Lógica do Comando (execute):**
        * \[ ] No método execute(CommandContext<CommandSourceStack> context):
            * \[ ] Obter ServerLevel a partir de context.getSource().getLevel().
            * \[ ] *Acessar Contraptions Ativas:*
                * Utilizar a API do Create para obter a lista de contraptions ativas no ServerLevel (ex: através de um ContraptionManager ou WorldStorage).
            * \[ ] *Iterar e Coletar Dados:*
                * Para cada IContraption ativa:
                    * Obter e armazenar o tipo da contraption (se disponível).
                    * Obter e armazenar a BlockPos de âncora/origem da contraption no mundo.
                    * Obter a coleção de Structure.StructureBlockInfo (ou tipo equivalente) da contraption (ex: contraption.getBlocks().values()).
                    * Para cada StructureBlockInfo:
                        * Armazenar info.state (o BlockState).
                        * Armazenar info.pos (a BlockPos local dentro da contraption).
            * \[ ] *Formatar e Enviar Resposta:*
                * Construir uma MutableComponent ou uma lista de componentes para a saída.
                * Para cada contraption:
                    * Adicionar linha: "Contraption ID/Tipo: [ID/Tipo], Posição Mundo: [X, Y, Z]".
                    * Para cada bloco na contraption:
                        * Adicionar linha recuada: "  - Bloco: [BlockState.toString()], Posição Local: [LX, LY, LZ]".
                * Enviar a(s) componente(s) para context.getSource().sendSuccess(...).
    * \[ ] *Testar Comando:*
        * \[ ] Compilar e executar o Minecraft com o mod.
        * \[ ] Criar algumas contraptions do Create no mundo.
        * \[ ] Executar /inspectcontraptions e verificar se a saída no chat está correta e completa.

---

## Etapa 2: Identificar Assembly e Disassembly (via Comando de Chat e Notificações)

*Objetivo:* Criar um comando para alternar o logging de eventos de assembly/disassembly e exibir notificações no chat quando esses eventos ocorrerem.

* *Checklist de Tarefas Técnicas:*
    * \[ ] *Criar Variável de Estado Global:*
        * \[ ] Definir public static boolean enableContraptionEventLogging = false; em uma classe apropriada (ex: sua classe principal do mod ou uma classe de gerenciamento).
    * \[ ] *Criar Classe de Comando de Alternância:*
        * \[ ] Criar arquivo ToggleContraptionLogCommand.java.
        * \[ ] Implementar e registrar o comando /togglecontraptionlog.
        * \[ ] No método execute:
            * \[ ] Inverter o valor de enableContraptionEventLogging.
            * \[ ] Enviar mensagem de feedback ao jogador: "Log de eventos de contraption: ATIVADO/DESATIVADO".
    * \[ ] *Criar Classe de Handler de Eventos:*
        * \[ ] Criar arquivo CreateContraptionEventHandler.java.
        * \[ ] Registrar esta classe no event bus do NeoForge/Forge (ex: NeoForge.EVENT_BUS.register(new CreateContraptionEventHandler())).
    * \[ ] **Implementar Listener de ContraptionEvents.ContraptionAssemblyEvent:**
        * \[ ] No CreateContraptionEventHandler.java, criar um método público com a anotação @SubscribeEvent que recebe com.simibubi.create.api.event.たとえば.ContraptionEvents.ContraptionAssemblyEvent como parâmetro.
        * \[ ] Dentro do método:
            * \[ ] Verificar if (!enableContraptionEventLogging) return;.
            * \[ ] Obter IContraption contraption = event.getContraption();.
            * \[ ] Obter Level world = contraption.getContraptionWorld(); (ou o nível onde a contraption foi montada, se o evento fornecer).
            * \[ ] Obter a BlockPos anchorPos = contraption.getAnchor(); (ou a posição relevante).
            * \[ ] Enviar mensagem para o chat (ex: para o jogador que está próximo ou para todos, usando Player.sendSystemMessage ou ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastSystemMessage): "Contraption [ID/Tipo] montada em [X,Y,Z]".
    * \[ ] **Implementar Listener de ContraptionEvents.ContraptionDisassemblyEvent:**
        * \[ ] No CreateContraptionEventHandler.java, criar um método público com a anotação @SubscribeEvent que recebe com.simibubi.create.api.event.たとえば.ContraptionEvents.ContraptionDisassemblyEvent como parâmetro.
        * \[ ] Dentro do método:
            * \[ ] Verificar if (!enableContraptionEventLogging) return;.
            * \[ ] Obter informações da contraption (event.getContraption()) e sua posição antes da desmontagem (se possível através do evento).
            * \[ ] Enviar mensagem para o chat: "Contraption [ID/Tipo] desmontada (anteriormente em [X,Y,Z])".
    * \[ ] *Testar no Jogo:*
        * \[ ] Compilar e executar.
        * \[ ] Usar /togglecontraptionlog para ativar.
        * \[ ] Montar e desmontar contraptions, verificando as mensagens no chat.
        * \[ ] Desativar o log e verificar se as mensagens param.

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