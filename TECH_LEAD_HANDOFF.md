# Tech Lead handoff

## Baseline

Repository: GregoryPina/delamain
Branch: main
Baseline integrada: `cf67638` (TASK-006 a TASK-008 em `main`).
HEAD validado pelo proprietário: apps, volume, hora, mídia, bateria e respostas variadas (TASK-006–008).

## Current objective

Manter VEXA estável e preparar a próxima capacidade local em recorte pequeno.

## Current wave

Wave 6 em andamento: TASK-009 (TTS) em `codex/tts-voice-output`. ADR-005 registrado.

## Completed

Wave 5 / TASK-008: status da bateria, respostas variadas (≥3) em todo o fluxo local; 42 testes verdes. Proprietário confirmou “testado”.

Wave 4 / TASK-007: faixa anterior/próxima. Proprietário: “testado ok”.

Wave 3 / TASK-006: abertura de apps. Proprietário: “teste ok”.

Wave 2: TASK-004 + TASK-005 via PR #1. VEXA ativo; roteador remoto inativo.

## In progress

TASK-009: TTS via `TextToSpeech` no painel DEV; STT e wake word ficam para TASK-010+. ADR-005 formaliza SpeechRecognizer + LocalCommandEngine + TTS, sem Google Assistente.

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

Validar TASK-009 no aparelho (áudio TTS). Depois preparar TASK-010 (STT com SpeechRecognizer). Não ativar roteador remoto nem play/pause como toggle.

## Warnings

Não criar agentes internos nem executar testes automaticamente. Não reenviar TASK-004/005: concluídas. Não renomear identificadores técnicos. Ler TEMP_LEAD_HANDOFF.md se existir.
