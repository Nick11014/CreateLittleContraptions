# Pesquisa Detalhada: Métodos de Renderização para LittleTiles em Contraptions

*Data de Criação: 29 de Maio de 2025*  
*Último Update: 29 de Maio de 2025 - 15:43*  
*Objetivo: Investigar métodos críticos para implementar renderização de LittleTiles em contraptions em movimento*

## ✅ Status da Pesquisa

**FASE AUTOMATIZADA CONCLUÍDA COM SUCESSO** (29/05/2025 15:41)
- ✅ **Implementação:** Comando `/contraption-debug rendering` funcional 
- ✅ **GameTests:** 5 testes automatizados passando (1.758s execução)
- ✅ **Validação:** Sistema de análise de renderização operacional  
- ✅ **Client:** Carregado e pronto para testes manuais (15:43)
- ⚙️ **Próximo:** Executar testes manuais in-game para coletar dados de renderização

---

## 🎯 Objetivos da Pesquisa

### Questões Principais a Responder:
1. **Parâmetros e Retornos:** Quais parâmetros devemos utilizar nos métodos relacionados à renderização de um bloco na contraption e qual o retorno de cada método
2. **Diferenças de Renderização:** Qual a diferença entre a renderização de um bloco comum e um bloco do LittleTiles
3. **Bloqueios de Renderização:** Quais métodos podem estar impedindo a renderização do bloco do LittleTiles dentro da elevator contraption

---

## 🔍 Step 2.5: Investigação Profunda de Métodos de Renderização

### **Fase 1: Análise de Métodos Críticos do BlockTile (LittleTiles)**

#### **1.1 Métodos de Shape e Renderização**
```java
// INVESTIGAR: Parâmetros, retorno e comportamento
VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos)
```
- **❓ Investigar:** Que tipo de `VoxelShape` é retornado para LittleTiles?
- **❓ Comparar:** Como difere do retorno de um bloco comum?
- **❓ Contexto:** Como `BlockGetter level` e `BlockPos pos` são utilizados?

```java
// INVESTIGAR: Impacto na renderização visual
boolean supportsExternalFaceHiding(BlockState state)
```
- **❓ Investigar:** Valor retornado para LittleTiles vs blocos comuns
- **❓ Impacto:** Como afeta a renderização em contraptions?

```java
// INVESTIGAR: Sistema de iluminação dinâmica
boolean hasDynamicLightEmission(BlockState state)
```
- **❓ Investigar:** LittleTiles usa iluminação dinâmica?
- **❓ Problema:** Pode causar conflitos em contraptions?

```java
// INVESTIGAR: Oclusão de luz por forma
boolean useShapeForLightOcclusion(BlockState state)
```
- **❓ Investigar:** Como LittleTiles calcula oclusão de luz?
- **❓ Conflito:** Pode interferir com sistema de luz da contraption?

#### **1.2 Métodos de Estado e Viewpoint**
```java
// INVESTIGAR CRÍTICO: Estado dinâmico baseado em viewpoint
BlockState getStateAtViewpoint(BlockState state, BlockGetter level, BlockPos pos, Vec3 viewpoint)
```
- **❓ CRÍTICO:** Como `Vec3 viewpoint` é calculado em contraptions em movimento?
- **❓ Problema:** Viewpoint pode estar incorreto para blocos em movimento?
- **❓ Investigar:** Diferença de comportamento vs blocos estáticos

#### **1.3 Métodos de Propagação de Luz**
```java
// INVESTIGAR: Sistema de skylight
boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos)
```
- **❓ Investigar:** LittleTiles propaga skylight diferentemente?
- **❓ Conflito:** Problemas com skylight em contraptions subterrâneas?

### **Fase 2: Análise de Métodos da ControlledContraptionEntity**

#### **2.1 Métodos de Renderização da Contraption**
```java
// INVESTIGAR: Loop principal de renderização
void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick)
```
- **❓ CRÍTICO:** Como `PoseStack` é manipulado para blocos individuais?
- **❓ CRÍTICO:** `MultiBufferSource` - como buffers são alocados para cada bloco?
- **❓ CRÍTICO:** `int packedLight` - como iluminação é calculada para blocos em movimento?
- **❓ CRÍTICO:** `float partialTick` - interpolação de movimento afeta renderização?

#### **2.2 Métodos de Posicionamento e Rotação**
```java
// INVESTIGAR: Transformações de posição
Vec3 applyRotation(Vec3 localPos, float partialTick)
Vec3 reverseRotation(Vec3 worldPos, float partialTick)
```
- **❓ Investigar:** Como `Vec3` é transformado para cada bloco?
- **❓ Problema:** LittleTiles pode ter problemas com transformações de rotação?

```java
// INVESTIGAR: Controle de ângulo
float getAngle(float partialTick)
void setAngle(float angle)
```
- **❓ Investigar:** Como ângulo afeta renderização de blocos individuais?

### **Fase 3: Análise de Métodos de ElevatorContraption**

#### **3.1 Acesso aos Blocos**
```java
// INVESTIGAR CRÍTICO: Estrutura de dados dos blocos
Map<BlockPos, StructureBlockInfo> getBlocks()
```
- **❓ CRÍTICO:** Como `StructureBlockInfo` representa LittleTiles?
- **❓ CRÍTICO:** `BlockPos` é posição local ou mundial?
- **❓ Investigar:** Estrutura interna do `StructureBlockInfo` para LittleTiles

#### **3.2 Métodos de Assembly**
```java
// INVESTIGAR: Processo de captura de blocos
Pair<StructureTemplate, Contraption> capture(Level level, BlockPos pos)
```
- **❓ Investigar:** Como LittleTiles são capturados durante assembly?
- **❓ Problema:** Dados do LittleTiles podem estar sendo perdidos?

```java
// INVESTIGAR: Validação de assembly
boolean assemble(Level level, BlockPos pos) throws AssemblyException
```
- **❓ Investigar:** Validações específicas para LittleTiles?
- **❓ Erro:** LittleTiles podem estar falhando na validação?

---

## 🧪 Metodologia de Investigação

### **Etapa 1: Expansão do Comando /contraption-debug**
Implementar análise específica de métodos de renderização:

```java
// ADICIONAR ao ContraptionDebugCommand.java
private void analyzeRenderingMethods(ControlledContraptionEntity contraption, 
                                   CommandSourceStack source) {
    
    // 1. Analisar cada bloco LittleTiles na contraption
    Map<BlockPos, StructureBlockInfo> blocks = contraption.getContraption().getBlocks();
    
    for (Map.Entry<BlockPos, StructureBlockInfo> entry : blocks.entrySet()) {
        StructureBlockInfo blockInfo = entry.getValue();
        
        if (blockInfo.state().getBlock() instanceof BlockTile) {
            BlockTile blockTile = (BlockTile) blockInfo.state().getBlock();
            BlockPos localPos = entry.getKey();
            
            // INVESTIGAR: Métodos específicos
            analyzeBlockTileRenderingMethods(blockTile, blockInfo.state(), localPos, source);
        }
    }
    
    // 2. Analisar métodos da contraption entity
    analyzeContraptionRenderingMethods(contraption, source);
}

private void analyzeBlockTileRenderingMethods(BlockTile blockTile, BlockState state, 
                                            BlockPos pos, CommandSourceStack source) {
    
    // TESTAR MÉTODOS CRÍTICOS:
    
    // 1. Shape e Suporte
    VoxelShape supportShape = blockTile.getBlockSupportShape(state, /* mock level */, pos);
    
    // 2. Ocultação de Faces
    boolean hidesFaces = blockTile.supportsExternalFaceHiding(state);
    
    // 3. Iluminação Dinâmica
    boolean dynamicLight = blockTile.hasDynamicLightEmission(state);
    
    // 4. Oclusão de Luz
    boolean lightOcclusion = blockTile.useShapeForLightOcclusion(state);
    
    // 5. Propagação de Skylight
    boolean skylightProp = blockTile.propagatesSkylightDown(state, /* mock level */, pos);
    
    // REPORTAR RESULTADOS
    source.sendSuccess(() -> Component.literal(
        String.format("§6BlockTile Analysis [%s]:\n" +
                     "  §7Support Shape: %s\n" +
                     "  §7Hides Faces: %b\n" +
                     "  §7Dynamic Light: %b\n" +
                     "  §7Light Occlusion: %b\n" +
                     "  §7Skylight Prop: %b",
                     pos, supportShape, hidesFaces, dynamicLight, lightOcclusion, skylightProp)
    ), false);
}
```

### **Etapa 2: GameTest para Comparação de Renderização**
Criar teste automatizado comparando bloco comum vs LittleTiles:

```java
@GameTest(template = "elevator_unassembled")
public void compareRenderingBehavior(GameTestHelper helper) {
    
    // 1. Criar contraption com bloco comum
    // 2. Criar contraption com LittleTiles
    // 3. Comparar retornos dos métodos de renderização
    // 4. Documentar diferenças
    
    helper.succeed();
}
```

### **Etapa 3: Teste Manual Focado**
Executar testes específicos no client:

1. **Contraption com Bloco Comum:**
   - Observar renderização normal
   - Executar `/contraption-debug rendering`

2. **Contraption com LittleTiles:**
   - Observar problemas de renderização
   - Executar `/contraption-debug rendering`
   - Comparar resultados

---

## 📊 Hipóteses a Validar

### **Hipótese 1: Problemas de VoxelShape**
- **Teoria:** LittleTiles retorna VoxelShape complexo que contraption não processa corretamente
- **Teste:** Comparar `getBlockSupportShape()` entre bloco comum e LittleTiles
- **Validação:** Verificar se shapes são preservados durante movimento

### **Hipótese 2: Conflitos de Iluminação**
- **Teoria:** Sistema de iluminação dinâmica do LittleTiles conflita com contraption
- **Teste:** Analisar `hasDynamicLightEmission()` e `useShapeForLightOcclusion()`
- **Validação:** Testar renderização em diferentes condições de luz

### **Hipótese 3: Problemas de Viewpoint**
- **Teoria:** `getStateAtViewpoint()` recebe coordenadas incorretas em contraptions
- **Teste:** Verificar `Vec3 viewpoint` durante movimento
- **Validação:** Comparar viewpoint para blocos estáticos vs em movimento

### **Hipótese 4: Perda de BlockEntity**
- **Teoria:** LittleTiles precisa de BlockEntity que não é preservado na contraption
- **Teste:** Verificar se `createBlockEntity()` é chamado para blocos em contraption
- **Validação:** Confirmar presença de BlockEntity durante movimento

### **Hipótese 5: Problemas de Assembly**
- **Teoria:** Dados específicos do LittleTiles são perdidos durante `capture()`
- **Teste:** Analisar `StructureBlockInfo` antes e depois do assembly
- **Validação:** Verificar integridade dos dados do LittleTiles

---

## 🎯 Resultados Esperados

### **Saídas da Investigação:**

1. **Tabela de Parâmetros e Retornos:**
   - Documentação completa de cada método crítico
   - Diferenças entre bloco comum e LittleTiles
   - Valores específicos para cada contexto (estático vs movimento)

2. **Mapa de Problemas:**
   - Métodos que retornam valores incorretos em movimento
   - Pontos onde dados são perdidos
   - Conflitos de renderização identificados

3. **Estratégia de Correção:**
   - Métodos que precisam ser interceptados/overridden
   - Transformações necessárias para coordenadas
   - Preservação de dados específicos do LittleTiles

---

## 📋 Próximos Passos

### **Implementação Imediata:**
1. **Expandir `/contraption-debug`** com análise de renderização
2. **Criar GameTests** para comparação automatizada
3. **Executar testes manuais** focados em renderização
4. **Documentar descobertas** em arquivo específico

### **Pós-Investigação:**
1. **Identificar métodos críticos** que precisam de correção
2. **Planejar implementação** de fixes específicos
3. **Criar Mixins** ou hooks necessários para correção
4. **Validar correções** através de testes

---

## 🔗 Referências e Contexto

- **DEVELOPMENT_TIMELINE.md:** Steps 1.5 e 2 completados
- **contraption-debug-results-analysis.md:** Classes e métodos já identificados
- **method-analysis-detailed.md:** Template para organização de dados
- **Novo_Planejamento.md:** Estratégia geral do projeto

---

*Este documento serve como roadmap detalhado para a investigação técnica necessária antes da implementação do Step 3 (renderização de LittleTiles em contraptions).*
