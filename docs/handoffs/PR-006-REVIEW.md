# Revisão conjunta TASK-014/015 — correções aplicadas

HEAD revisado após correções: ver commit em `codex/task-015-session-state` (posterior a `79c13b3`).
Base: `8f7da528c6ad36af15e1d0640f7b1acbca968ab2` (TASK-014 corrigida).
Resultado: achados de revisão estática tratados; `testDebugUnitTest`, `assembleDebug` e `assembleRelease` executados com sucesso nesta rodada.

## C1 — TTS associado à interação errada [P1]

`DebugCommandPanel` enviava `interactionCoordinator.output(state, interactionId)` com o ID **atual** do painel, não o ID do `speak()` em andamento. Fala antiga podia alterar o rosto da interação nova.

Correção: `SpeechOutputSession` propaga `(SpeechOutputState, interactionId)` em todos os callbacks; `speak(text, interactionId)` no `SpeechOutputPort`; painel passa o ID de `coordinator.begin()` no momento do pedido. STT usa `listenInteractionId` fixado ao iniciar OUVIR.

## C2 — Watchdog TTS ausente [P1]

TASK-015 exigia timers canceláveis para inicialização (10s) e conclusão proporcional ao texto, sem sucesso por timeout.

Correção: `SpeechOutputWatchdog` com `SpeechOutputWatchdogScheduler` injetável; acoplado em `AndroidTextToSpeechPort` via `Handler`. Timeout em Queued → `Failed`; em Speaking → `Failed` se não houver evento terminal.

## C3 — Assinatura release do painel DEV [P2]

`DelamainApp` passou `InteractionCoordinator` ao stub release; versão intermediária não aceitava o parâmetro.

Correção: já presente em `79c13b3`; mantida e validada com `assembleRelease`.

## C4 — Eventos de infraestrutura no rosto [P2]

Callbacks de `Preparing`/`Ready` com `interactionId = 0` podiam competir com boot.

Correção: `InteractionCoordinator` ignora `interactionId == 0L` em `input`/`output`.

## Testes adicionados

- `SpeechOutputWatchdogTest`
- `SpeechOutputSessionTest`: ID de interação preservado; watchdog de fila
- `InteractionCoordinatorTest`: eventos com ID zero ignorados

## Pendências

Validação manual acumulada pelo proprietário (roteiro em `docs/handoffs/TASK-015.md`). Sem merge em `main` até aceite. Registro histórico desta entrega; TASK-016–019 implementadas posteriormente. Estado atual em docs/PROJECT_STATUS.md.
