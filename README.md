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

TASK-004–013 integradas com aceite funcional anterior. TASK-014–019 implementadas em sequência de branches: persistência de voz, rosto por eventos, controles principais, mute, nome/tom e foco de áudio. Ainda aguardam validação no aparelho e integração.

A entrega atual `codex/stabilize-014-019` acrescenta correções de concorrência/estado/foco e ajuda local (“o que você sabe fazer?”). [Estado coordenado](docs/PROJECT_STATUS.md) e [roteiro único de teste](docs/handoffs/STABILIZATION-014-019.md) são as referências atuais. Nenhum build/teste executado nesta rodada; resultados anteriores não validam o patch.

Na tela principal: OUVIR, PARAR, TEXTO, MUTE/VOZ e TOM. Depois de permitir o microfone, toque OUVIR novamente. STT local exige suporte no aparelho; sem suporte, use texto. DEV só no debug. IA e wake word não implementados.
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

Para testar mudanças de voz e regressões de TASK-010–014, consulte o [handoff TASK-014](docs/handoffs/TASK-014.md) e o [roteiro consolidado anterior](docs/handoffs/TASK-012.md).

[Próximas tarefas detalhadas (013–022)](tasks/NEXT_TASKS.md): planejamento para coordenador/executor; TASK-014–019 implementadas; estabilização em validação.
