# Tech Lead handoff

## Baseline e estado

TASK-010–012 integradas em main após aceite funcional do proprietário. Baseline integrada: `a4789b1c9e3b6a74e325215487c849f133c6fcd3`. Este fechamento documental é posterior e não altera código.

Proprietário: “Testei o conjunto: microfone, fala, cancelar e trocar voz”. Registro completo, hashes e limites em [TASK-013](docs/handoffs/TASK-013.md). Não inferir testes unitários/release/offline a partir desse relato. Relatórios automáticos encontrados são antigos. Nenhum build/teste foi executado pelo coordenador nesta rodada.

## Próxima ação

TASK-013 concluída como consolidação; preparar TASK-014 para executor externo: lembrar voz escolhida, com fallback local elegível e preferência distinta da seleção real. Ler [plano de despacho](tasks/NEXT_TASKS.md) e [TASK-014](tasks/TASK-014.md). Informar hash completo publicado após este fechamento, não o hash de uma antiga branch pendente. TASK-014–022 planejadas, não despachadas.

## Entregas encerradas

PR #2: TTS, interrupção/callbacks/ciclo de vida. PR #3: escuta por botão, local em API31+ quando disponível. PR #4: comparação de vozes pt-BR instaladas, escolha por sessão. Integração em ordem 010 → 011 → 012, sem squash. Não reaplicar esses patches.

TASK-004–009 também concluídas. Nome/gatilho: VEXA; identificadores técnicos delamain preservados.

## Contratos e limitações

- LocalCommandEngine é canônico; executor remoto e CommandRouter legado não ativados. Mídia confirma despacho, não efeito.
- DEV concentra interação; release sem painel/microfone. Rosto ainda usa estados demonstrativos, não acompanha voz real.
- TTS Queued não significa fala concluída. Stop é aceite do motor, não prova silêncio físico. Sem watchdog de TTS ainda; fechar libera.
- STT: botão, sessão única, 15s, sem fallback remoto. Permissão concedida exige novo toque. Cancelamento invalida callbacks.
- Voz: seleção só na sessão; rede/dados ausentes excluídos. Persistência é TASK-014.
- Sem wake word, IA, play/pause ou volume percentual no fluxo ativo.
- Validação offline, rotas Bluetooth, cenários de lifecycle, testes unitários novos e release não confirmados separadamente. Nenhuma falha relatada.

## Processo

Exceção de implementação direta pelo Astra encerrou com 010–012. Processo normal: coordenador planeja/revisa/integra; proprietário encaminha tarefa ao executor externo e executa testes. Não criar agentes internos, executar testes automaticamente ou iniciar 014 localmente sem nova autorização.

Ao substituir: ler AGENTS/README/arquitetura/roadmap/estado/decisões, este arquivo e TEMP_LEAD_HANDOFF.md se existir. Registrar baselines inicial/final, evidências, PRs, mudanças locais e próximo passo. Preservar trabalho alheio; o STL não rastreado visto anteriormente não apareceu no status desta retomada, nenhuma ação foi feita nele.
