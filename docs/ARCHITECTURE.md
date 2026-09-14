# Arquitetura inicial

## Visão geral

O DELAMAIN será um aplicativo Android modular. A interface não deve conter lógica de negócio e o motor de comandos não deve depender da implementação visual.

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

## UI

A primeira versão deve ser fullscreen e orientada para uso em uma tela horizontal. O design terá estética futurista/cyberpunk, mas os assets devem ser originais ou licenciados para o projeto. O rosto/efeitos serão tratados como uma camada visual independente da lógica.

## Compatibilidade

O projeto deve declarar uma versão mínima de Android deliberadamente escolhida após verificar os requisitos das bibliotecas de voz e UI. Não assumir suporte a recursos específicos de MIUI/HyperOS.
