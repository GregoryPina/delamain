# Tech Lead handoff

## Baseline

Repositório: GregoryPina/delamain. Branch de trabalho: `codex/task-012-voice-preview`, baseada em `7517161e13a30ef7702b3f669ee7aba9ca5164a8` da TASK-011/PR #3, que depende da TASK-010/PR #2. Main permanece sem as três tarefas.
Baseline auditada em 2026-09-16: `80b48c396d5882d2d3e1cd2e0f8f918a34842a26`, sincronizada com origin/main na retomada. O código TTS entrou em `add292a`; commits seguintes até esta baseline documentam fechamento. A implementação TASK-010 partiu de `02f65daf210e1b569c821e66085a9a9c9fa96e21`; nenhum build/teste novo foi executado.

## Objetivo e estado

VEXA já responde por texto e fala no painel DEV. TASK-004 a TASK-009 integradas; não refazer nem reenviar. Apps, volume, hora, mídia, bateria, variantes e TTS têm aceite histórico do proprietário; detalhes em docs/PROJECT_STATUS.md e docs/handoffs/TASK-006.md a TASK-009.md. Não inventar logs, aparelho ou cobertura a partir de “teste ok”.

## Próxima ação

Receber validação conjunta do proprietário para TASK-010, TASK-011 e TASK-012, implementadas excepcionalmente pelo Astra em branches dependentes. Roteiro consolidado: docs/handoffs/TASK-012.md. Checkout já contém as três; 29 testes novos criados, nenhum executado. Status das três: IMPLEMENTADA_AGUARDANDO_TESTE; contratos específicos nos respectivos handoffs. Revisar o HEAD testado antes de integrar; não reenviar implementação a outro executor.

TASK-011 já implementada por autorização de continuar nesta sessão: escuta local por botão, API31+, sem fallback remoto, timeout e resultados únicos. Roteiro em docs/handoffs/TASK-011.md; dez testes novos não executados. Fechar TASK-010 antes de integrar TASK-011. TASK-012 já oferece comparação de vozes locais instaladas, apenas na sessão DEV; preferência persistente, engine/pitch/velocidade permanecem futuros. Integração em ordem 010 → 011 → 012, retarget das filhas para main e revisão de diff após cada merge; não integrar automaticamente em branches mães. Wake word, IA, play/pause e volume percentual continuam futuros.

## Achados e limites

- Na branch TASK-010, `Queued` substitui `Spoken`; sessão controla callbacks por pedido, stop/descarte e prontidão. DEV tem status e PARAR VOZ. Voz pt-BR sem rede declarada; teste real ainda pendente. Sem timeout para motor que nunca responde: fechar painel libera instância.
- TASK-009 foi aceita: não reinterpretar estes riscos de revisão estática como falha reproduzida no aparelho.
- LocalCommandEngine é canônico; CompositeLocalActionPort reúne adapters ativos. Não ativar domain/command/CommandRouter ou platform/AndroidCommandExecutor.
- Mídia confirma despacho, não efeito no player. Unknown não dispara IA.
- Release não tem painel DEV; integração com estados faciais não está concluída.

## Responsáveis e validação

Nenhum executor ativo. O proprietário autorizou Astra implementar TASK-010 e depois continuar o desenvolvimento nesta sessão como exceção em 2026-09-16. Amanhã e nas próximas tarefas o método habitual permanece: Astra coordena e integra; executores externos recebem TASK + BASE COMMIT exato, entregam branch/PR ou patch. Nunca main/merge pelo executor. Não criar/reativar agentes internos. Proprietário executa builds/testes; coordenador faz revisão estática e registra a origem de cada evidência.

## Trabalho local preservado

`Sem_titulo_bordas_arredondadas.stl` estava não rastreado na retomada. Não incluir, apagar ou alterar sem tarefa correspondente. Não descartar outras mudanças que apareçam após este registro.

## Quando substituir ou retornar

Ler AGENTS.md, README, arquitetura, roadmap, estado, decisões e docs/DEVELOPMENT.md. Ler TEMP_LEAD_HANDOFF.md se existir; ele não existia nesta retomada. Registrar nele baseline inicial/final completas, tarefas, commits/PRs, testes realmente executados e por quem, pendências, mudanças locais e próximo passo. Atualizar os parágrafos de estado atual; guardar história nos handoffs, sem acumular próximos passos contraditórios.

Auditoria: docs/handoffs/RETOMADA-2026-09-16.md. Nenhum build/teste foi executado nesta retomada.

## Entregas publicadas desta sessão

| Tarefa | PR draft | Commit de implementação |
| --- | --- | --- |
| TASK-010 | https://github.com/GregoryPina/delamain/pull/2 | `4d32ce9dac04b54d99e5da119fe070c1199d4b32` |
| TASK-011 | https://github.com/GregoryPina/delamain/pull/3 | `7517161e13a30ef7702b3f669ee7aba9ca5164a8` |
| TASK-012 | https://github.com/GregoryPina/delamain/pull/4 | `7d63e605cc3ad36cc3ded50bd62dfcdff449c207` |

O último commit acima é a baseline de código conjunta; este registro posterior é apenas documental. Nenhum merge/build/teste executado. Ao receber teste, registrar o HEAD efetivamente usado e comparar eventual delta com essa baseline. Não reutilizar os relatos antigos para encerrar as três tarefas.

## Próximas tarefas detalhadas

O proprietário pediu planejamento das TASK-013–022; documentos em tasks/NEXT_TASKS.md com dependências, protocolo de despacho e saída obrigatória. Todas são planejadas, não despachadas. A próxima é TASK-013, depois de receber testes de 010–012. Não usar hashes deste planejamento como base futura: publicar/conferir baseline integrada no despacho.

Sequência: validar/corrigir → voz persistente → sessão/rosto real → controles fora do DEV → interrupção/modo texto → personalidade → áudio/rotas → IA fake → provider real condicionado a decisão. Wake word tem pesquisa separada na 022, sem autorização para captura contínua. A autorização atual é para documentar, não implementar essas tarefas. Processo normal retomado pelo proprietário: testes com ele, coordenação Astra, implementação por executor externo quando despachada.
