# TASK-007 — faixa anterior e próxima

Status: implementado, aguardando teste do proprietário. `testDebugUnitTest`: 37 testes, 0 falhas.
Base: `8bd5d7f` (main pós-TASK-006).
Branch: `codex/media-track-local`.

## Resumo

`LocalAction` ganhou `MediaNext` e `MediaPrevious`. O motor reconhece frases fechadas em português e `AndroidMediaKeyActionPort` envia `KEYCODE_MEDIA_NEXT` / `KEYCODE_MEDIA_PREVIOUS` via `AudioManager.dispatchMediaKeyEvent`. Resultado `Dispatched` confirma apenas o envio da tecla, sem alegar troca de faixa. Play/pause não foi implementado.

## Catálogo (após normalização)

Próxima: `proxima musica`, `proxima faixa`, `proxima`.
Anterior: `musica anterior`, `faixa anterior`, `anterior`.

## Arquivos

- `domain/LocalActionPort.kt`, `LocalIntent.kt`, `LocalCommandEngine.kt`
- `integration/audio/AndroidMediaKeyActionPort.kt` (novo)
- `integration/CompositeLocalActionPort.kt`
- `ui/DebugCommandPanel.kt`
- `domain/LocalCommandEngineTest.kt`

## Roteiro para o proprietário

Com Spotify/YouTube Music em reprodução, no painel DEV:

1. `Vexa, próxima música`
2. `faixa anterior`
3. `não pule a música` → não reconhecido
4. Volume, apps e hora continuam funcionando

```powershell
$env:JAVA_HOME='C:\Users\Gregory\.jdks\jbr-21.0.11'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```
