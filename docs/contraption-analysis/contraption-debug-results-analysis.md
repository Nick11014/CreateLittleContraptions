# Contraption Debug Results Analysis
*Data da Análise: 28 de Maio de 2025*

## 📋 Resumo da Contraption Analisada

- **Tipo:** ControlledContraptionEntity (ElevatorContraption)
- **Posição:** BlockPos{x=-7, y=67, z=22}
- **Total de Blocos:** 33
- **LittleTiles Detectados:** 2
- **Posições dos LittleTiles:**
  - `BlockPos{x=1, y=-3, z=0}`
  - `BlockPos{x=1, y=-2, z=0}`

---

## 🏗️ Análise de Classes da Contraption Entity

### **ControlledContraptionEntity** 
*Package: com.simibubi.create.content.contraptions*
- **Extends:** AbstractContraptionEntity
- **Métodos Declarados:** 23

#### **Métodos Principais (Public)**
- `void setPos(double, double, double)` - Posicionamento da entidade
- `void lerpTo(double, double, double, float, float, int)` - Interpolação de movimento
- `Vec3 applyRotation(Vec3, float)` - Aplicação de rotação
- `Vec3 reverseRotation(Vec3, float)` - Rotação reversa
- `void setRotationAxis(Axis)` - Definição do eixo de rotação
- `float getAngle(float)` - Obtenção do ângulo
- `void setAngle(float)` - Definição do ângulo
- `Vec3 getContactPointMotion(Vec3)` - Movimento de pontos de contato

#### **Métodos de Controle (Protected)**
- `void tickContraption()` - Loop principal da contraption
- `float getStalledAngle()` - Ângulo quando travada
- `void onContraptionStalled()` - Callback de travamento
- `boolean shouldActorTrigger(...)` - Trigger de actors

#### **Métodos de Persistência (Protected)**
- `void writeAdditional(CompoundTag, Provider, boolean)` - Serialização
- `void readAdditional(CompoundTag, boolean)` - Deserialização

#### **Métodos Herdados Relevantes (AbstractContraptionEntity)**
- `boolean isIgnoringBlockTriggers()` - Ignorar triggers de blocos
- `boolean handlePlayerInteraction(...)` - Interação com jogador
- `Optional getControllingPlayer()` - Jogador controlando
- `void addSittingPassenger(Entity, int)` - Adicionar passageiro

---

## 🏭 Análise da Contraption Interna

### **ElevatorContraption**
*Package: com.simibubi.create.content.contraptions.elevator*
- **Extends:** PulleyContraption
- **Métodos Declarados:** 15

#### **Métodos de Controle de Elevador**
- `void setAllControlsToFloor(int)` - Definir todos os controles para andar
- `void syncControlDisplays()` - Sincronizar displays de controle
- `int getContactYOffset()` - Offset Y de contato
- `void setClientYTarget(int)` - Definir target Y no client
- `boolean isTargetUnreachable(int)` - Verificar se target é inalcançável
- `Integer getCurrentTargetY(Level)` - Obter target Y atual

#### **Métodos de Gestão de Dados**
- `void tickStorage(AbstractContraptionEntity)` - Tick de armazenamento
- `ContraptionType getType()` - Tipo da contraption
- `CompoundTag writeNBT(Provider, boolean)` - Escrita NBT
- `void readNBT(Level, CompoundTag, boolean)` - Leitura NBT

#### **Métodos de Assembly**
- `boolean assemble(Level, BlockPos) throws AssemblyException` - Montagem
- `Pair capture(Level, BlockPos)` - Captura de blocos
- `void disableActorOnStart(MovementContext)` - Desabilitar actor

#### **Métodos de Coordenação**
- `void broadcastFloorData(Level, BlockPos)` - Broadcast de dados de andar
- `ColumnCoords getGlobalColumn()` - Coordenadas da coluna global

#### **Métodos Herdados Relevantes**
- `Map getBlocks()` *(de Contraption)* - **🔑 CRÍTICO para Step 3**
- `void expandBoundsAroundAxis(Axis)` - Expansão de bounds
- `boolean containsBlockBreakers()` - Contém quebra-blocos
- `Set getOrCreateColliders(Level, Direction)` - Criação de colliders

---

## 🧱 Análise de Classes de Blocos

### **1. BlockTile (LittleTiles)** ⭐
*Package: team.creative.littletiles.common.block.mc*
- **Extends:** BaseEntityBlock
- **Implements:** LittlePhysicBlock, SimpleWaterloggedBlock
- **Métodos Declarados:** 78

#### **Métodos de Renderização/Visual (Críticos para Step 3)**
- `boolean supportsExternalFaceHiding(BlockState)` - Ocultação de faces
- `boolean hasDynamicLightEmission(BlockState)` - Emissão dinâmica de luz
- `boolean useShapeForLightOcclusion(BlockState)` - Oclusão de luz por forma
- `VoxelShape getBlockSupportShape(BlockState, BlockGetter, BlockPos)` - Shape de suporte
- `boolean propagatesSkylightDown(BlockState, BlockGetter, BlockPos)` - Propagação de skylight
- `BlockState getStateAtViewpoint(BlockState, BlockGetter, BlockPos, Vec3)` - Estado no viewpoint

#### **Métodos de Estado e Placement**
- `BlockState getStateForPlacement(BlockPlaceContext)` - Estado para placement
- `void createBlockStateDefinition(Builder)` - Definição de estados
- `BlockState getStateByAttribute(Level, BlockPos, int)` - Estado por atributo

#### **Métodos de Interação**
- `boolean onDestroyedByPlayer(...)` - Destruição pelo jogador
- `boolean removedByPlayerClient(...)` - Remoção no client
- `void spawnDestroyParticles(...)` - Partículas de destruição

#### **Métodos de Física/Colisão**
- `boolean isCollisionShapeFullBlock(...)` - Shape de colisão completa
- `float getExplosionResistance(...)` - Resistência à explosão
- `float getEnchantPowerBonus(...)` - Bônus de enchantment

### **2. FenceBlock (Minecraft)**
*Package: net.minecraft.world.level.block*
- **Extends:** CrossCollisionBlock
- **Métodos Declarados:** 11

#### **Métodos de Conexão**
- `boolean connectsTo(BlockState, boolean, Direction)` - Conexão com outros blocos
- `boolean isSameFence(BlockState)` - Verificação de mesmo tipo de cerca

#### **Métodos de Shape**
- `VoxelShape getOcclusionShape(...)` - Shape de oclusão
- `VoxelShape getVisualShape(...)` - Shape visual

### **3. Block (Minecraft Base)**
*Package: net.minecraft.world.level.block*
- **Extends:** BlockBehaviour
- **Implements:** ItemLike, IBlockExtension, LittleBlockProvider
- **Métodos Declarados:** 72

#### **Métodos Fundamentais**
- `Reference builtInRegistryHolder()` - Holder do registry
- `BlockState getStateForPlacement(BlockPlaceContext)` - Placement
- `void createBlockStateDefinition(Builder)` - Definição de estados
- `BlockState updateFromNeighbourShapes(...)` - Update por vizinhos

### **4. RedstoneContactBlock (Create)**
*Package: com.simibubi.create.content.redstone.contact*
- **Extends:** WrenchableDirectionalBlock
- **Métodos Declarados:** 11

#### **Métodos de Redstone**
- `int getSignal(BlockState, BlockGetter, BlockPos, Direction)` - Sinal redstone
- `boolean isSignalSource(BlockState)` - Fonte de sinal
- `boolean canConnectRedstone(...)` - Conexão redstone
- `boolean hasValidContact(...)` - Contato válido

---

## 🎯 Insights para Step 3 (Renderização de LittleTiles)

### **Classes e Métodos Críticos Identificados:**

1. **ElevatorContraption.getBlocks()** - Acesso aos blocos da contraption
2. **BlockTile (LittleTiles)** - Classe principal dos blocos LittleTiles
3. **Métodos de renderização do BlockTile:** 
   - `supportsExternalFaceHiding()`
   - `hasDynamicLightEmission()`
   - `useShapeForLightOcclusion()`
   - `getBlockSupportShape()`

### **Próximos Passos Sugeridos:**
1. Investigar `BaseEntityBlock` (superclasse do BlockTile)
2. Analisar `LittlePhysicBlock` interface
3. Estudar sistema de `VoxelShape` para renderização
4. Investigar conexão com `BETiles` (Block Entity)

---

## 📊 Estatísticas da Análise

- **Total de classes analisadas:** 6
- **Métodos únicos identificados:** ~200+
- **Classes relacionadas ao LittleTiles:** 1 (BlockTile)
- **Classes relacionadas ao Create:** 3 (ControlledContraptionEntity, ElevatorContraption, RedstoneContactBlock)
- **Classes do Minecraft:** 2 (Block, FenceBlock)

---

*Esta análise fornece a base técnica necessária para implementar a renderização de LittleTiles em contraptions em movimento (Step 3).*
