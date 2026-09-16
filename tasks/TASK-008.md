# TASK-008 — status do sistema e respostas variadas

## Objective

Consulta local de bateria (recorte de status do sistema) e variantes de resposta para resultados de ação, alternando frases equivalentes sem repetir imediatamente.

## Desired behavior

Frases como `status do sistema`, `bateria`, `nível da bateria` retornam percentual e estado de carga via porta injetável. **Toda** resposta local fora da API de IA — reflexos, hora, bateria e resultados de ação — deve ter **no mínimo 3 variantes**, com rotação sem repetição imediata.

## Constraints

- Sem novas permissões desnecessárias; leitura via `ACTION_BATTERY_CHANGED` sticky.
- Não inventar outros sensores além da bateria nesta TASK.
- Preservar significado factual; variar só a forma da frase.
- Não conectar roteador remoto.

## Acceptance criteria

- [ ] Frases de bateria reconhecidas; negações/compostos → `Unknown`.
- [ ] Painel DEV injeta `AndroidBatteryStatusPort`.
- [ ] App não instalado, volume, mídia e demais resultados alternam respostas.
- [ ] Testes unitários verdes; reflexos existentes intactos.

## Validation

Proprietário: `testDebugUnitTest`, `assembleDebug`, testar `status do sistema` e repetir comando de app ausente para ouvir variantes.
