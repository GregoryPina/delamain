# TASK-010 — estabilização TTS

STATUS ATUAL: INTEGRADA após aceite funcional do proprietário. Ver [fechamento TASK-013](TASK-013.md). O relato abaixo preserva condições da entrega original, inclusive testes então pendentes.
BASE COMMIT: `02f65daf210e1b569c821e66085a9a9c9fa96e21`.
Branch: `codex/task-010-tts-hardening`. Implementação local pelo Astra excepcionalmente autorizada pelo proprietário em 2026-09-16; método habitual preservado para próximas tarefas. Sem agentes internos. Sem merge em main.

## Entrega e contratos

- `SpeechOutputResult.Queued` substitui `Spoken`: significa aceitação, não reprodução concluída.
- `SpeechOutputSession` é Kotlin puro; identifica cada pedido e aceita eventos apenas do pedido ativo. Estados: Preparing, Ready, Unavailable, Queued, Speaking, Completed, Stopped, Failed, Closed.
- `SpeechOutputEngine` é a pequena interface testável do adapter. Chamadas/eventos serializados na thread principal; callbacks Android são sempre postados nela.
- `stop(): Boolean` indica aceite do motor, não prova silêncio físico. Erro ao parar impede enfileirar substituição. Não iniciar futura escuta se retornar false. Shutdown é idempotente, libera recursos mesmo se stop falhar e não notifica a UI descartada.
- Inicialização tardia não reativa instância encerrada. Falha de inicialização/idioma/listener/voz mantém texto e informa indisponibilidade; não guarda fala para reprodução automática posterior.
- Voz escolhida somente entre pt-BR, sem rede declarada e sem marcador de dados não instalados; preserva a voz atual se elegível. Sem fallback remoto. O comportamento real depende do motor instalado, ainda precisa de teste offline.
- Manifesto declara consulta TTS_SERVICE. Sem novas dependências/permissões de microfone ou internet.
- DEV mostra estado da fala e PARAR VOZ. Fecha/libera motor ao fechar painel, recria ao reabrir, solicita stop em ON_STOP. Abrir outro app também pode interromper a resposta falada ao levar VEXA ao fundo.
- Resposta escrita é preservada. Sem alteração do rosto/HUD principal, ações locais ou release sem painel.

## Revisão e testes

Revisão estática local realizada; não equivale a revisão independente ou build aprovado. Verificados usos do contrato, serialização de callbacks, invalidação de pedidos e diff sem erros de whitespace.

12 testes criados em SpeechOutputSessionTest: fila versus início/término; indisponibilidade; substituição e eventos antigos; cancelamento e novo pedido; descarte antes/depois de inicialização; falha de enqueue; erro assíncrono e retry explícito; stop do motor; stop rejeitado; texto vazio; fechamento durante preparação. Fakes não verificam binder, Compose, seleção de voz real ou áudio do aparelho.

TESTES EXECUTADOS: nenhum. Builds também não executados, por preferência do proprietário. Não reutilizar aceite TASK-009 para concluir TASK-010.

## Roteiro do proprietário

```powershell
git switch codex/task-010-tts-hardening
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease
```

1. Instalar APK debug, abrir DEV, aguardar “Voz: pronta”. Enviar `Vexa`, `bateria`, `que horas são?`: texto preservado, estado passa por fila/fala/conclusão (estados curtos podem não ser visíveis).
2. Enviar comandos sucessivos: só o pedido mais recente determina estado; não reaparecer fala antiga depois de parar.
3. Apertar PARAR VOZ durante fala; enviar novo comando depois. FECHAR deve parar; reabrir deve inicializar uma nova sessão.
4. Abrir/fechar DEV rapidamente enquanto prepara; sair/retornar ao app durante preparação/fala. Sem crash ou fala retomada automaticamente. Ao abrir Spotify por comando, texto/ação mantidos e fala pode ser interrompida quando VEXA sair da tela.
5. Sem rede, testar fala com dados pt-BR já instalados. Se não houver voz elegível, deve manter texto e informar indisponibilidade; não instalar/remover engines como pré-requisito do teste. Falhas de voz só testar se reproduzíveis no aparelho.
6. Regressão: presença informal, hora, bateria, volume, apps permitidos, mídia anterior/próxima e Unknown. Conferir painel com teclado; release continua sem DEV.

Informar HEAD testado, resultado do comando e cenários manuais. Após confirmação, revisar delta e integrar; TASK-011 continua bloqueada pelo fechamento desta tarefa.

## Limitações

Sem timeout automático para motor que nunca responde; PARAR VOZ/FECHAR permitem cancelar/liberar. Sem garantia de silêncio físico, funcionamento offline ou callbacks corretos de motores de terceiros antes de validação no aparelho. Personalização da voz, STT e sincronização facial continuam fora do recorte.
