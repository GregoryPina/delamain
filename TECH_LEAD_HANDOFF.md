# Tech Lead handoff

## Baseline

Repositório: GregoryPina/delamain. Branch de trabalho: `codex/task-011-push-to-talk`, baseada em `4d32ce9dac04b54d99e5da119fe070c1199d4b32` da TASK-010 (PR #2). Main permanece sem ambas.
Baseline auditada em 2026-09-16: `80b48c396d5882d2d3e1cd2e0f8f918a34842a26`, sincronizada com origin/main na retomada. O código TTS entrou em `add292a`; commits seguintes até esta baseline documentam fechamento. A implementação TASK-010 partiu de `02f65daf210e1b569c821e66085a9a9c9fa96e21`; nenhum build/teste novo foi executado.

## Objetivo e estado

VEXA já responde por texto e fala no painel DEV. TASK-004 a TASK-009 integradas; não refazer nem reenviar. Apps, volume, hora, mídia, bateria, variantes e TTS têm aceite histórico do proprietário; detalhes em docs/PROJECT_STATUS.md e docs/handoffs/TASK-006.md a TASK-009.md. Não inventar logs, aparelho ou cobertura a partir de “teste ok”.

## Próxima ação

Receber validação conjunta do proprietário para TASK-010 e TASK-011, implementadas excepcionalmente pelo Astra em branches dependentes. Roteiro e contratos em docs/handoffs/TASK-010.md. Status IMPLEMENTADA_AGUARDANDO_TESTE; 12 testes novos não executados. Revisar o HEAD testado antes de integrar; não reenviar implementação a outro executor.

TASK-011 já implementada por autorização de continuar nesta sessão: escuta local por botão, API31+, sem fallback remoto, timeout e resultados únicos. Roteiro em docs/handoffs/TASK-011.md; dez testes novos não executados. Fechar TASK-010 antes de integrar TASK-011. Personalização da voz é preferência do proprietário e merece recorte separado. Wake word, IA, play/pause e volume percentual continuam futuros.

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
