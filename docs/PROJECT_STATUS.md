# Estado do projeto

Atualizado em 2026-09-16. Fonte canônica do estado atual.
Baseline auditada: `80b48c396d5882d2d3e1cd2e0f8f918a34842a26`, em `main` e `origin/main` na retomada. Após a retomada documental, TASK-010 foi implementada na branch `codex/task-010-tts-hardening`, a partir de `02f65daf210e1b569c821e66085a9a9c9fa96e21`, aguardando testes e merge.

## Entregas integradas

| Tarefa | Resultado | Evidência histórica |
| --- | --- | --- |
| TASK-004/005 | Parser remoto corrigido; nome e chamada VEXA | PR #1 integrado após “teste ok” |
| TASK-006 | Apps da lista permitida | Proprietário: “teste ok”; handoff registra 32 testes verdes |
| TASK-007 | Próxima/faixa anterior | Proprietário: “testado ok”; handoff registra 37 testes verdes |
| TASK-008 | Bateria e pelo menos três variantes de respostas | Proprietário: “testado”; handoff registra 42 testes verdes |
| TASK-009 | TTS no painel DEV | Proprietário: “funcionou, ela falou”; voz padrão não agradou |

Essas evidências pertencem às entregas registradas em `handoffs/`. Não são execuções desta auditoria nem comprovam 42 testes na baseline atual. TASK-009 permanece aceita funcionalmente.

## Fluxo atual e limites

Entrada digitada no painel debug → LocalCommandEngine → portas tipadas de volume/apps/mídia/bateria → resposta local → TTS. Release não oferece o painel DEV. Apps permitidos: YouTube, Chrome, Maps, Spotify e WhatsApp.

- `Dispatched` significa evento de mídia enviado, sem garantir mudança de faixa.
- Parser remoto e AndroidCommandExecutor continuam inativos. Play/pause e volume percentual não foram ativados.
- Catálogo fechado; Unknown não chama IA. Sem microfone, wake word ou IA integrada.
- Na branch TASK-010, TTS acompanha fila/início/término/erro, cancela e protege descarte. Main mantém TASK-009 até aceite. Ver [handoff TASK-010](handoffs/TASK-010.md).
- Voz offline, rotas Bluetooth, limites de volume e comportamento entre aparelhos não estão comprovados pelos relatos resumidos.

## Plano atual

1. [TASK-010](../tasks/TASK-010.md): estabilizar TTS, resultados, cancelamento e ciclo de vida. Implementada excepcionalmente pelo Astra, aguardando testes locais. 12 testes criados, nenhum executado.
2. TASK-011: planejar STT por botão, uma frase por sessão, depois da TASK-010. Parar TTS antes de ouvir; resultados finais únicos; fallback remoto somente com escolha explícita.
3. Personalização de voz em tarefa separada; wake word e IA permanecem posteriores.

## Operação e memória

Implementação excepcional da TASK-010 encerrada, aguardando validação. Nas próximas tarefas: coordenação local exclusiva do Astra; implementadores externos via proprietário. Builds e testes pelo proprietário. Não criar agentes internos, repetir tarefas encerradas ou executar testes automaticamente.

Ler [handoff principal](../TECH_LEAD_HANDOFF.md), [auditoria da retomada](handoffs/RETOMADA-2026-09-16.md), [decisões](DECISIONS.md) e [processo de substituição](DEVELOPMENT.md). Histórico anterior preservado no Git e nos handoffs de cada tarefa.
