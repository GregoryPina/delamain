# Decisões

## ADR-001 — domínio local antes de IA

Kotlin/Compose existentes são preservados. O fluxo ativo usa `LocalCommandEngine`, catálogo explícito e portas tipadas; a IA não é necessária para reflexos ou volume. Reconsiderar roteamento somente com testes que preservem negações e ausência de efeitos inesperados.

## ADR-002 — transição para executores externos

Instrução do proprietário em 2026-09-15 substitui a política de SOL interno. Astra concentra Work, integração, testes e Git. Não criar/reativar agentes internos. O proprietário encaminha TASKs a modelos externos; cada wave usa commit publicado exato e retorna patches.

## ADR-003 — preservar as duas contribuições durante sincronização

O remoto avançou de `5d0be25` a `2894e76` com `domain/command/CommandRouter` e `platform/AndroidCommandExecutor`, enquanto o trabalho local introduziu `LocalCommandEngine` e `LocalActionPort`. Preservar ambas as contribuições no checkpoint. O painel continua usando apenas o motor local validado. O código remoto não deve ser conectado antes de revisar reconhecimento por substring, negações e confirmação de resultados.

Uma TASK externa deverá propor consolidação incremental, aproveitando comportamentos úteis sem duplicar caminhos ativos. Mídia, abertura de apps e volume percentual presentes no remoto não são considerados integrados/validados pelo simples merge.

## ADR-004 — validação pelo proprietário e identidade VEXA

O proprietário assumiu builds/testes locais para economizar uso do Work. Astra faz revisão estática e integração, fornece roteiro e registra resultados informados. Não executar testes automaticamente. Código aguardando validação permanece identificado como tal.

A companion passa a se chamar VEXA. TASK-005 altera apenas nome exibido, textos e gatilho digitado; repositório, applicationId, pacotes/classes e assets são preservados. O nome antigo permanece somente como referência histórica/técnica, não como gatilho.

## ADR-005 — voz via APIs nativas do Android

Não desenvolver STT/TTS próprios nesta fase. STT futuro usará `SpeechRecognizer`, preferindo on-device quando disponível. Serviço do sistema que possa usar rede exige escolha explícita do usuário; não fazer fallback silencioso. TTS usa `android.speech.tts.TextToSpeech`. Não usar Google Assistente, “Ok Google” nem substituir o Assistente por um wake word do app para **interpretar** comandos — apenas reutilizar a infraestrutura de reconhecimento/fala do aparelho.

Fluxo alvo V0.3:

```text
Microfone → SpeechRecognizer → texto → LocalCommandEngine → ação/resposta local ou Unknown → TextToSpeech
```

- Roteamento canônico: **`LocalCommandEngine`** (não `CommandRouter`, que permanece inativo).
- Gatilho de chamada: **VEXA** (não Delamain).
- IA/provider: conversa aberta futura quando habilitada, nunca fallback automático de todo `Unknown`; entradas ambíguas, negadas ou falhas locais não autorizam envio remoto. Provider ainda não escolhido.
- Wake word contínua: etapa futura, independente do STT inicial.
- Camadas desacopladas via portas (`SpeechInputPort` futuro, `SpeechOutputPort` agora) para permitir troca de implementação sem reescrever o domínio.

TASK-009 implementa apenas a saída de voz (TTS) no painel DEV; STT e wake word ficam para TASKs posteriores.

## ADR-007 — modo mute, não “somente texto”

Decisão do proprietário (2026-09-16): não haverá modo de produto “somente texto”. A entrada TEXTO (TASK-016) é método de comando alternativo ao microfone, sempre disponível, não um modo exclusivo.

**Modo mute** (TASK-017): VEXA continua processando comandos e mostrando respostas na tela, mas suprime TTS automático. Diferente de PARAR (interrompe fala atual) e de volume zero do sistema. Persistência no mesmo store da preferência de voz (TASK-014). Prévia explícita (ex.: TESTAR VOZ no DEV) pode falar mesmo em mute.

“Modo silencioso” em PERSONALITY/INTERACTIONS para comentários espontâneos futuros permanece conceito separado (V0.5+), não confundir com mute operacional.

## ADR-006 — estabilizar saída antes de captura de voz

Revisão de continuidade em 2026-09-16: preservar aceite funcional da TASK-009, mas tratar semântica assíncrona, descarte e erros em TASK-010 antes do STT. TASK-011 será escuta explícita por botão, uma sessão por vez, sem wake word/loop contínuo, com cancelamento e sem autoescuta do TTS. Personalização da voz é recorte separado, posterior à estabilidade.

`TextToSpeech.speak` aceita um pedido na fila; conclusão depende do listener. `SpeechRecognizer` do sistema pode transmitir áudio, e `EXTRA_PREFER_OFFLINE` pode ser ignorado. Logo “API nativa” não prova operação offline. Não incluir gravação/rede implícita nas próximas tarefas.

Fontes: [TextToSpeech](https://developer.android.com/reference/android/speech/tts/TextToSpeech), [SpeechRecognizer](https://developer.android.com/reference/android/speech/SpeechRecognizer), [preferência offline](https://developer.android.com/reference/android/speech/RecognizerIntent#EXTRA_PREFER_OFFLINE).
