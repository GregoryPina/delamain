# Tech Lead handoff

## Baseline

Repository: GregoryPina/delamain
Branch: main
Baseline integrada: 85d249df33d5722cec74532bcee8dd6a58d2b3af (merge PR #1).
HEAD validado pelo proprietário: 39d9fce03943760eb722bd31b557596653a15798.
Commits posteriores apenas de documentação não alteram esse código validado.

## Current objective

Manter VEXA estável e preparar a próxima capacidade local em recorte pequeno.

## Current wave

Wave 3 preparada: TASK-006 (abertura de aplicativo no fluxo local ativo). Despacho em `tasks/WAVE-003-DISPATCH.md`.

## Completed

Wave 2: TASK-004 + TASK-005 integradas pelo PR #1. Roteador remoto passou a reconhecer frases completas; correção de pontuação numérica revisada. Nome exibido e gatilho VEXA implementados; identificadores técnicos preservados. Proprietário respondeu “teste ok” ao roteiro de build e verificações manuais do HEAD indicado. Não forneceu logs, contagem de testes ou detalhes do aparelho; não inferir esses dados. Astra não repetiu testes.

## In progress

TASK-006 commitada em `codex/open-app-local` (`0260d6b`), publicada em `origin`. PR pendente de abertura/merge: https://github.com/GregoryPina/delamain/compare/main...codex/open-app-local Proprietário validou YouTube no aparelho. Demais apps da allowlist e `testDebugUnitTest` ainda pendentes. TASK-007 (mídia faixa anterior/próxima) redigida; aguarda merge da Wave 3.

## Waiting / blocked

Nenhum bloqueio desta wave. Rotas adicionais de áudio e diferenças entre aparelhos não foram especificadas no relato.

## Current agent assignments

Nenhum. Próximo executor externo só recebe tarefa com baseline exata. Branch/PR próprio se possuir escrita; nunca main ou merge por conta própria.

## Important decisions

Work exclusivo do Astra, sem agentes internos. Builds/testes locais pelo proprietário. LocalCommandEngine é canônico; contribuição remota continua fora da UI. VEXA é nome e gatilho; repositório/applicationId/pacotes permanecem delamain.

## Open decisions

Próximo recorte recomendado: abertura de um aplicativo permitido, com resultado tipado, validação de disponibilidade e falha. Preparar TASK antes de implementar. Mídia e volume percentual permanecem futuros.

## Known problems

Executor remoto ainda sem confirmação robusta e pausa como toggle; não ativá-lo. Catálogo de frases fechado. Não há voz, STT ou IA.

## Next recommended action

1. Abrir/mergear PR de TASK-006 (`codex/open-app-local` → `main`).
2. Executar `testDebugUnitTest` e validar demais apps da allowlist.
3. Despachar TASK-007 (faixa anterior/próxima) após baseline publicada. Não ativar roteador remoto nem play/pause como toggle nesta etapa.

## Warnings

Não criar agentes internos nem executar testes automaticamente. Não reenviar TASK-004/005: concluídas. Não renomear identificadores técnicos. Ler TEMP_LEAD_HANDOFF.md se existir.
