# DELAMAIN — guia de desenvolvimento

## Regra principal

Construir o projeto em pequenos incrementos. Cada incremento deve deixar o repositório em um estado compilável ou, quando ainda for apenas documentação, claramente separado do código executável.

## Fluxo de trabalho

1. Ler `README.md`, `AGENTS.md` e a documentação relevante em `docs/` antes de alterar o projeto.
2. Implementar somente a etapa solicitada.
3. Fazer build/testes relevantes.
4. Corrigir erros antes de avançar.
5. Atualizar `ROADMAP.md` quando uma etapa realmente estiver concluída.
6. Registrar decisões arquiteturais relevantes na documentação.

## Ferramentas

- Android Studio: ambiente principal para build, execução e debugging Android.
- Git/GitHub: versionamento e histórico.
- Claude Code: agente de backup para alterações locais, especialmente quando for conveniente trabalhar diretamente na máquina do desenvolvedor.
- ChatGPT/GitHub: arquitetura, revisão, documentação e alterações no repositório quando necessário.

## Estratégia de IA de desenvolvimento

O agente de código deve evitar reescrever grandes partes do projeto sem necessidade. Antes de alterar uma implementação existente, procurar os arquivos e componentes relacionados e preservar interfaces públicas já estabelecidas.

Quando uma biblioteca externa for necessária:

- justificar a necessidade;
- preferir biblioteca madura e pequena;
- verificar compatibilidade com a versão mínima do Android;
- evitar adicionar uma dependência apenas para uma funcionalidade que pode ser implementada com APIs Android adequadas.

## Testes no dispositivo

O primeiro dispositivo real de teste será o Redmi 13. O comportamento observado no dispositivo deve ser considerado evidência mais forte que suposições sobre hardware/MIUI.

Para recursos de voz, testar separadamente:

1. captura de áudio;
2. wake word;
3. STT;
4. roteamento de comando;
5. execução da ação;
6. TTS.

Não juntar todos esses componentes em um único primeiro teste.

## Segurança e privacidade

- Nenhuma chave de API deve ser commitada.
- Credenciais devem ficar fora do repositório.
- O aplicativo deve solicitar somente permissões necessárias para a funcionalidade atual.
- O caminho local de comandos deve permanecer local.
- O usuário deve ter controle explícito sobre integrações que enviem dados para serviços externos.

## Hardware externo

O monitor automotivo de 7 polegadas utiliza entrada de vídeo composto e será integrado somente depois que o aplicativo estiver funcional no celular. Não introduzir código específico de saída de vídeo na V0.1.
