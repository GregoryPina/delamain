# TASK-017 — handoff

Status: **implementado, aguardando teste do proprietário**.

Branch: `codex/task-017-mute-controls` (`d1d6d99`). Base: `codex/task-016-user-controls` (`72b32ce`).

## Entregue

### Controles prioritários (voz e texto)

`InteractionControlRecognizer` intercepta antes do `LocalCommandEngine`, com correspondência **exata** após normalização e prefixo opcional `Vexa`:

| Frase | Efeito |
| --- | --- |
| pare de falar / parar de falar | Para TTS atual; sem confirmação falada |
| cancelar / cancela | Cancela STT/TTS pendente; mensagem visual |
| modo mute / silenciar voz / desativar voz | Ativa mute + para fala atual |
| ativar voz / sair do mute / respostas por voz | Desativa mute |

Negações e frases compostas não casam por substring.

### Modo mute

- Estado `voiceMuted` na sessão compartilhada.
- Persistido em `vexa_preferences` (`voice_muted`), independente das chaves de voz.
- Restaurado na abertura da sessão via `SpeechVoicePreferenceCoordinator.restoreMute`.
- Com mute ativo: comandos normais mostram resposta na tela, TTS automático suprimido.
- `speakSample()` (TESTAR VOZ no DEV) continua falando — prévia explícita.
- Botão **MUTE** / **VOZ** na tela principal; mesma semântica das frases.

### Arquivos principais

- `domain/CommandInputNormalizer.kt` — normalização compartilhada
- `domain/InteractionControlIntent.kt` — reconhecedor de controles
- `domain/SpeechVoicePreference.kt` — mute no store/coordinator
- `integration/voice/DataStoreSpeechVoicePreferenceStore.kt` — chave `voice_muted`
- `ui/VoiceInteractionSession.kt` — precedência, mute, persistência
- `ui/UserInteractionControls.kt` — toggle mute
- `res/values/strings.xml` — strings MUTE/VOZ e cancelamento

## Testes

`InteractionControlRecognizerTest`, `SpeechVoicePreferenceCoordinatorTest` (mute), `DataStoreSpeechVoicePreferenceStoreTest` (mute key).

Executados pelo coordenador nesta rodada: `testDebugUnitTest` (100 testes), `assembleDebug`, `assembleRelease` — todos verdes.

## Roteiro de validação manual

1. Com voz ligada, enviar comando — VEXA fala e mostra texto.
2. Durante fala, tocar **PARAR** ou digitar “pare de falar” — fala para, sem nova fala.
3. Digitar “cancelar” durante escuta — escuta cancela, mensagem “Interação cancelada.”
4. Ativar **MUTE** (botão ou frase) — próximas respostas só na tela.
5. **TESTAR VOZ** no painel DEV com mute ativo — ainda fala.
6. **VOZ** ou “ativar voz” — TTS automático volta; não repete histórico.
7. Reiniciar app — mute restaurado.

## Build

```powershell
$env:JAVA_HOME='C:\Users\Gregory\.jdks\jbr-21.0.11'
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease
```
