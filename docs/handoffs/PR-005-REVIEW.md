# PR #5 — revisão estática TASK-014

HEAD revisado: `8e4d8bc09e236ec1429f2a6935ba05691eccf21b`.
Base conferida: `f7e851bc799b018cd58c6f1bf2b15a54511f2c3f` (merge-base corresponde).
Resultado: CHANGES REQUIRED antes de construir TASK-015 sobre esta entrega. Nenhum build/teste executado; achados derivados do código, não reproduções no aparelho.

Proprietário autorizou acumular testes e continuar desenvolvimento. Isso permite branch dependente não validada; não transforma revisão estática em aceite funcional nem autoriza merge de código pendente em main.

## R1 — restauração precisa pertencer a uma sessão [P1]

`DebugCommandPanel.kt`: coordinator e rememberCoroutineScope vivem fora do DisposableEffect do painel; onDispose encerra TTS, mas não invalida/cancela restore. Evento Restore procura o speechOutputPort mutável atual. `SpeechVoicePreference.kt:50–65` só compara geração de escolhas, não sessão.

Sequência: leitura A suspensa → fechar/reabrir DEV → porta B criada → leitura A termina sem nova escolha explícita. O resultado da sessão A pode ser aplicado na porta B. Outra corrida: salvar nova voz ainda pendente → reabrir → read obtém preferência antiga → restaura antiga → gravação nova termina e UI marca salva para seleção divergente.

Correção: token/lifetime de sessão no restore e aplicação de eventos, invalidação ao fechar/descartar, leitura coordenada com escritas já aceitas. Resultado antigo não toca porta nova. Definir política para gravação aceita que continue ao fechar (pode concluir persistência, sem atualizar sessão errada). Testar ambos os interleavings com gates/fakes, sem sleeps. Não depender de DataStore ser rápido.

## R2 — estado da fala fica oculto após restaurar/salvar [P2]

`DebugCommandPanel.kt`, Text que usa `submissionProblem ?: preferenceMessage ?: when (speechState)`. preferenceMessage permanece não nula após Saved/Restore; Speaking/Completed/Failed ficam invisíveis inclusive em erro assíncrono posterior.

Correção: apresentar estado de TTS e status da preferência separadamente, ou limpar/expirar status sem esconder falhas. Testar restaurar/salvar → falar → erro/conclusão e conferir estados. Não substituir tudo por um booleano de sucesso.

## R3 — CancellationException é convertida em erro de store [P2]

`DataStoreSpeechVoicePreferenceStore.kt:29–63`: catch RuntimeException também captura cancelamento de corrotina. read/write/clear podem retornar Failed/false ao chamador cancelado, que então publica evento no coordinator.

Correção: propagar CancellationException antes das capturas gerais; respeitar cancelamento/lifetime antes de publicar eventos. Cobrir read/write/clear cancelados e descarte com trabalho pendente, sem SaveFailed/ReadFailed tardio. Referência: https://kotlinlang.org/docs/cancellation-and-timeouts.html.

## R4 — geração protege evento, mas não gravação obsoleta [P2]

`SpeechVoicePreference.kt:68–79`: comparação de change ocorre depois de store.write/clear; mutex serializa chegada, não ordem lógica da escolha. Se change antigo for executado depois do novo, ainda sobrescreve disco (mesmo sem evento UI). Teste atual só inicia one antes de two e não cobre essa ordem invertida.

Correção: validar geração dentro da região serializada antes da mutação, ou fila que garanta ordem lógica; incluir clear versus save e execução invertida dos jobs. Última intenção aceita deve determinar persistência e rótulo, não só evento final. Leitura de restore também deve respeitar o contrato definido em R1.

## Ajuste de feedback associado

USAR PADRÃO exibe “Preferência removida” imediatamente quando selectDefaultVoice falha, antes do resultado assíncrono de clear. Só anunciar remoção quando confirmada; falha de seleção e falha de persistência são resultados distintos. Cobrir clear falhando e fallback indisponível, sem confirmação falsa.

## Observações

DataStore 1.2.1 confirmado como estável na documentação oficial em 2026-09-16; isso não comprova compilação no projeto. Reformatar extensivamente painel/build tornou diff maior sem ajudar o recorte: evitar novas reformatações, mas não exigir reversão cosmética que cause retrabalho.

Fonte DataStore: https://developer.android.com/jetpack/androidx/releases/datastore.

## Continuidade autorizada

Mesmo executor corrige PR #5 primeiro (commits próprios); registra novo HEAD exato; então abre branch TASK-015 desse HEAD corrigido e PR draft contra branch da 014. Sem merge, build/testes ficam acumulados. Retornar ambos os HEADs e roteiro conjunto. Se algum achado não puder ser resolvido, explicar concretamente e não apoiar 015 em contrato ainda contraditório.
