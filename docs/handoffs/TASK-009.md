# TASK-009 — TTS com TextToSpeech

Status: implementado, aguardando teste do proprietário.
Branch: `codex/tts-voice-output`.
ADR: `docs/DECISIONS.md` (ADR-005).

## Resumo

`SpeechOutputPort` desacopla a saída de voz. `AndroidTextToSpeechPort` usa `TextToSpeech` com `pt-BR` quando disponível. O painel DEV fala a resposta do motor (sem prefixo de intent no áudio). `LocalUnknownResponses` fornece ≥3 variantes para `Unknown`.

## Arquivos

- `domain/SpeechOutputPort.kt`
- `domain/LocalUnknownResponses.kt`
- `integration/voice/AndroidTextToSpeechPort.kt`
- `ui/DebugCommandPanel.kt`
- `domain/LocalUnknownResponsesTest.kt`

## Roteiro

1. `assembleDebug`, abrir painel DEV.
2. Aguardar ~1s para TTS inicializar.
3. `Vexa, que horas são?` → ouvir hora falada.
4. Comando inválido repetido → ouvir variantes de desconhecido.
