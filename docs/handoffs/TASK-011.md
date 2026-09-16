# TASK-011 — escuta por botão

STATUS: IMPLEMENTADA_AGUARDANDO_TESTE.
BASE COMMIT: `4d32ce9dac04b54d99e5da119fe070c1199d4b32` (PR #2, TASK-010, não integrada).
Branch: `codex/task-011-push-to-talk`, dependente de `codex/task-010-tts-hardening`. Não integrar antes da validação conjunta e fechamento da TASK-010.

Implementação local pelo Astra excepcionalmente autorizada ao continuar o desenvolvimento nesta sessão. Não altera o método habitual de coordenação/executores externos nas próximas sessões.

## Comportamento

- OUVIR inicia uma sessão; o botão vira CANCELAR ESCUTA. Nunca há captura automática, loop, wake word ou fallback remoto.
- Apenas `createOnDeviceSpeechRecognizer`, em Android 12/API31+ e serviço disponível. Android 26–30 mantém texto. `EXTRA_PREFER_OFFLINE` é complementar; não é a garantia utilizada para escolher o serviço.
- RECORD_AUDIO e consulta RecognitionService apenas no manifesto debug. Permissão solicitada ao tocar; após conceder, tocar OUVIR novamente. Não iniciar captura a partir do callback de permissão, inclusive se o painel fechou.
- `SpeechInputSession` verifica disponibilidade/permissão, pede parada ao TTS e só então inicia; se parada falhar, não captura. `stop()` aceito não comprova silêncio físico: testar eco/resíduo no aparelho.
- Resultado final único entra no LocalCommandEngine, aparece no campo e produz resposta escrita/TTS. Parciais não fazem nada. Sessão e reconhecedor liberados antes de processar resultado.
- IDs isolam sessões: callbacks após cancelamento/erro/descarte ou de sessões anteriores não executam ações. Editar/enviar texto, fechar painel ou ON_STOP cancela a captura. Cada nova ativação cria um reconhecedor próprio.
- Timeout total de 15 segundos, cancelamento/destroy, erro/ausência de correspondência sem repetição automática. Unknown permanece local.

## Verificação

10 testes de sessão criados: entrega única; ausência de suporte; permissão; falha de parada TTS; busy; eventos após cancelamento/substituição; erro/timeout; resultado vazio; descarte; falha nativa com retry explícito.

TESTES/BUILDS EXECUTADOS: nenhum, conforme preferência do proprietário. Revisão estática e diff apenas. Não há prova de disponibilidade de reconhecimento local pt-BR no Redmi 13, nem da qualidade em ruído. Fakes não validam microfone, Compose, permissões Android ou serviços reais.

## Roteiro de amanhã

A branch inclui TASK-010; pode validar ambas no mesmo APK. Registrar HEAD testado.

```powershell
git switch codex/task-011-push-to-talk
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease
```

1. Cumprir primeiro roteiro TASK-010 (fala normal/interrupção/fechar/reabrir/offline).
2. Em DEV, tocar OUVIR. Negar permissão: texto funciona, nenhuma ação. Conceder quando desejar; microfone não começa até novo toque.
3. Com serviço local disponível, tocar, esperar “Ouvindo” e dizer `Vexa, que horas são?`. Um resultado, uma resposta e nenhuma escuta automática depois.
4. Dizer `aumente o volume`: conferir uma única alteração. Repetir com novo toque. Dizer frase desconhecida: Unknown sem IA.
5. Cancelar durante escuta; falar após cancelar não deve executar. Reabrir e testar novo pedido. Editar/enviar texto durante escuta deve cancelar resultado de voz anterior.
6. Fechar DEV e sair do app durante escuta/processamento: microfone libera, não há ação/fala tardia. Retornar não recomeça escuta.
7. Permanecer em silêncio: timeout/erro local, sem loop. Se serviço/idioma indisponível, mensagem e texto utilizável; não trocar para serviço remoto.
8. Sem rede, repetir frase com modelo já disponível. Ouvir enquanto TTS fala: TTS deve parar antes da captura; conferir ausência de autoescuta no aparelho.
9. Conferir painel com teclado/scroll. Release deve continuar sem DEV e sem permissão RECORD_AUDIO.

Não alterar main até confirmação. Próxima entrega possível: escolha de voz local em recorte separado; sem adicionar API de IA ou wake word antes de estabilizar o conjunto.
