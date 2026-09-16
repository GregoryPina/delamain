# Matriz de rotas de áudio — TASK-019

Preencher pelo proprietário no aparelho alvo (Redmi 13 previsto). Valores válidos: OK, FALHA, NÃO DISPONÍVEL, NÃO TESTADO.

| Cenário | Fala (TTS) | Escuta (STT) | Notas |
| --- | --- | --- | --- |
| Alto-falante interno | NÃO TESTADO | NÃO TESTADO | |
| Fone com fio | NÃO TESTADO | NÃO TESTADO | |
| Bluetooth (se disponível) | NÃO TESTADO | NÃO TESTADO | Não marcar suportado sem teste |
| Sem player ativo | NÃO TESTADO | NÃO TESTADO | |
| Player tocando | NÃO TESTADO | NÃO TESTADO | Verificar se música continua/pausa |
| Perda de foco (outro app) | NÃO TESTADO | NÃO TESTADO | VEXA deve parar e não retomar |
| App em segundo plano | NÃO TESTADO | NÃO TESTADO | |
| Desconexão de rota durante fala | NÃO TESTADO | NÃO TESTADO | Ex.: tirar fone |
| Desconexão de rota durante escuta | NÃO TESTADO | NÃO TESTADO | |

## Política implementada (código)

- Foco transitório (`AUDIOFOCUS_GAIN_TRANSIENT`) via `AudioFocusRequest` (API 26+, targetSdk 35).
- TTS: `USAGE_ASSISTANT` + `CONTENT_TYPE_SPEECH`.
- STT: `USAGE_VOICE_COMMUNICATION` + `CONTENT_TYPE_SPEECH`.
- Aquisição somente durante fala/escuta explícita; liberação em sucesso, falha, cancelamento, timeout, perda de foco, mudança de rota e shutdown.
- Perda de foco ou rota alterada cancela fala/escuta ativa; **sem** retomada automática.
- Foco negado → mensagem visual; resposta permanece em texto.
- Sem SCO/A2DP forçado, sem permissões Bluetooth novas, sem serviço em foreground.

Fonte consultada na implementação: [Android — foco de áudio](https://developer.android.com/media/optimize/audio-focus).
