# Tech Lead handoff

## Baseline

Repositório: GregoryPina/delamain. Branch: main.
Baseline auditada em 2026-09-16: `80b48c396d5882d2d3e1cd2e0f8f918a34842a26`, sincronizada com origin/main na retomada. O código TTS entrou em `add292a`; commits seguintes até esta baseline documentam fechamento. Este handoff é atualização documental posterior, não nova validação de código.

## Objetivo e estado

VEXA já responde por texto e fala no painel DEV. TASK-004 a TASK-009 integradas; não refazer nem reenviar. Apps, volume, hora, mídia, bateria, variantes e TTS têm aceite histórico do proprietário; detalhes em docs/PROJECT_STATUS.md e docs/handoffs/TASK-006.md a TASK-009.md. Não inventar logs, aparelho ou cobertura a partir de “teste ok”.

## Próxima ação

Preparar despacho externo da TASK-010 (estabilização TTS), usando hash completo publicado que contenha a especificação. A tarefa está planejada, não despachada. Antes de iniciar, conferir Git e possíveis novos relatos do proprietário. Não usar automaticamente a baseline desta auditoria para um despacho posterior.

Depois: TASK-011, STT por botão para uma frase, dependente do fechamento da TASK-010. Personalização da voz é preferência do proprietário e merece recorte separado. Wake word, IA, play/pause e volume percentual continuam futuros.

## Achados e limites

- TTS: `Spoken` indica aceitação na fila; callbacks atuais de início/término/erro não atualizam estado. Revisar inicialização tardia após descarte, cancelamento e voz disponível/offline.
- TASK-009 foi aceita: não reinterpretar estes riscos de revisão estática como falha reproduzida no aparelho.
- LocalCommandEngine é canônico; CompositeLocalActionPort reúne adapters ativos. Não ativar domain/command/CommandRouter ou platform/AndroidCommandExecutor.
- Mídia confirma despacho, não efeito no player. Unknown não dispara IA.
- Release não tem painel DEV; integração com estados faciais não está concluída.

## Responsáveis e validação

Nenhum executor ativo. Astra coordena e integra; executores externos recebem TASK + BASE COMMIT exato, entregam branch/PR ou patch. Nunca main/merge pelo executor. Não criar/reativar agentes internos. Proprietário executa builds/testes; coordenador faz revisão estática e registra a origem de cada evidência.

## Trabalho local preservado

`Sem_titulo_bordas_arredondadas.stl` estava não rastreado na retomada. Não incluir, apagar ou alterar sem tarefa correspondente. Não descartar outras mudanças que apareçam após este registro.

## Quando substituir ou retornar

Ler AGENTS.md, README, arquitetura, roadmap, estado, decisões e docs/DEVELOPMENT.md. Ler TEMP_LEAD_HANDOFF.md se existir; ele não existia nesta retomada. Registrar nele baseline inicial/final completas, tarefas, commits/PRs, testes realmente executados e por quem, pendências, mudanças locais e próximo passo. Atualizar os parágrafos de estado atual; guardar história nos handoffs, sem acumular próximos passos contraditórios.

Auditoria: docs/handoffs/RETOMADA-2026-09-16.md. Nenhum build/teste foi executado nesta retomada.
