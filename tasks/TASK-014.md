# TASK-014 — lembrar a voz escolhida

Status: IMPLEMENTADA_AGUARDANDO_TESTE na branch `codex/task-014-voice-preference`.
BASE COMMIT: `f7e851bc799b018cd58c6f1bf2b15a54511f2c3f`. Aplicar [protocolo](NEXT_TASKS.md). Handoff: [TASK-014](../docs/handoffs/TASK-014.md).

## Resultado esperado

Hoje a voz volta à escolha inicial ao fechar DEV. Após selecionar e salvar uma voz válida, reabrir painel/reiniciar app deve tentar restaurar essa preferência. Não selecionar por posição da lista: “voz 2” pode mudar após atualização do motor.

## Contrato proposto

Adicionar pequeno repositório de preferências com leitura assíncrona e escrita explícita. Persistir identidade do motor TTS + ID da voz, versão do registro e nada de transcrição/áudio. `SpeechVoiceSelection` continua sendo catálogo disponível, separado da preferência desejada e da seleção efetivamente aplicada.

Preferir Preferences DataStore conforme arquitetura. Usar uma instância por arquivo/processo; verificar versão compatível antes de acrescentar dependência. Não criar banco Room nem novo módulo Gradle para duas chaves. A interface do repositório deve permitir fake em teste; nomes concretos ficam a cargo do executor.

## Sequência de aplicação

1. Carregar preferência e catálogo sem bloquear a thread de UI. Não falar automaticamente enquanto restaura.
2. Só restaurar se motor/ID corresponderem a voz pt-BR instalada e declarada sem rede. Reusar filtro da TASK-012.
3. Se ausente/incompatível, informar preferência indisponível e usar alternativa local elegível já prevista. Não sobrescrever automaticamente a preferência antiga com fallback temporário.
4. Se nenhuma local elegível, manter texto. Não baixar voz, trocar motor do Android ou fazer fallback remoto.
5. Salvar apenas após `setVoice` confirmado. Falha de escrita deve dizer “usando nesta sessão; não foi possível salvar”, sem desmarcar seleção real nem fingir persistência.
6. Uma seleção nova do usuário vence leitura antiga que chegue depois. Serializar escritas; a última escolha explícita deve prevalecer.
7. Oferecer “usar padrão” para remover só a preferência de voz. Não apagar futuras configurações de personalidade.

## Arquivos permitidos e limites

`SpeechVoiceSelection`, adapter TTS, integração de preferências (nova), composição DEV, build para uma dependência justificada, testes/docs. Não adicionar pitch/velocidade, múltiplos engines instalados, IA, mic automático ou mudança no rosto. Atualizar rótulo “sessão” somente quando persistência tiver sucesso.

## Cenários de teste

Fake de store: salvar/restaurar; ID removido; motor diferente com ID igual; voz agora de rede; erro de leitura/escrita; duas seleções com respostas de escrita invertidas; leitura tardia após escolha; reset preserva outras chaves. Testar identidade por string, nunca índice.

Proprietário: selecionar, fechar/abrir, reiniciar app, usar padrão, testar sem rede. Não exigir remover um motor para simular ausência; cobrir essa condição com fake. Informar explicitamente limitações de backup/restauração entre aparelhos.

## Aceite

Preferência restaurada somente quando elegível, sem áudio automático ou bloqueio da UI; erro de persistência distinguido de erro de TTS. Handoff inclui esquema de chaves, comportamento de fallback e migração. Leituras/gravações não podem ocorrer a cada recomposição.

Referência de implementação: [Android DataStore](https://developer.android.com/topic/libraries/architecture/datastore). Revalidar documentação e compatibilidade no despacho.
