# VEXA

Projeto pessoal de um companion de viagem Android inspirado na estética de inteligência artificial veicular de Cyberpunk 2077, desenvolvido para uso privado. A companion se chama VEXA e tem personalidade própria: cortês, calma, atenta e com humor seco discreto.

O repositório continua em `GregoryPina/delamain` e os identificadores técnicos existentes (`applicationId`, namespace, pacotes, classes, tema e assets com nomes Delamain) são preservados para manter compatibilidade e histórico do projeto.

## Objetivo

Transformar um celular Android em um sistema automotivo pessoal com:

- interface fullscreen futurista;
- animação de inicialização e rosto digital;
- wake word personalizada;
- comandos locais de baixa latência;
- voz (STT/TTS);
- integração opcional com IA online;
- música e mídia;
- Bluetooth;
- GPS/navegação;
- câmera;
- futura saída para um monitor automotivo externo.

## Estratégia

O aplicativo deve funcionar primeiro como um app Android independente. O hardware do carro e a saída de vídeo serão tratados posteriormente.

### Princípios

1. **Local-first:** comandos simples devem funcionar sem internet e sem depender de uma LLM.
2. **Baixa latência:** wake word e comandos frequentes devem ter o caminho mais curto possível.
3. **Modularidade:** voz, IA, mídia, navegação e interface devem ser componentes separados.
4. **Android compatível:** evitar dependências desnecessárias de um fabricante específico.
5. **Privacidade:** áudio não deve ser enviado para a nuvem sem necessidade.
6. **Iteração incremental:** cada versão deve compilar e funcionar antes de adicionar outra camada.

## Desenvolvimento

O projeto será preparado neste repositório para que possa ser aberto no Android Studio e trabalhado com Claude Code/Codex ou manualmente.

## Status

**Interface V0.1 validada funcionalmente pelo proprietário em 15 de setembro de 2026.**

O fluxo ativo no painel DEV inclui reflexos locais, hora, presença informal, volume de mídia, abertura de cinco apps permitidos, faixa anterior/próxima, bateria, respostas variadas e fala por TTS. TASK-006–009 foram integradas e os handoffs registram validação pelo proprietário; o último relato de TTS foi “funcionou, ela falou”. A voz padrão ainda precisa ser personalizada. STT, wake word e IA não estão implementados.

No APK debug, abra `DEV` e envie “Vexa, tá aí?”, “que horas são?”, “abra o spotify”, “próxima música” ou “bateria”. O painel começa fechado e não existe na variante release. As ações ativas usam adapters próprios; o antigo `CommandRouter`/`AndroidCommandExecutor` continua inativo. Builds e testes locais são executados pelo proprietário, não automaticamente pelo Tech Lead.

Próximo recorte planejado: estabilizar o ciclo de vida e os resultados do TTS antes de STT por botão. Veja [estado atual](docs/PROJECT_STATUS.md) e [TASK-010](tasks/TASK-010.md).

## Como o companion responderá

- **Reflexos locais:** gatilhos conhecidos escolhem frases prontas e executam ações permitidas, sem consultar IA.
- **Conversa por IA:** perguntas abertas e pedidos complexos usam uma API opcional, mantendo a mesma personalidade.
- **Controle local:** a IA pode propor uma ação, mas o app valida sua permissão, parâmetros e disponibilidade antes de executar.

Exemplo: “Vexa, está aí?” → “Sempre a postos, Gregory.” “Que horas são?” → resposta com a hora do aparelho. “Me ajude a organizar uma viagem de três dias” → conversa via IA, se habilitada.

A personalidade aparece desde as respostas prontas. Internet não é requisito para dar identidade ao personagem; reconhecimento e síntese de voz offline precisam ser validados separadamente.

## Documentação

- [Arquitetura](docs/ARCHITECTURE.md)
- [Roadmap](docs/ROADMAP.md)
- [Status coordenado](docs/PROJECT_STATUS.md)
- [Retomada do Tech Lead](TECH_LEAD_HANDOFF.md)
- [Decisões](docs/DECISIONS.md)
- [Personalidade e respostas](docs/PERSONALITY.md)
- [Roteamento e gatilhos](docs/INTERACTIONS.md)
- [Ideias para evolução](docs/IDEAS.md)
- [Design visual](docs/DESIGN.md)
- [Desenvolvimento](docs/DEVELOPMENT.md)
- [Diretrizes para agentes de código](AGENTS.md)
