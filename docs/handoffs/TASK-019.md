# TASK-019 — handoff

Status: **implementado, aguardando teste do proprietário no aparelho**.

Branch: `codex/task-019-audio-focus` (base: `codex/task-018-personality`).

## Entregue

### Foco de áudio

- `AudioFocusPort` + `AudioFocusSession` (domínio)
- `AndroidAudioFocusPort` — `AudioFocusRequest` com foco transitório (targetSdk 35, minSdk 26)
- Integração em `VoiceInteractionSession`: aquisição antes de TTS/STT; liberação em estados terminais, cancelamento, perda de foco e shutdown

### Monitor de rota

- `AndroidAudioRouteMonitor` — `ACTION_AUDIO_BECOMING_NOISY` + remoção de dispositivo roteável
- Interrupção sem reexecutar comando nem reiniciar captura em background

### Mensagens

- Áudio indisponível (foco negado)
- Foco perdido para outro app
- Rota alterada

## Matriz de validação

Ver [`TASK-019-AUDIO-MATRIX.md`](TASK-019-AUDIO-MATRIX.md) — preencher no Redmi 13 (ou aparelho disponível).

## Testes

`AudioFocusSessionTest` (fake de foco: concedido/negado/perda/abandon tardio).

Executados: `testDebugUnitTest`, `assembleDebug`, `assembleRelease` — verdes.

## Roteiro manual

1. Com música tocando, pedir resposta por voz — verificar comportamento do player e mensagem se foco negado.
2. Durante fala, abrir app que rouba áudio — VEXA para, não retoma.
3. Durante escuta, desconectar fone/BT — interação cancela, sem nova escuta automática.
4. App em segundo plano durante fala — registrar na matriz.
5. Preencher matriz com OK/FALHA/NÃO TESTADO por rota.

## Build

```powershell
$env:JAVA_HOME='C:\Users\Gregory\.jdks\jbr-21.0.11'
.\gradlew.bat assembleDebug
```
