# DELAMAIN — especificação visual V0.1

## Objetivo

Criar a primeira tela do aplicativo: uma experiência fullscreen horizontal que pareça um sistema de IA automotivo dedicado, e não um aplicativo Android convencional.

A referência estética é cyberpunk/ficção científica, com inspiração em interfaces de IA veicular e no clima visual de Delamain/KITT. Para o software, usar assets próprios ou devidamente licenciados; não incorporar diretamente arquivos extraídos de Cyberpunk 2077.

## Dispositivo de desenvolvimento

- Primeiro alvo: Xiaomi Redmi 13.
- O app não deve depender do modelo do aparelho.
- Orientação principal: landscape.
- Interface: edge-to-edge/fullscreen.
- Sem barra de navegação ou status bar visível durante a experiência principal, quando o Android permitir.

## V0.1 — estados

### BOOT
Sequência curta, aproximadamente 2–4 segundos.

1. Fundo preto.
2. Ruído/interferência muito discreta.
3. Elementos do rosto surgem gradualmente.
4. Glitch/flicker curto.
5. Indicador de inicialização.
6. Transição suave para IDLE.

O boot não deve bloquear indefinidamente. Deve ter duração determinística e tolerar animações reduzidas em aparelhos lentos.

### IDLE
Estado padrão.

- Rosto digital central.
- Pequena animação de respiração/vida.
- Glitch ocasional e discreto.
- HUD mínimo.
- Nenhuma informação desnecessária.

### LISTENING
Quando o sistema estiver ouvindo o usuário.

- Rosto muda para indicar atenção.
- Indicador visual de áudio/escuta.
- Animação mais ativa que IDLE.
- Deve ser imediatamente distinguível de IDLE.

### THINKING
Enquanto uma solicitação estiver sendo processada.

- Rosto permanece visível.
- Efeito de processamento/glitch controlado.
- Indicador textual opcional.
- Não usar animação pesada que prejudique desempenho.

### SPEAKING
Enquanto o TTS estiver falando.

- Rosto indica atividade vocal.
- Futuramente poderá existir animação de boca/lábios sincronizada com amplitude do áudio.
- V0.1 pode usar apenas uma animação simples; sincronização labial fica para versão posterior.

### ERROR
- Estado visual claramente diferente.
- Mensagem curta.
- Permitir retorno ao IDLE.
- Nunca deixar a interface presa por erro de rede/IA.

## Direção visual

### Cores

Base predominantemente preta/escura.

Paleta inicial sugerida:

- fundo: quase preto;
- elemento principal: ciano/azul frio;
- destaque de atividade: vermelho ou magenta apenas quando necessário;
- texto secundário: cinza frio.

As cores devem ser centralizadas em constantes/tema para poder trocar a personalidade visual posteriormente.

### Tipografia

- Fonte sans-serif/monoespaçada de aparência tecnológica.
- Alta legibilidade em tela pequena.
- Evitar excesso de texto.
- Textos de estado devem ser curtos: `INITIALIZING`, `ONLINE`, `LISTENING`, `THINKING`, `SPEAKING`, `ERROR`.

### Rosto

O rosto é um componente visual independente da lógica do aplicativo.

Requisitos:

- aparência digital/futurista;
- leitura clara mesmo em baixa resolução;
- animação leve;
- preparado para substituir o asset/personagem no futuro;
- não depender de vídeo pesado para animações básicas.

## Animações

Preferir animações procedurais/2D leves em Compose/Canvas ou assets pequenos. Evitar GIFs grandes e vídeos como mecanismo principal da UI.

Efeitos permitidos na V0.1:

- opacity/fade;
- deslocamento pequeno;
- flicker;
- scanlines discretas;
- glitch horizontal curto;
- ruído/grain leve;
- pulsação de elementos.

Todos os efeitos devem poder ser desativados ou reduzidos caso o desempenho seja insuficiente.

## Layout conceitual

```text
┌────────────────────────────────────────────────────┐
│                                                    │
│                                                    │
│                    ROSTO                           │
│                                                    │
│                 DIGITAL AI                         │
│                                                    │
│                                                    │
│   STATUS                         TIME / SYSTEM     │
│                                                    │
└────────────────────────────────────────────────────┘
```

Não transformar a primeira versão em um painel cheio de widgets. O foco é criar identidade visual.

## Arquitetura da UI

A UI deve receber um estado de alto nível e renderizá-lo. Não colocar lógica de voz, IA ou comandos diretamente nos composables visuais.

Modelo conceitual:

```text
UiState
  ├── Boot
  ├── Idle
  ├── Listening
  ├── Thinking
  ├── Speaking
  └── Error
       ↓
  DelamainScreen
       ├── FaceLayer
       ├── EffectsLayer
       └── HudLayer
```

## V0.1 não inclui

- wake word;
- STT;
- TTS;
- IA;
- câmera;
- GPS;
- Bluetooth;
- controle de mídia;
- saída para monitor externo.

Esses recursos entram posteriormente e não devem ser simulados com dependências desnecessárias na V0.1.

## Critério visual de aceite

A V0.1 estará pronta quando:

1. compilar sem erros;
2. abrir em fullscreen landscape;
3. executar o boot automaticamente;
4. chegar ao IDLE sem intervenção;
5. apresentar rosto e efeitos fluidos;
6. permitir testar manualmente os estados de UI durante desenvolvimento;
7. não exigir internet;
8. não exigir permissões perigosas;
9. não deixar serviços de microfone rodando em background.
