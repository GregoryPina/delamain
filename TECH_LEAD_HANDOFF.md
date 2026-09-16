# Tech Lead handoff

## Baseline

Repository: GregoryPina/delamain
Branch: main
Baseline integrada: `cf67638` (TASK-006 a TASK-008 em `main`).
HEAD validado pelo proprietário: apps, volume, hora, mídia, bateria e respostas variadas (TASK-006–008).

## Current objective

Manter VEXA estável e preparar a próxima capacidade local em recorte pequeno.

## Current wave

Wave 6 concluída: TASK-009 (TTS) integrada em `main`. Proprietário confirmou áudio; voz do sistema a personalizar depois.

## Completed

Wave 6 / TASK-009: TTS no painel DEV via `SpeechOutputPort`; ADR-005. Proprietário: “funcionou, ela falou”; voz padrão a trocar depois.

Wave 5 / TASK-008: status da bateria, respostas variadas (≥3) em todo o fluxo local; 42 testes verdes. Proprietário confirmou “testado”.

Wave 4 / TASK-007: faixa anterior/próxima. Proprietário: “testado ok”.

Wave 3 / TASK-006: abertura de apps. Proprietário: “teste ok”.

Wave 2: TASK-004 + TASK-005 via PR #1. VEXA ativo; roteador remoto inativo.

## In progress

Nenhuma implementação ativa. Próximo recorte: TASK-010 (STT) ou personalização de voz TTS (feedback do proprietário).

## Waiting / blocked

Nenhum bloqueio desta wave. Rotas adicionais de áudio e diferenças entre aparelhos não foram especificadas no relato.

## Current agent assignments

Nenhum. Próximo executor externo só recebe tarefa com baseline exata. Branch/PR próprio se possuir escrita; nunca main ou merge por conta própria.

## Important decisions

Work exclusivo do Astra, sem agentes internos. Builds/testes locais pelo proprietário. LocalCommandEngine é canônico; contribuição remota continua fora da UI. VEXA é nome e gatilho; repositório/applicationId/pacotes permanecem delamain.

## Open decisions

Play/pause como toggle e volume percentual permanecem futuros. TTS baseline validado; STT é próximo passo V0.3. Voz TTS: trocar engine/tom em etapa futura (proprietário não gostou da voz padrão).

## Known problems

Executor remoto ainda sem confirmação robusta e pausa como toggle; não ativá-lo. Catálogo de frases fechado. TTS baseline no painel DEV; qualidade da voz pendente. Sem STT, wake word ou IA.

## Next recommended action

Preparar TASK-010 (STT com SpeechRecognizer). Personalização de voz TTS pode entrar como TASK separada. Não ativar roteador remoto nem play/pause como toggle.

## Warnings

Não criar agentes internos nem executar testes automaticamente. Não reenviar TASK-004/005: concluídas. Não renomear identificadores técnicos. Ler TEMP_LEAD_HANDOFF.md se existir.
