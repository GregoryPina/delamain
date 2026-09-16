# TASK-015 — sessão única e rosto orientado por eventos reais

Status: PLANEJADA, NÃO DESPACHADA. Depende de TASK-013/014 integradas.
BASE COMMIT: fornecido no despacho. Aplicar [protocolo](NEXT_TASKS.md) e ler DESIGN.

## Problema concreto

`DelamainApp.kt` tem `UiState` privado e alternância de estados por toque; `DebugCommandPanel` possui instâncias/estado próprios de comandos, TTS e STT. O rosto não representa a sessão real. `DelamainAppFixed.kt` é apenas placeholder, não uma segunda implementação a desenvolver.

## Resultado

Criar um coordenador pequeno de interação, independente dos desenhos/efeitos, com uma única instância de LocalCommandEngine e cada porta de voz. UI observa snapshot imutável e envia eventos explícitos. Não substituir SpeechInputSession/SpeechOutputSession por outra máquina duplicada: elas continuam protegendo seus próprios pedidos; coordenador resolve prioridade entre subsistemas.

## Estado e prioridade propostos

| Evento real | Rosto/HUD | Observação |
| --- | --- | --- |
| Boot ativo, sem interação iniciada | BOOT | Término do timer não sobrescreve interação posterior |
| STT Starting | IDLE + preparando | Não afirmar captura antes de onReady |
| STT Listening | LISTENING | Prioridade sobre callback antigo de TTS |
| STT Processing | THINKING | Apenas até resultado/erro/timeout |
| TTS Queued | IDLE + aguardando voz | Aceitação não é áudio |
| TTS Speaking | SPEAKING | Vinculado ao pedido ativo |
| Conclusão/cancelamento | IDLE | Não reproduzir nem escutar de novo |
| Falha recuperável | ERROR breve + mensagem | Default proposto: 3s, ou até nova ação; texto permanece |

Usar geração/ID de interação para descartar eventos, inclusive timers de erro. Um timer de ERROR antigo não pode derrubar LISTENING novo. Unknown é resposta local normal, não erro de infraestrutura.

## Implementação em passos

1. Extrair propriedade das instâncias e callbacks do painel, mantendo UI/efeitos existentes. Injetar portas em coordenador testável; nada de singleton global mutável.
2. Cancelar entrada antes de enviar texto/testar voz; parar saída antes de escutar. Falha de stop não autoriza captura.
3. Lifetimes de Activity/composição claros: ON_STOP cancela, descarte libera, recomposição não recria motores. Sem retenção de Activity por engine em applicationContext.
4. Remover ciclo manual de estados do caminho normal; preservar eventual demonstração apenas no DEV e nunca enquanto sessão real estiver ativa. Toque no rosto não inicia microfone implicitamente.
5. Tratar TTS que não inicializa ou não envia evento terminal: watchdog cancelável, com tempo configurável para teste. Default proposto 10s para inicializar/iniciar e limite de conclusão proporcional ao texto, nunca concluir com sucesso por timeout. Definir fórmula/limite no PR; não cortar fala legítima curta por timer arbitrário.
6. Manter release sem controles funcionais até TASK-016; esta tarefa não promove permissão de microfone.

## Arquivos e testes

Permitidos: `DelamainApp.kt`, painel debug/stub release, coordenador novo, contratos de estado se necessário, adaptação pontual dos adapters e testes. Não editar assets, GlitchFace, cores, fullscreen ou orientação.

Testes com relógio/fakes: boot termina durante interação; fala antiga conclui após nova escuta; erro antigo expira durante novo pedido; pausa/retorno não retoma áudio; recomposição não duplica ação; callbacks após shutdown; watchdog invalida apenas pedido correto. Não usar sleeps reais em testes. Proprietário verifica rosto e HUD com texto, voz, silêncio, cancelamento e saída do app.

## Aceite e handoff

Rosto acompanha eventos sem inventar estado, uma ação por entrada, sem timers/callbacks escapando do descarte. Registrar propriedade das instâncias, mapa de estados e contratos alterados; explicar como TASK-016 reutiliza o coordenador sem criar segundo motor.
