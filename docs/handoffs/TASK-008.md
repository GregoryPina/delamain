# TASK-008 — status do sistema e respostas variadas

Status: concluída e integrada em `main`. Proprietário confirmou “testado”. `testDebugUnitTest`: 42 testes, 0 falhas.
Branch: `codex/system-status-responses`.

## Resumo

- `BATTERY_STATUS`: frases fechadas consultam `BatteryStatusPort`; adapter Android lê nível e carga sem permissão extra.
- `responseFor`, bateria e reflexos sociais usam ≥3 variantes cada (chamada, presença, saudação, agradecimento, hora, volume, apps, mídia).
- Rotação sem repetição imediata via `pickVariant` / `nextResponse`.

## Frases de bateria

`status do sistema`, `status da bateria`, `como esta a bateria`, `nivel da bateria`, `quanto de bateria`, `bateria`.

## Roteiro

1. `status do sistema` → percentual (e “carregando” se aplicável).
2. Repetir `abra o netflix` (app ausente) → ouvir variantes diferentes.
3. Volume/apps/mídia continuam funcionando.
