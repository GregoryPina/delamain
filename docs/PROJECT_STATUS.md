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

TASK-014 recebida no [PR #5](https://github.com/GregoryPina/delamain/pull/5), HEAD `8e4d8bc09e236ec1429f2a6935ba05691eccf21b`, base correta `f7e851bc799b018cd58c6f1bf2b15a54511f2c3f`. Status IMPLEMENTADA_AGUARDANDO_TESTE; revisão estática CHANGES REQUIRED em [registro](handoffs/PR-005-REVIEW.md). Código não aplicado à main/local; branch remota buscada para leitura.

Proprietário autorizou acumular testes. Próxima entrega externa: corrigir 014 e depois implementar 015 em branch dependente, conforme [despacho](../tasks/TASK-015-DISPATCH.md). Não é necessário aguardar teste manual entre essas fases; achados conhecidos devem ser tratados primeiro. Sem merge até aceite posterior. Coordenador não executou testes/builds.

[TASK-016–022](../tasks/NEXT_TASKS.md) continuam planejadas. Processo normal: Astra coordena/revisa, executor externo implementa, proprietário testa o conjunto quando disponível.

[Retomada](../TECH_LEAD_HANDOFF.md), [decisões](DECISIONS.md), [processo](DEVELOPMENT.md).
