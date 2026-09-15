# Arquitetura inicial

## Visão geral

O DELAMAIN será um aplicativo Android modular. A interface não deve conter lógica de negócio e o motor de comandos não deve depender da implementação visual. As camadas abaixo são o desenho alvo; hoje o módulo `app` contém a interface e um núcleo Kotlin local independente de Android.

## Fluxo ativo: V0.2-B

`domain/LocalCommandEngine` recebe texto e retorna `LocalCommandResult.Recognized(intent, response, actionResult)` ou `Unknown`. Usa catálogo fechado, aliases informais, normalização e prefixo opcional “Delamain”; o relógio é injetável e as frases alternam por intenção. Executa apenas ações tipadas `VOLUME_UP/DOWN` por `LocalActionPort`, cujo padrão é indisponível. Ver [LOCAL-02](handoffs/LOCAL-02.md).

O adapter `integration/audio/AndroidMediaVolumeActionPort` ajusta somente `STREAM_MUSIC`, verifica volume fixo/limites e confirma mudança pela leitura anterior/posterior. Falta de confirmação não causa repetição automática. A permissão normal `MODIFY_AUDIO_SETTINGS` está no manifesto. Domínio não depende de Android ou rede.

Também existe uma contribuição remota em `domain/command/` e `platform/`, ainda não conectada ao fluxo ativo. Preservá-la até a revisão de consolidação em [TASK-003](../tasks/TASK-003.md); não tratar mídia/apps/volume percentual como integrados.

A entrada manual é fornecida por `DebugCommandPanel` em `src/debug`; `src/release` fornece a mesma função sem conteúdo. O rosto apenas recebe a sobreposição, sem lógica de reconhecimento nos componentes visuais. Voz e máquina de estados de conversa serão integradas em etapas posteriores.

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
Separar wake word, speech-to-text e text-to-speech. A primeira implementação pode usar APIs Android disponíveis; componentes offline serão avaliados antes de adicionar serviços externos.

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
