# Estado do projeto

Atualizado em 2026-09-16. Baseline integrada: `a4789b1c9e3b6a74e325215487c849f133c6fcd3`; documentação de fechamento posterior.

## Entregas integradas

TASK-004–009: VEXA, parser legado corrigido e inativo, apps permitidos, volume, hora, mídia anterior/próxima, bateria, variantes e TTS inicial. Evidências históricas nos handoffs correspondentes.

TASK-010–012: TTS com estados/cancelamento/descarte, escuta local por botão no DEV e seleção de vozes pt-BR por sessão. PRs #2/#3/#4 integrados. Proprietário confirmou microfone, fala, cancelar e trocar voz. TASK-013 consolidou aceite e integração; [evidência e cobertura](handoffs/TASK-013.md).

## Em andamento (branches, sem merge em main)

TASK-014 (`8f7da52`), TASK-015 (`fd2b3d7`), TASK-016 (`codex/task-016-user-controls`): persistência, rosto/sessão e controles na tela principal. IMPLEMENTADA_AGUARDANDO_TESTE; validação manual acumulada pelo proprietário.

## Fluxo e limites

Texto ou reconhecimento final no DEV → LocalCommandEngine → portas de ação → resposta escrita/TTS. Apps: YouTube, Chrome, Maps, Spotify, WhatsApp. Mídia confirma envio de evento, não troca efetiva. Unknown permanece local.

STT on-device em API31+, uma frase por toque, timeout 15s. TTS com watchdog (fila 10s + limite proporcional). Voz persistida (014). Rosto acompanha sessão real (015). Sem IA, wake word, play/pause ou volume percentual ativo.

## Próximo passo

Proprietário validar em massa branches 014–016 (roteiros em handoffs). Depois merge em `main`.

[Retomada](../TECH_LEAD_HANDOFF.md), [decisões](DECISIONS.md), [processo](DEVELOPMENT.md).
