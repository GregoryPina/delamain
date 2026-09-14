# DELAMAIN

Projeto pessoal de um assistente automotivo Android inspirado na estética de inteligência artificial veicular de Cyberpunk 2077, desenvolvido para uso privado.

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

**Fase 0 — especificação e estrutura.**

Nenhum APK está sendo produzido ainda.

## Documentação

- [Arquitetura](docs/ARCHITECTURE.md)
- [Roadmap](docs/ROADMAP.md)
- [Diretrizes para agentes de código](AGENTS.md)
