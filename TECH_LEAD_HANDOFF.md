# Tech Lead handoff

## Baseline

Repository: GregoryPina/delamain
Branch: main
Baseline integrada: `323298b` (TASK-006 + TASK-007 em `main`).
HEAD validado pelo proprietário: apps, volume, hora (TASK-006) e faixa anterior/próxima (TASK-007).

## Current objective

Manter VEXA estável e preparar a próxima capacidade local em recorte pequeno.

## Current wave

Wave 4 concluída: TASK-007 integrada em `main` (`323298b`).

## Completed

Wave 4 / TASK-007: faixa anterior e próxima no fluxo local; 37 testes automáticos verdes. Proprietário confirmou “testado ok” no aparelho.

Wave 3 / TASK-006: abertura de apps allowlisted, `<queries>` no manifesto. Proprietário confirmou “teste ok”.

Wave 2: TASK-004 + TASK-005 via PR #1. VEXA ativo; roteador remoto corrigido e inativo.

## In progress

TASK-008 em `codex/system-status-responses`: bateria/status do sistema + respostas variadas para resultados de ação. 42 testes automáticos verdes; aguardando validação no aparelho.

## Waiting / blocked

Nenhum bloqueio desta wave. Rotas adicionais de áudio e diferenças entre aparelhos não foram especificadas no relato.

## Current agent assignments

Nenhum. Próximo executor externo só recebe tarefa com baseline exata. Branch/PR próprio se possuir escrita; nunca main ou merge por conta própria.

## Important decisions

Work exclusivo do Astra, sem agentes internos. Builds/testes locais pelo proprietário. LocalCommandEngine é canônico; contribuição remota continua fora da UI. VEXA é nome e gatilho; repositório/applicationId/pacotes permanecem delamain.

## Open decisions

Play/pause como toggle e volume percentual permanecem futuros. TTS/STT é a próxima fase do roadmap (V0.3).

## Known problems

Executor remoto ainda sem confirmação robusta e pausa como toggle; não ativá-lo. Catálogo de frases fechado. Não há voz, STT ou IA.

## Next recommended action

Preparar próxima TASK de V0.3 (TTS) ou recorte local pendente (status do sistema), conforme prioridade do proprietário. Não ativar roteador remoto nem play/pause como toggle.

## Warnings

Não criar agentes internos nem executar testes automaticamente. Não reenviar TASK-004/005: concluídas. Não renomear identificadores técnicos. Ler TEMP_LEAD_HANDOFF.md se existir.
