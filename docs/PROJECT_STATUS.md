# Estado do projeto

Atualizado em 2026-09-16. Baseline integrada: `a4789b1c9e3b6a74e325215487c849f133c6fcd3`; documentação de fechamento posterior.

## Entregas integradas

TASK-004–009: VEXA, parser legado corrigido e inativo, apps permitidos, volume, hora, mídia anterior/próxima, bateria, variantes e TTS inicial. Evidências históricas nos handoffs correspondentes.

TASK-010–012: TTS com estados/cancelamento/descarte, escuta local por botão no DEV e seleção de vozes pt-BR por sessão. PRs #2/#3/#4 integrados. Proprietário confirmou microfone, fala, cancelar e trocar voz. TASK-013 consolidou aceite e integração; [evidência e cobertura](handoffs/TASK-013.md).

## Em andamento (branches, sem merge em main)

| TASK | Branch | Entrega | Status |
| --- | --- | --- | --- |
| 014 | `codex/task-014-voice-preference` (`8f7da52`) | Persistência de voz (DataStore) | IMPLEMENTADA_AGUARDANDO_TESTE |
| 015 | `codex/task-015-session-state` (`fd2b3d7`) | `InteractionCoordinator`, rosto por eventos reais, watchdog TTS | IMPLEMENTADA_AGUARDANDO_TESTE |
| 016 | `codex/task-016-user-controls` (`72b32ce`) | `VoiceInteractionSession` compartilhada, OUVIR/PARAR/TEXTO na tela principal | IMPLEMENTADA_AGUARDANDO_TESTE |
| 017 | `codex/task-017-mute-controls` | Controles prioritários (parar/cancelar) e modo mute (ADR-007) | IMPLEMENTADA_AGUARDANDO_TESTE |

Validação manual acumulada pelo proprietário. Roteiros em `docs/handoffs/TASK-014.md` … `TASK-017.md`.

## Fluxo e limites

Entrada (OUVIR, TEXTO ou DEV) → `InteractionControlRecognizer` (parar/cancelar/mute) → `LocalCommandEngine` → portas de ação → resposta na tela; TTS automático salvo se mute desligado. Apps: YouTube, Chrome, Maps, Spotify, WhatsApp. Mídia confirma envio de evento, não troca efetiva. Unknown permanece local.

STT on-device em API31+, uma frase por toque, timeout 15s. TTS com watchdog (fila 10s + limite proporcional). Voz e mute persistidos no mesmo DataStore (014/017). Rosto acompanha sessão real (015). Prévia explícita (TESTAR VOZ no DEV) fala mesmo em mute. Sem IA, wake word, play/pause ou volume percentual ativo.

## Próximo passo

Proprietário validar em massa branches 014–017. Depois merge sequencial em `main` (014 → 015 → 016 → 017).

[Retomada](../TECH_LEAD_HANDOFF.md), [decisões](DECISIONS.md), [processo](DEVELOPMENT.md).
