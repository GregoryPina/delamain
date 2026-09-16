# Tech Lead handoff

## Baseline e estado

TASK-010–012 integradas em main após aceite funcional do proprietário. Baseline integrada: `a4789b1c9e3b6a74e325215487c849f133c6fcd3`. Este fechamento documental é posterior e não altera código.

Proprietário: “Testei o conjunto: microfone, fala, cancelar e trocar voz”. Registro em [TASK-013](docs/handoffs/TASK-013.md).

## Próxima ação

TASK-014 corrigida (`codex/task-014-voice-preference`, `8f7da52`). TASK-015 revisada e corrigida (`codex/task-015-session-state`); ver [PR-006-REVIEW](docs/handoffs/PR-006-REVIEW.md). `testDebugUnitTest` + `assembleDebug` + `assembleRelease` verdes nesta rodada. Aguardar validação manual acumulada do proprietário; sem merge em `main`. TASK-016+ não iniciadas.

## Entregas encerradas

PR #2: TTS, interrupção/callbacks/ciclo de vida. PR #3: escuta por botão, local em API31+ quando disponível. PR #4: comparação de vozes pt-BR instaladas, escolha por sessão. Integração em ordem 010 → 011 → 012, sem squash. Não reaplicar esses patches.

TASK-004–009 também concluídas. Nome/gatilho: VEXA; identificadores técnicos delamain preservados.

## Contratos e limitações

- LocalCommandEngine é canônico; executor remoto e CommandRouter legado não ativados. Mídia confirma despacho, não efeito.
- DEV concentra interação; release sem painel/microfone funcional.
- TTS Queued não significa fala concluída. Stop é aceite do motor, não prova silêncio físico. Watchdog TTS implementado na 015 (fila 10s + limite proporcional); timeout reporta falha, não sucesso.
- STT: botão, sessão única, 15s, sem fallback remoto. Permissão concedida exige novo toque. Cancelamento invalida callbacks.
- Voz: persistência implementada na 014 (DataStore); seleção offline pt-BR. Rosto acompanha sessão real na 015.
- Sem wake word, IA, play/pause ou volume percentual no fluxo ativo.
- Validação offline/lifecycle no aparelho ainda pendente. Testes unitários e builds debug/release verdes na branch 015.

## Processo

Exceção de implementação direta pelo Astra encerrou com 010–012. Processo normal: coordenador planeja/revisa/integra; proprietário encaminha tarefa ao executor externo e executa testes. Não criar agentes internos, executar testes automaticamente ou iniciar 014 localmente sem nova autorização.

Ao substituir: ler AGENTS/README/arquitetura/roadmap/estado/decisões, este arquivo e TEMP_LEAD_HANDOFF.md se existir. Registrar baselines inicial/final, evidências, PRs, mudanças locais e próximo passo. Preservar trabalho alheio; o STL não rastreado visto anteriormente não apareceu no status desta retomada, nenhuma ação foi feita nele.
