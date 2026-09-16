# Arquitetura — estado atual da VEXA

## Visão geral

A VEXA mantém Kotlin/Compose no módulo `app`, com domínio independente de Android e adapters de plataforma. Não introduzir outra arquitetura para continuar as entregas existentes.

## Fluxo ativo após TASK-009

`domain/LocalCommandEngine` recebe texto e retorna `LocalCommandResult.Recognized(intent, response, actionResult)` ou `Unknown`. Usa catálogo fechado, aliases informais, normalização e prefixo opcional “Vexa”; relógio e bateria são injetáveis. `LocalAction` é uma sealed interface com `VolumeUp`, `VolumeDown`, `OpenApp`, `MediaNext` e `MediaPrevious`. `CompositeLocalActionPort` encaminha ao adapter correspondente. Respostas locais têm pelo menos três variantes; Unknown usa `LocalUnknownResponses`.

O adapter `integration/audio/AndroidMediaVolumeActionPort` ajusta somente `STREAM_MUSIC`, verifica volume fixo/limites e confirma mudança pela leitura anterior/posterior. Falta de confirmação não causa repetição automática. A permissão normal `MODIFY_AUDIO_SETTINGS` está no manifesto. Domínio não depende de Android ou rede.

Apps: `AndroidLaunchAppActionPort`, allowlist de YouTube, Chrome, Maps, Spotify e WhatsApp e `<queries>` correspondentes. Mídia: `AndroidMediaKeyActionPort` envia anterior/próxima e retorna `Dispatched`, sem afirmar que o player trocou a faixa. Bateria: `BatteryStatusPort` + `AndroidBatteryStatusPort`, sem permissão extra.

O código em `domain/command/` e `platform/` continua inativo. TASK-003 revisou a sobreposição e TASK-004 corrigiu o parser remoto; capacidades ativas posteriores foram adicionadas ao motor canônico, sem ligá-lo ao executor antigo. Play/pause e volume percentual continuam fora do fluxo ativo.

A entrada manual é fornecida por `DebugCommandPanel` em `src/debug`; `src/release` fornece a mesma função sem conteúdo. Após processar o texto, o painel pede fala via `SpeechOutputPort` + `AndroidTextToSpeechPort`. Na branch TASK-010 (aguardando testes), `Queued` representa aceite da fila; `SpeechOutputSession` acompanha início/término/erro pelo ID ativo, invalida callbacks cancelados e protege descarte. O adapter serializa eventos na thread principal e seleciona voz pt-BR declarada sem rede. DEV mostra estado, oferece interrupção e libera TTS ao fechar; stop em ON_STOP. `stop()` retorna aceite do motor, não prova de silêncio físico. Main mantém TASK-009 até validação. O rosto ainda não acompanha automaticamente os eventos de voz.

```text
Microfone / UI / eventos Android
            |
            v
      Input / Voice Layer
            |
            v
      Command Router
        /          \
 local action      AI request
      |                 |
      v                 v
 Android APIs       AI Provider
        \             /
         v           v
          Response / Action
                 |
                 v
          TTS + UI state
```

## Camadas planejadas

### Presentation
Jetpack Compose. Responsável por fullscreen UI, boot sequence, rosto/personagem, estados IDLE/LISTENING/THINKING/SPEAKING/ERROR e HUD.

### Domain
Casos de uso e modelos independentes do Android. Exemplos futuros: `ExecuteCommand`, `ProcessVoiceInput`, `GenerateResponse`.

### Command Engine
Recebe texto/intenção e tenta primeiro comandos locais determinísticos. Exemplos: volume, mídia, abrir aplicativo, navegação, câmera, hora. Deve evitar chamada à IA quando a intenção puder ser resolvida localmente.

Inclui reflexos sociais: saudações e respostas curtas por gatilho também são locais. Um catálogo de frases aplica a [personalidade](PERSONALITY.md) tanto às respostas sociais quanto ao resultado das ações. A personalidade não escolhe permissões nem inventa sucesso. Ver [contrato de interações](INTERACTIONS.md) para precedência, cancelamento, modelos e cenários de aceite.

### Voice
Separar wake word, speech-to-text e text-to-speech. STT futuro: `SpeechRecognizer` (on-device quando disponível). TTS: `TextToSpeech` via `SpeechOutputPort` (TASK-009). Não usar Google Assistente para interpretar comandos. Ver ADR-005.

Fluxo alvo V0.3:

```text
Microfone → SpeechRecognizer → texto → LocalCommandEngine → resposta → TextToSpeech
```

Wake word contínua permanece etapa futura, independente do STT inicial.

### AI
Interface de provider abstrata. A implementação inicial não deve amarrar o domínio a Gemini, OpenAI ou outro fornecedor. O provider poderá ser trocado por configuração.

### Android integration
Adapters para AudioManager, MediaSession/MediaController, intents, câmera, localização, Bluetooth e outras APIs necessárias. Permissões devem ser solicitadas apenas quando a funcionalidade for ativada.

### Persistence
Preferir DataStore para configurações simples. Banco de dados só quando houver necessidade real.

## Baixa latência

O caminho de comando local deve ser:

```text
wake word -> STT -> intent -> Android API
```

sem rede e sem LLM. Consultas abertas/conversacionais seguem por AI provider.

Este é o caminho alvo quando o STT offline estiver disponível e validado. A escolha de uma API de voz não prova funcionamento offline. Medir separadamente reconhecimento, roteamento, ação e início de fala. Não enviar comandos desconhecidos ou ambíguos automaticamente à nuvem.

## UI

A primeira versão deve ser fullscreen e orientada para uso em uma tela horizontal. O design terá estética futurista/cyberpunk, mas os assets devem ser originais ou licenciados para o projeto. O rosto/efeitos serão tratados como uma camada visual independente da lógica.

## Compatibilidade

O scaffold atual declara `minSdk = 26`. Validar essa escolha junto às bibliotecas de voz e UI antes de prometer compatibilidade. Não assumir suporte a recursos específicos de MIUI/HyperOS.
