# TASK-015 — sessão única e rosto orientado por eventos reais

STATUS: IMPLEMENTADA_AGUARDANDO_TESTE. Base TASK-014 corrigida: `8f7da528c6ad36af15e1d0640f7b1acbca968ab2`. Revisão e correções: `docs/handoffs/PR-006-REVIEW.md`.

## Implementação

`InteractionCoordinator` mantém snapshot imutável (`face`, HUD, interactionId) e resolve prioridade entre STT/TTS sem duplicar suas máquinas. Boot só termina se nenhuma interação começou. Cada nova ação incrementa ID; callback/timer de ID antigo é descartado.

`DelamainApp` observa o snapshot; painel DEV compartilha o coordinator. TTS/STT ligam eventos com **ID da interação que originou o pedido** (não o ID corrente do painel). `SpeechOutputWatchdog` cobre fila (10s) e fala (limite proporcional ao texto); timeout nunca reporta sucesso.

Persistência da TASK-014 mantém lifetime próprio; coordinator não duplica store/porta.

## Testes executados (coordenador, esta rodada)

```powershell
$env:JAVA_HOME='C:\Users\Gregory\.jdks\jbr-21.0.11'
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease
```

BUILD SUCCESSFUL; suite unitária verde (inclui novos testes de watchdog, interactionId e coordinator).

## Roteiro conjunto do proprietário

Mesmo comando acima para reproduzir build local.

1. Persistência 014: trocar voz, fechar/reabrir DEV, reiniciar, USAR PADRÃO, offline.
2. Boot: ação antes de 2,2s; timer de boot não devolve IDLE durante interação.
3. OUVIR → PREPARING/LISTENING/THINKING; resposta falada depois.
4. ENVIAR/TESTAR VOZ: Queued ≠ SPEAKING; SPEAKING só após onStart; terminal → IDLE.
5. Durante fala, OUVIR: fala para antes da captura.
6. Nova ação após ERROR antigo: rosto não regride.
7. Abrir/fechar DEV e sair do app: sem fantasma.
8. Release: sem controles DEV/microfone.

## Pendências

Aceite funcional do proprietário. Sem merge em `main` até confirmação. Registro histórico desta entrega; TASK-016–019 implementadas posteriormente. Estado atual em docs/PROJECT_STATUS.md.
