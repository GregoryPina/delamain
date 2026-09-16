# TASK-016 — interação de uso normal, fora do DEV

STATUS: IMPLEMENTADA_AGUARDANDO_TESTE. Base: `codex/task-015-session-state` (`fd2b3d7`). TESTES EXECUTADOS: nenhum (proprietário acumulará validação em massa).

## Implementação

- `VoiceInteractionSession` única no nível do app: motor local, TTS, STT e preferência de voz compartilhados entre a UI principal e o painel DEV.
- `UserInteractionControls` na tela principal: OUVIR/CANCELAR, PARAR durante fala, texto recolhível por padrão, mensagens curtas via `strings.xml` (sem IDs técnicos na release).
- `RECORD_AUDIO` e query `RecognitionService` movidos para o manifesto `main`; debug manifest vazio.
- Release mantém stub do DEV; debug preserva diagnóstico (trocar voz, intent no painel).
- Toque no rosto não inicia captura. Mesmo `InteractionCoordinator` da 015.

## Arquivos principais

- `ui/VoiceInteractionSession.kt`
- `ui/UserInteractionControls.kt`
- `ui/DelamainApp.kt`
- `ui/DebugCommandPanel.kt` (debug/release)
- `AndroidManifest.xml` (main/debug)
- `res/values/strings.xml`

## Roteiro do proprietário (acumulado)

```powershell
$env:JAVA_HOME='C:\Users\Gregory\.jdks\jbr-21.0.11'
git checkout codex/task-016-user-controls
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease
```

Debug e release:
1. Abrir app: sem pedido de permissão nem escuta automática.
2. OUVIR: pede permissão se necessário; negar mantém texto; conceder exige novo toque.
3. Falar comando; resposta falada e rosto acompanha.
4. PARAR durante fala sem abrir DEV.
5. TEXTO: enviar comando sem microfone.
6. Debug: DEV ainda funciona e não destrói sessão ao fechar.
7. Release: sem botão DEV; sem mensagens técnicas de estado.

## Pendências

Validação manual em massa pelo proprietário. Sem merge em `main` até aceite. TASK-017 implementada em branch derivada (`codex/task-017-mute-controls`); ver `docs/handoffs/TASK-017.md`.
