# TASK-008 — status do sistema e respostas variadas

Status: implementado, aguardando teste do proprietário. `testDebugUnitTest`: 42 testes, 0 falhas.
Branch: `codex/system-status-responses`.

## Resumo

- `BATTERY_STATUS`: frases fechadas consultam `BatteryStatusPort`; adapter Android lê nível e carga sem permissão extra.
- `responseFor` e leituras de bateria usam `pickVariant` com ≥3 frases por tipo de resultado.
- Primeira variante preserva mensagens anteriores para compatibilidade de testes.

## Frases de bateria

`status do sistema`, `status da bateria`, `como esta a bateria`, `nivel da bateria`, `quanto de bateria`, `bateria`.

## Roteiro

1. `status do sistema` → percentual (e “carregando” se aplicável).
2. Repetir `abra o netflix` (app ausente) → ouvir variantes diferentes.
3. Volume/apps/mídia continuam funcionando.
