# `PLANO_DE_IMPLEMENTACAO_REFINADO_CLAUDE.md`

## **Assunto:** Continuação e Refinamento do Projeto: Compatibilidade Create & LittleTiles

## **1. Contexto e Próximos Passos**

**Reconhecimento do Progresso:**
Excelente trabalho na implementação inicial do sistema de "Model Baking". Conforme o `timeline.md`, a Fase 1 (Limpeza) e a implementação do `LittleTilesModelBaker`, `ContraptionModelCache` e a integração com o `ContraptionEventHandler` foram concluídas com sucesso. O projeto está em um estado compilável e pronto para os próximos passos.

**Objetivo Desta Fase:**
Nosso foco agora é duplo:
1.  **Refinar a arquitetura de cache:** Vamos substituir o `ContraptionModelCache` estático por uma "Duck Interface". Isso tornará nossa solução mais robusta, segura em relação à memória e mais alinhada às melhores práticas de modding que vimos no `Create Interactive`.
2.  **Implementar o Hook de Renderização:** Adicionar o Mixin final que injeta nossos modelos "assados" no pipeline de renderização do Create, fazendo com que os blocos finalmente apareçam no jogo.

## **2. Fluxo de Trabalho e Controle de Versão (Git)**

Continue seguindo o fluxo de trabalho estabelecido:
* **`timeline.md`**: Documente cada passo completado.
* **Commits Atômicos**: Ao final de cada passo numerado, confirme que o projeto compila.
* **Instruções de Git**: Após cada passo bem-sucedido, execute:
    1.  `git add .`
    2.  `git commit -m "Mensagem do Commit Indicada no Passo"`
    3.  `git push`

---

## **3. Fase 2 (Refinamento): Migração para "Duck Interface"**

**Objetivo:** Substituir o cache estático por um cache atrelado diretamente ao objeto `Contraption`.

### **Passo 2.1: Remover o Cache Estático Obsoleto**

1.  **Ação**: Delete a classe `com.createlittlecontraptions.rendering.cache.ContraptionModelCache`. Ela será completamente substituída pela nova abordagem.
2.  **Confirmação**: O projeto não irá compilar neste ponto. Isso é esperado.
3.  **Git Commit**:
    * `git add .`
    * `git commit -m "refactor(cache): Remove static ContraptionModelCache to replace with Duck Interface"`
    * `git push`

### **Passo 2.2: Criar a "Duck Interface" e o Mixin para `Contraption`**

**Objetivo:** Adicionar a capacidade de armazenar nosso cache diretamente em qualquer instância da classe `Contraption` do Create.

1.  **Criar a Interface "Duck"**:
    * Crie o pacote `com.createlittlecontraptions.mixins.duck`.
    * Dentro dele, crie a interface `IContraptionBakedModelCache`.
    * Defina os seguintes métodos na interface:
        ```java
        void setModelCache(Map<BlockPos, BakedModel> cache);
        Optional<Map<BlockPos, BakedModel>> getModelCache();
        ```
2.  **Criar o Mixin para `Contraption`**:
    * Crie o pacote `com.createlittlecontraptions.mixins.create`.
    * Dentro dele, crie a classe `ContraptionMixin`.
    * Faça-a aplicar o Mixin à classe `com.simibubi.create.content.contraptions.Contraption` e implementar a interface `IContraptionBakedModelCache`.
    * Adicione o campo para o cache: `private Map<BlockPos, BakedModel> bakedModelCache;`.
    * Implemente os métodos da interface para que atuem como getters e setters para este campo.
3.  **Confirmação**: Verifique se o projeto compila.
4.  **Git Commit**:
    * `git add .`
    * `git commit -m "feat(cache): Implement Duck Interface to cache baked models on Contraption objects"`
    * `git push`

### **Passo 2.3: Atualizar o `ContraptionEventHandler` para Usar o "Duck"**

**Objetivo:** Modificar a lógica de eventos para usar o novo sistema de cache.

1.  **Modificar `ContraptionEventHandler`**:
    * No método `onEntityJoinLevel`, onde você itera pelos `BlockEntity`s e chama o `LittleTilesModelBaker`, a lógica de "baking" permanece a mesma.
    * **Mude o armazenamento:** Ao final do loop, em vez de chamar o antigo cache estático, faça o seguinte:
        1.  Obtenha o objeto `Contraption`: `contraption.getContraption()`.
        2.  Faça o cast deste objeto para sua nova interface: `(IContraptionBakedModelCache) contraption.getContraption()`.
        3.  Use o setter para armazenar o mapa de modelos "assados": `duck.setModelCache(seuMapLocalDeModelos);`.
    * O método `onEntityLeaveLevel` não precisa mais limpar o cache, pois o Java Garbage Collector fará isso automaticamente quando o objeto `Contraption` for destruído. Você pode remover a lógica de limpeza dele.
2.  **Confirmação**: O projeto deve compilar novamente.
3.  **Git Commit**:
    * `git add .`
    * `git commit -m "refactor(events): Update event handler to use Duck Interface for caching"`
    * `git push`

---

## **4. Fase 3: Implementação do Hook de Renderização**

**Objetivo:** O passo final. Fazer com que o renderizador do Create use os modelos do nosso novo cache "ducked".

### **Passo 3.1: Criar o Mixin para `ContraptionRenderInfo`**

**Objetivo:** Interceptar o processo de obtenção de modelos do renderizador legado e fornecer nossos modelos customizados.

1.  **Criar Mixin para `ContraptionRenderInfo`**:
    * Em `mixins/create`, crie a classe `ContraptionRenderInfoMixin`.
    * Use `@Redirect` para interceptar a chamada `dispatcher.getBlockModel(state)` dentro do método `buildStructureBuffer`.
2.  **Implementar a Lógica do Redirect**:
    * A assinatura do seu método de redirecionamento deve receber os parâmetros da chamada original e os objetos de contexto. Use `@Local` para injetar a variável `BlockPos pos`.
        ```java
        @Redirect(method = "buildStructureBuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;getBlockModel(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/BakedModel;"))
        private BakedModel onGetBlockModel(BlockRenderDispatcher dispatcher, BlockState state, RenderType layer, @Local(name = "pos") BlockPos pos) {
            // ... sua lógica aqui
        }
        ```
    * **Dentro da lógica do redirect**:
        1.  Obtenha a instância da `Contraption` através do campo `this.contraption` da instância `ContraptionRenderInfo` (que você pode obter adicionando `@Mixin-annotated 'this' as parameter` se precisar).
        2.  Faça o cast da `contraption` para `IContraptionBakedModelCache`.
        3.  Acesse o cache: `duck.getModelCache()`.
        4.  Se o cache existe (`isPresent()`) e contém um modelo para a `pos` atual (`cache.get().get(pos)`), retorne esse modelo.
        5.  Caso contrário, **chame o método original** e retorne seu resultado: `dispatcher.getBlockModel(state)`.
3.  **Confirmação**: Compile o projeto. Este é o passo que habilita a funcionalidade visual.
4.  **Git Commit**:
    * `git add .`
    * `git commit -m "feat(rendering): Inject baked models into Create's legacy renderer via ContraptionRenderInfo mixin"`
    * `git push`

## **5. Fase 4: Teste e Validação**

* Inicie o Minecraft.
* **Crucial:** Nas configurações do mod Create, **desabilite o "Flywheel Engine"**.
* Carregue um mundo e monte uma contraption com blocos complexos do Little Tiles.
* **Validação:** Os blocos devem agora ser renderizados corretamente na contraption.
* Finalize o `timeline.md` com os últimos passos.
* **Git Commit Final**:
    * `git add .`
    * `git commit -m "docs: Finalize timeline and complete legacy renderer implementation"`
    * `git push`

---

## **Apêndice A: Guia de Referência sobre Baked Models**

*(Esta seção permanece a mesma do plano anterior, pois continua sendo a referência técnica para o `LittleTilesModelBaker`)*

* **O que é um `BakedModel`?** É a representação final e imutável de um modelo 3D, composta por `BakedQuad`s.
* **O que é um `BakedQuad`?** É uma face de 4 vértices com todos os dados de renderização (posição, cor, UV, normais, textura).
* **Construção Programática:**
    1.  **Captura de Vértices:** Use um `VertexConsumer` customizado para salvar os dados de vértices gerados pelo `BETilesRenderer`.
    2.  **Construção de `BakedQuad`:** Empacote os dados de 4 vértices em um objeto `BakedQuad`.
    3.  **Construção do `BakedModel`:** Crie uma classe que implemente `BakedModel` e cujo método `getQuads(...)` retorne a lista de `BakedQuad`s gerada.