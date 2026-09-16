# TASK-009 — TTS com TextToSpeech do Android

## Objective

Primeira entrega de V0.3: falar as respostas do `LocalCommandEngine` usando `android.speech.tts.TextToSpeech`, sem STT, sem wake word e sem IA.

## Context (ADR-005)

- Não implementar síntese própria.
- Domínio permanece independente de Android via `SpeechOutputPort`.
- Fluxo: texto reconhecido/processado → `LocalCommandEngine` → resposta → TTS.
- Não conectar `CommandRouter` remoto.

## Desired behavior

No painel DEV, após enviar um comando:

1. O motor processa o texto como hoje.
2. A resposta exibida é falada pelo TTS em português brasileiro (`pt-BR` quando disponível).
3. `Unknown` também pode ser falado com variantes locais (≥3).
4. Falha/indisponibilidade de TTS não altera o resultado do motor; apenas não há áudio.

## Relevant files

- `domain/SpeechOutputPort.kt` (novo)
- `domain/LocalUnknownResponses.kt` (novo, ≥3 variantes para Unknown)
- `integration/voice/AndroidTextToSpeechPort.kt` (novo)
- `ui/DebugCommandPanel.kt` (debug)
- `docs/handoffs/TASK-009.md`

Não alterar `LocalCommandEngine` além do estritamente necessário. Não implementar STT, wake word, estados visuais SPEAKING na tela principal, nem IA.

## Requirements

1. `SpeechOutputPort` com `speak(text)`, `stop()` e `shutdown()`; resultado tipado (`Spoken`, `Unavailable`, `Failed`).
2. Adapter Android inicializa `TextToSpeech` de forma assíncrona; idioma `pt-BR` com fallback para locale padrão do dispositivo.
3. `DisposableEffect` no painel DEV chama `shutdown()` ao sair.
4. Falar somente o texto da resposta (sem prefixo `intent:` no áudio).
5. Preservar comportamento digitado existente e todos os testes do motor.

## Constraints

- Sem `RECORD_AUDIO` nesta TASK (TTS não exige).
- Sem rede obrigatória para TTS.
- Release continua sem painel DEV funcional.

## Dependencies

- TASK-008 integrada em `main` (`67dfc7c` ou posterior).

## Acceptance criteria

- [ ] Comando reconhecido é falado no aparelho.
- [ ] `Unknown` usa variante rotativa (≥3).
- [ ] TTS indisponível não causa crash; painel mostra texto normalmente.
- [ ] `testDebugUnitTest` verde.
- [ ] Roteador remoto intocado.

## Validation

Proprietário: `assembleDebug`, painel DEV, testar “Vexa, que horas são?” e comando desconhecido; confirmar áudio e texto.
