# Projeto de Implementação: Compatibilidade Visual entre Create e LittleTiles

## 1. Objetivo Principal

O objetivo deste projeto é implementar um sistema de "Model Baking" para o mod `CreateLittleContraptions`. Este sistema permitirá que blocos do mod Little Tiles, que usam um renderizador customizado (`BlockEntityRenderer`), sejam visualmente representados em contraptions do mod Create, que esperam modelos estáticos (`BakedModel`). A compatibilidade necessária é apenas visual.

## 2. Conceito da Solução ("Model Baking")

A estratégia consiste em, no momento da montagem de uma contraption:
1.  Identificar todos os blocos do Little Tiles.
2.  Executar o renderizador customizado deles uma única vez, de forma "offline".
3.  Capturar a geometria (vértices) gerada por essa renderização.
4.  Construir (ou "assar" / "bake") um `BakedModel` estático a partir dessa geometria.
5.  Armazenar em cache este novo modelo.
6.  Instruir o motor de renderização do Create a usar nosso modelo "assado" em vez do modelo original (que é invisível).

## 3. Ferramentas e Requisitos

* Plataforma: Minecraft Forge ou NeoForge
* Linguagem: Java
* Técnica de Injeção de Código: Mixins (SpongePowered Mixin)

## 4. Registro de Atividades (`timeline.md`)

Você deve criar e manter um arquivo na raiz do projeto chamado `timeline.md`. Cada passo significativo, como a remoção de um arquivo, criação de uma nova classe ou modificação de um método existente, deve ser registrado neste arquivo com uma breve descrição da ação e do motivo.

**Exemplo de entrada no `timeline.md`:**
```markdown
- **AÇÃO:** Deletado o arquivo `ContraptionBlockRenderController.java`.
- **MOTIVO:** Esta classe fazia parte de uma abordagem de controle de renderização em tempo real que está sendo descontinuada em favor da solução de "Model Baking".
```

---

## 5. Fase 1: Limpeza e Refatoração do Código Existente

Antes de implementar a nova solução, o projeto precisa ser limpo de todas as tentativas anteriores para evitar conflitos.

**Passos:**

1.  **Inicie o `timeline.md`**. Sua primeira entrada deve ser "Início do projeto de refatoração para implementação do Model Baking."
2.  **Delete os seguintes arquivos**, registrando cada remoção no `timeline.md`:
    * `rendering/ContraptionBlockRenderController.java`
    * `mixins/ContraptionEntityRendererFilterMixin.java`
    * `mixins/ContraptionEntityRendererMixin.java`
    * `mixins/ContraptionRendererMixin.java`
    * `mixins/ContraptionDisassemblyMixin.java`
    * `mixins/create/AllMovementBehavioursMixin.java`
3.  **Modifique os seguintes arquivos**, removendo código que se tornou obsoleto, e registre as mudanças no `timeline.md`:
    * **`CreateLittleContraptions.java`**:
        * Remova o método `registerLittleTilesMovementBehaviour()` e qualquer chamada a ele. A lógica de registro pode ser completamente removida da classe principal.
    * **`events/ContraptionEventHandler.java`**:
        * Remova o método `notifyNearbyPlayers` e qualquer chamada para ele.
        * Mantenha a classe e seus listeners de evento (`onEntityJoinLevel`, `onEntityLeaveLevel`), pois eles serão a base para a nova implementação.

Ao final desta fase, o projeto deve compilar sem erros, com apenas a estrutura básica e o `ContraptionEventHandler` prontos para a nova implementação.

---

## 6. Fase 2: Implementação do Sistema de "Baking" (Foco no Renderizador Legado)

Agora, implemente a nova lógica. Focaremos primeiro em fazê-la funcionar com o renderizador legado do Create, que é mais simples de interceptar.

**Passo 2.1: Crie o Cache de Modelos**
* Crie um novo pacote: `com.createlittlecontraptions.rendering.cache`.
* Dentro dele, crie a classe `ContraptionModelCache`.
* **Funcionalidade:**
    * Deve conter um `Map` estático para armazenar os modelos: `private static final Map<UUID, Map<BlockPos, BakedModel>> CACHED_MODELS = new ConcurrentHashMap<>();`. A chave externa é a UUID da entidade da contraption.
    * Crie os métodos estáticos públicos:
        * `public static void cacheModel(UUID contraptionId, BlockPos pos, BakedModel model)`
        * `public static Optional<BakedModel> getModel(UUID contraptionId, BlockPos pos)`
        * `public static void clearCache(UUID contraptionId)`

**Passo 2.2: Crie o "Baker" de Modelos**
* Crie um novo pacote: `com.createlittlecontraptions.rendering.baking`.
* Dentro dele, crie a classe `LittleTilesModelBaker`.
* **Funcionalidade:**
    * Crie um método estático público: `public static Optional<BakedModel> bake(BlockEntity blockEntity)`.
    * **Dentro do método `bake`:**
        1.  Verifique se o `blockEntity` é uma instância de `team.creative.littletiles.common.block.entity.BETiles`. Se não for, retorne `Optional.empty()`.
        2.  Obtenha o renderizador correspondente: `Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity)`. Verifique se ele é uma instância de `BETilesRenderer`.
        3.  **Crie um `VertexConsumer` customizado** que captura os dados de cada vértice (posição, cor, UVs, normais) e os armazena em uma lista, em vez de enviá-los para a GPU.
        4.  Chame o método `render` do `BETilesRenderer`, passando os parâmetros necessários (um `PoseStack` novo, o `VertexConsumer` customizado, luz e overlay padrões).
        5.  Após a chamada, use os dados de vértices capturados para construir uma `List<BakedQuad>`.
        6.  Crie uma implementação anônima ou uma subclasse simples de `BakedModel` que retorne a lista de `BakedQuad`s que você criou. Este é o seu modelo "assado".
        7.  Retorne `Optional.of(seuModeloAssado)`.
    * **Consulte o *Apêndice A* no final deste documento para uma referência técnica detalhada sobre a estrutura de `BakedModel` e `BakedQuad` e como construí-los programaticamente.**

**Passo 2.3: Integre o Processo de "Baking" na Montagem**
* Modifique a classe `ContraptionEventHandler`.
* **Dentro do método `onEntityJoinLevel` (que detecta a montagem):**
    1.  Faça o cast do `event.getEntity()` para `AbstractContraptionEntity` e obtenha sua UUID.
    2.  Verifique se `contraption.getContraption()` não é nulo.
    3.  Itere sobre a coleção de `BlockEntity` da contraption (`contraption.getContraption().getRenderedBEs()`).
    4.  Para cada `BlockEntity`, chame `LittleTilesModelBaker.bake(be)`.
    5.  Se o resultado (`Optional<BakedModel>`) estiver presente (`isPresent()`), adicione-o ao cache: `ContraptionModelCache.cacheModel(contraption.getUUID(), be.getBlockPos(), result.get())`.
* **Dentro do método `onEntityLeaveLevel` (que detecta a desmontagem):**
    1.  Obtenha a UUID da entidade e limpe o cache correspondente para liberar memória: `ContraptionModelCache.clearCache(contraption.getUUID())`.

**Passo 2.4: Injete o Modelo "Assado" no Renderizador do Create (Caminho Legado)**
* Crie um novo Mixin no pacote `mixins.create` para a classe `com.simibubi.create.content.contraptions.render.ContraptionRenderInfo`.
* **Use `@Redirect` para interceptar o método `buildStructureBuffer`:**
    * O alvo do redirect deve ser a chamada para `dispatcher.getBlockModel(state)`.
    * **Na sua lógica de redirecionamento (`@Redirect`):**
        1.  A assinatura do seu método deve corresponder aos parâmetros da chamada que você está substituindo, mais a instância da classe (`ContraptionRenderInfo info`, `BlockRenderDispatcher dispatcher`, `BlockState state`).
        2.  Você também precisará dos parâmetros do loop. Use `@Local` para injetar a variável `BlockPos pos`.
        3.  Obtenha a `contraption` a partir da instância (`info.getContraption()`) e sua UUID.
        4.  Tente buscar um modelo no seu cache: `Optional<BakedModel> cachedModel = ContraptionModelCache.getModel(contraption.entity.getUUID(), pos);`.
        5.  Se `cachedModel.isPresent()`, retorne `cachedModel.get()`.
        6.  Caso contrário, chame e retorne o método original: `dispatcher.getBlockModel(state)`.

---

## 7. Fase 3: Teste e Validação (Próximos Passos)

1.  Após implementar a Fase 2, compile o mod.
2.  Inicie o Minecraft e, nas configurações do mod Create, **desabilite a opção "Enable Flywheel Engine"** para forçar o uso do renderizador legado.
3.  Teste montando uma contraption que contenha blocos do Little Tiles. Eles devem agora aparecer visualmente.
4.  Verifique o `timeline.md` para garantir que todos os passos foram documentados.
5.  A adaptação para o motor Flywheel será um passo futuro e mais complexo, a ser abordado após a validação desta solução.

## 8. Estrutura de Arquivos Final (Exemplo)

```
com/createlittlecontraptions/
├── CreateLittleContraptions.java       (Modificado)
├── events/
│   └── ContraptionEventHandler.java    (Modificado)
├── mixins/create/
│   └── ContraptionRenderInfoMixin.java (Novo)
└── rendering/
    ├── baking/
    │   └── LittleTilesModelBaker.java    (Novo)
    └── cache/
        └── ContraptionModelCache.java      (Novo)
```

Este plano de ação define um caminho claro e incremental. Execute cada fase com atenção aos detalhes.

---

## Apêndice A: Guia de Referência sobre Baked Models no NeoForge

https://docs.neoforged.net/docs/resources/client/models/bakedmodel/

Esta seção serve como um guia técnico para a criação programática de `BakedModel`s, que é o núcleo do `LittleTilesModelBaker`.

### O que é um `BakedModel`?

Um `BakedModel` é a representação final e otimizada de um modelo 3D, pronta para ser enviada para a GPU. É imutável e contém toda a informação necessária para a renderização.

* **Composição:** É primariamente uma coleção de `BakedQuad`.
* **Propriedades:** Define atributos como o uso de *ambient occlusion*, se é um modelo 3D na GUI, qual a textura de partícula e como o modelo se comporta em diferentes situações (`ItemOverrides`).
* **Nosso Objetivo:** Nosso objetivo é criar uma implementação simples desta interface que armazene e forneça uma lista de `BakedQuad`s que capturamos do Little Tiles.

### O que é um `BakedQuad`?

Um `BakedQuad` representa uma única face de 4 vértices (um quadrilátero). Ele contém todos os dados de seus vértices de forma compactada, pronta para o buffer.

* **Dados por Vértice:** Cada um dos 4 vértices tem:
    * Posição (x, y, z)
    * Cor (r, g, b, a)
    * Coordenadas de Textura (u, v) no atlas de texturas.
    * Coordenadas de Luz (u, v) no atlas de luz.
    * Normal (Nx, Ny, Nz) para cálculo de iluminação.
* **Dados do Quad:** Além dos dados dos vértices, o quad também armazena:
    * `tintIndex`: Usado para colorir o quad (ex: folhagens). Usaremos `-1` para desativar.
    * `Direction`: A direção para a qual a face aponta (e.g., `Direction.UP`). Usado para culling e para o método `getQuads`.
    * `TextureAtlasSprite`: A textura (sprite) que deve ser aplicada a esta face.

### Como Construir um `BakedModel` Programaticamente

O processo envolve dois grandes passos: capturar a geometria e depois construir os objetos do modelo.

#### 1. Capturando a Geometria

A classe `LittleTilesModelBaker` precisará de um `VertexConsumer` customizado. A maneira mais fácil de fazer isso é estender `BufferBuilder` ou criar uma implementação de `VertexConsumer` que, em vez de escrever em um `ByteBuffer`, escreve os dados em uma lista de objetos que você define.

Quando o `BETilesRenderer` chamar `.vertex(x, y, z)`, `.color(...)`, `.uv(...)`, `.normal(...)`, etc., seu `VertexConsumer` irá simplesmente armazenar esses valores. Quando `endVertex()` for chamado, você terá um vértice completo. Após 4 chamadas de `endVertex()`, você terá um quad completo.

#### 2. Construindo os Objetos

1.  **Construindo um `BakedQuad`:**
    * Uma vez que você tenha os dados para 4 vértices, você precisa empacotá-los em um `BakedQuad`. A classe `BakedQuad` tem um construtor que aceita um array de inteiros (`int[]`) com os dados dos vértices, um `tintIndex`, a `Direction` da face, e o `TextureAtlasSprite`.
    * Você precisará de uma função auxiliar para empacotar os dados de cada vértice (floats e bytes) em inteiros, seguindo o `VertexFormat` do jogo (geralmente `DefaultVertexFormat.BLOCK`). A ordem e o formato são cruciais.

2.  **Construindo o `BakedModel`:**
    * Crie uma classe simples que implementa `BakedModel`.
    * No construtor, passe a `List<BakedQuad>` que você gerou. Você pode querer organizá-los em um `Map<Direction, List<BakedQuad>>` para otimizar o `getQuads`.
    * **Implemente os métodos de `BakedModel`:**
        * `getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand)`: Este é o método mais importante. Se `side` for nulo, retorne todos os quads. Se não, retorne apenas os quads da face correspondente.
        * `useAmbientOcclusion()`: Retorne `true` na maioria dos casos.
        * `isGui3d()`: Retorne `false`, pois não será usado na GUI.
        * `usesBlockLight()`: Retorne `true`.
        * `isCustomRenderer()`: Retorne `false`, pois estamos fornecendo um modelo padrão.
        * `getParticleIcon()`: Retorne uma textura padrão (ex: `Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("block/dirt"))`).
        * `getOverrides()`: Retorne `ItemOverrides.EMPTY`.

Com estas etapas, seu `LittleTilesModelBaker` será capaz de traduzir a geometria dinâmica do Little Tiles para o formato estático que o Create pode entender e renderizar.
