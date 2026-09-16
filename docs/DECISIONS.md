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

Não desenvolver STT/TTS próprios nesta fase. STT futuro usará `SpeechRecognizer` (on-device quando disponível; senão serviço do sistema). TTS usará `android.speech.tts.TextToSpeech`. Não usar Google Assistente, “Ok Google” nem substituir o Assistente por um wake word do app para **interpretar** comandos — apenas reutilizar a infraestrutura de reconhecimento/fala do aparelho.

Fluxo alvo V0.3:

```text
Microfone → SpeechRecognizer → texto → LocalCommandEngine → ação/resposta local ou Unknown → TextToSpeech
```

- Roteamento canônico: **`LocalCommandEngine`** (não `CommandRouter`, que permanece inativo).
- Gatilho de chamada: **VEXA** (não Delamain).
- IA/OpenAI: fallback futuro para `Unknown` e conversa aberta; não para comandos locais determinísticos.
- Wake word contínua: etapa futura, independente do STT inicial.
- Camadas desacopladas via portas (`SpeechInputPort` futuro, `SpeechOutputPort` agora) para permitir troca de implementação sem reescrever o domínio.

TASK-009 implementa apenas a saída de voz (TTS) no painel DEV; STT e wake word ficam para TASKs posteriores.
