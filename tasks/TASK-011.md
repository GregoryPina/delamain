# TASK-011 — escuta local por botão

Status: IMPLEMENTADA_AGUARDANDO_TESTE, excepcionalmente pelo Astra, conforme autorização de continuar em 2026-09-16. Método habitual de executores externos permanece para próximas sessões.
BASE COMMIT: `4d32ce9dac04b54d99e5da119fe070c1199d4b32` (TASK-010, PR #2, ainda aguardando teste). Branch: `codex/task-011-push-to-talk`, dependente da TASK-010, sem merge antes de validação de ambas.

## Recorte

Botão OUVIR no painel DEV, uma frase por ativação. SpeechInputPort independente de Android; adapter SpeechRecognizer exclusivamente on-device disponível no Android 12/API31+. Android 26–30 ou aparelho sem serviço local mantém digitação e informa indisponibilidade. Não oferecer fallback remoto nesta entrega.

Permissão RECORD_AUDIO só ao tocar; conceder não inicia captura automaticamente, exige novo toque. Parar TTS antes de escutar; recusar escuta se stop falhar. Cancelar ao fechar, sair, digitar/enviar ou descartar. Não recomeçar automaticamente. Resultado final não vazio entra uma única vez no motor canônico; parciais, resultados antigos/cancelados e erros não executam ações. Timeout total de 15 segundos. Sem gravação persistida, wake word, serviço em background ou IA.

## Aceite

Testes de sessão para entrega única, callbacks antigos, cancelamento, erro/timeout, permissão e falha de stop. Criar sem executar; proprietário testa amanhã. Roteiro deve cobrir offline, permissão negada, aparelho sem serviço, silêncio, cancelar/repetir, comandos com ação e coexistência TTS/STT. Atualizar handoff e estado. Release não inclui painel nem permissão de microfone nesta etapa.

Entrega e roteiro: [handoff TASK-011](../docs/handoffs/TASK-011.md). Dez testes criados, nenhum executado.
