# TASK-009 — TTS com TextToSpeech

Status: concluída e integrada em `main`. Proprietário confirmou áudio TTS (“funcionou, ela falou”). Voz do sistema a personalizar depois.
Branch: `codex/tts-voice-output` (merge em `main`).
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

## Validação do proprietário

- Relato: “funcionou, ela falou”.
- Voz atual (engine padrão do sistema, `pt-BR`) **não agradou** — trocar em etapa futura (ver pendência abaixo).

## Pendência futura (não bloqueia merge)

Personalização de voz: escolher engine/voz instalada no aparelho, ajustar pitch/rate, ou avaliar pacote de voz dedicado. Hoje `AndroidTextToSpeechPort` usa o TTS padrão do Android sem seleção de voz.
