# TASK-015 — sessão única e rosto orientado por eventos reais

STATUS: IMPLEMENTADA_AGUARDANDO_TESTE. Base: HEAD corrigido da TASK-014 `8f7da528c6ad36af15e1d0640f7b1acbca968ab2`. Nenhum build/teste executado.

## Implementação
`InteractionCoordinator` mantém snapshot imutável (`face`, HUD, interactionId) e resolve prioridade entre as máquinas existentes de STT/TTS sem substituí-las. Boot só termina se nenhuma interação começou. Cada nova ação incrementa ID; callback/timer de ID antigo é descartado. `stop/shutdown` invalidam callbacks. Estados: Starting→IDLE/PREPARING, Listening→LISTENING, Processing→THINKING, Queued→IDLE/VOICE QUEUED, Speaking→SPEAKING, terminal/cancel→IDLE, falhas→ERROR.

`DelamainApp` observa o snapshot e mantém os mesmos assets, cores, layout base, scanlines/glitch/flicker. O ciclo manual por toque saiu do caminho normal. O painel DEV recebe a mesma instância do coordinator e conecta callbacks reais de TTS/STT. Antes de escutar, saída é parada; falha de stop não autoriza captura. ON_STOP/descarte cancelam/invalidate sessão. Release continua com stub sem controles/microfone.

Persistência da TASK-014 mantém lifetime próprio: leitura antiga não restaura em painel novo; uma gravação aceita pode concluir após fechar, mas não atualiza UI de sessão errada. O coordinator de interação não duplica store/porta.

## Testes criados
`InteractionCoordinatorTest`: boot versus interação, conclusão TTS antiga versus nova escuta, timer de erro antigo versus pedido novo, Queued não é Speaking, callback após shutdown e stop invalidando callback. Sem sleeps reais.

## Watchdogs
O contrato de ID/timer cancelável foi preparado por `expireError(interactionId)`. O erro visual é invalidável por nova ação. Watchdog TTS de 10s/conclusão proporcional não foi acoplado ao adapter nesta entrega sem relógio injetável; deve ser tratado como pendência de revisão antes de merge, não como sucesso por timeout. TASK-016 não foi iniciada.

## Roteiro conjunto do proprietário
```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease
```
1. Validar persistência da 014: trocar voz, fechar/reabrir DEV, reiniciar, USAR PADRÃO e repetir offline.
2. Boot: abrir app e iniciar ação antes de 2,2s; timer de boot não deve devolver rosto a IDLE durante a interação.
3. OUVIR: PREPARING antes de ready, LISTENING durante captura, THINKING no reconhecimento, depois resposta/TTS.
4. ENVIAR e TESTAR VOZ: Queued não deve mostrar SPEAKING; somente callback onStart muda rosto para SPEAKING; terminal volta IDLE.
5. Durante fala, tocar OUVIR: fala deve parar antes da captura. Cancelar escuta não reinicia áudio.
6. Provocar falha/silêncio e iniciar nova ação: ERROR antigo não deve derrubar o novo LISTENING/THINKING.
7. Sair/reentrar no app e abrir/fechar DEV repetidamente: sem fala/escuta fantasma, sem duplicar ação.
8. Release: confirmar ausência dos controles DEV/microfone.

## Pendências
Validação local acumulada pelo proprietário. Revisar especificamente watchdog configurável exigido pela TASK antes de qualquer merge. Ambas 014/015 permanecem IMPLEMENTADA_AGUARDANDO_TESTE.
