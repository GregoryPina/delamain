# Estado do projeto

Atualizado em 2026-09-16. Baseline integrada: `a4789b1c9e3b6a74e325215487c849f133c6fcd3`; documentação de fechamento posterior.

## Entregas integradas

TASK-004–009: VEXA, parser legado corrigido e inativo, apps permitidos, volume, hora, mídia anterior/próxima, bateria, variantes e TTS inicial. Evidências históricas nos handoffs correspondentes.

TASK-010–012: TTS com estados/cancelamento/descarte, escuta local por botão no DEV e seleção de vozes pt-BR por sessão. PRs #2/#3/#4 integrados. Proprietário confirmou microfone, fala, cancelar e trocar voz. TASK-013 consolidou aceite e integração; [evidência e cobertura](handoffs/TASK-013.md).

Os 29 testes novos foram criados; execução não confirmada. Relatórios unitários/release encontrados são anteriores. Nenhum teste foi executado pelo coordenador. Aceite funcional não comprova todos os cenários offline/lifecycle ou aparelhos.

## Fluxo e limites

Texto ou reconhecimento final no DEV → LocalCommandEngine → portas de ação → resposta escrita/TTS. Apps: YouTube, Chrome, Maps, Spotify, WhatsApp. Mídia confirma envio de evento, não troca efetiva. Unknown permanece local.

STT somente on-device disponível em API31+, uma frase por toque, timeout 15s e sem fallback remoto. Permissão apenas debug; release sem DEV. TTS separa fila/início/término/erro, mas ainda não tem watchdog. Voz escolhida se perde ao fechar painel. Rosto não acompanha a sessão real. Sem IA, wake word, play/pause ou volume percentual ativo.

## Próximo passo e responsáveis

Preparar [TASK-014](../tasks/TASK-014.md), persistência da voz, contra baseline completa publicada. TASK-014–022 detalhadas em [plano](../tasks/NEXT_TASKS.md), não despachadas. Nenhum executor ativo; coordenação Astra, execução externa, testes pelo proprietário. Exceção de implementação local anterior encerrada.

[Retomada](../TECH_LEAD_HANDOFF.md), [decisões](DECISIONS.md), [processo](DEVELOPMENT.md).
