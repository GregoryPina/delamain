# TASK-010 — estabilizar a saída de voz

Status: CONCLUÍDA e integrada em main após aceite funcional do proprietário; ver [TASK-013](../docs/handoffs/TASK-013.md). As seções seguintes preservam a especificação e baseline original.
BASE COMMIT da implementação: `02f65daf210e1b569c821e66085a9a9c9fa96e21`. Nenhum executor externo foi despachado.

## Objetivo

Manter a fala funcional no painel DEV e tornar explícitos disponibilidade, pedido aceito, término, falha e interrupção, antes de adicionar entrada por microfone.

## Escopo permitido

SpeechOutputPort, AndroidTextToSpeechPort, painel DEV, manifesto, testes pertinentes e documentação da tarefa. Ler documentação obrigatória e DESIGN antes de ajustar controles DEV. Preservar layout principal, textos/ações locais e release sem painel DEV. Não adicionar STT, IA, dependências pesadas ou novo motor.

## Requisitos

- Distinguir aceitação na fila de fala concluída: retorno imediato de speak não comprova áudio. Usar callbacks associados ao pedido para início, término e erro; descartar eventos antigos/cancelados.
- Tratar inicialização e idioma indisponível sem indicar prontidão indevida. Impedir callback tardio de reativar instância descartada; shutdown seguro e idempotente.
- Disponibilizar interrupção simples no DEV; parar ao fechar o painel e no descarte. Preparar contrato para parar antes de futura escuta, sem implementar STT.
- Tratar resultado e erros na integração, preservando a resposta escrita quando áudio estiver indisponível.
- Adicionar visibilidade TTS_SERVICE conforme documentação Android. Preferir voz pt-BR que não exija rede; se indisponível, informar indisponibilidade sem fallback de rede silencioso. Não alegar garantia offline sem validação no aparelho.
- Personalização completa de voz/engine fica fora deste recorte. Não prometer qualidade de voz diferente.
- Criar testes significativos para transições/cancelamento/callback tardio por fakes ou pequena separação testável, sem reestruturar o app inteiro.

## Aceite e entrega

PR/patch contra baseline exata; revisão estática sem falsos relatos de fala concluída; documentação do contrato e limitações atualizada. Entregar roteiro ao proprietário: fala normal, pedidos sucessivos, interromper, fechar/reabrir painel, sair durante inicialização, voz ausente/erro quando reproduzível, comportamento sem rede, regressão de comandos digitados. Não exigir desinstalação de apps ou engines para testar.

Builds/testes locais exclusivamente pelo proprietário: `./gradlew.bat testDebugUnitTest assembleDebug assembleRelease`. Executor informa testes criados separadamente dos realmente executados. Até aceite, status IMPLEMENTADA_AGUARDANDO_TESTE; nunca merge/push em main pelo executor.

Retorno obrigatório: TASK, BASE COMMIT, HEAD, STATUS, RESULTADO DA REVISÃO (se houver), ALTEROU ARQUIVOS, PR/patch, TESTES EXECUTADOS, PENDÊNCIAS, PRÓXIMO PASSO. Registrar handoff em docs/handoffs/TASK-010.md.

Referência: [Android TextToSpeech](https://developer.android.com/reference/android/speech/tts/TextToSpeech).
