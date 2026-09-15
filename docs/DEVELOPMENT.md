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

### Coordenação e economia de tokens

O GPT-Astra é responsável por planejamento, contratos, revisão, aplicação local, builds, testes e Git. Por instrução posterior do proprietário, Work é exclusivo do Astra: não criar ou reativar agentes internos. Os registros de GPT-SOL em handoffs anteriores são históricos.

O quadro fica em [PROJECT_STATUS.md](PROJECT_STATUS.md); a retomada começa em [TECH_LEAD_HANDOFF.md](../TECH_LEAD_HANDOFF.md). Agentes externos são abertos pelo proprietário e normalmente só leem GitHub. Não presumir acesso local nem resultados de testes.

Fluxo de cada incremento:

1. Astra preserva alterações, valida, cria checkpoint, faz commit/push e registra baseline exata.
2. Define TASKs em `tasks/` e waves por dependência; agrupa tarefas com arquivos compartilhados.
3. Entrega EXTERNAL AGENT DISPATCH copiável com repositório, branch, BASE COMMIT, TASKs e saída esperada.
4. Executor externo devolve patch/arquivos e limitações. O proprietário traz a solução para Astra.
5. Astra confere baseline e diff, aplica e testa. Correções pequenas podem ser locais; mudanças relevantes viram PATCH REQUEST.
6. Após validação, commit/push e atualização de baseline e handoff. Nenhuma implementação pesada é enviada a agentes internos.

Cada passagem deve incluir: status; resumo; arquivos alterados; interfaces e exemplos de uso; comandos de validação e resultados; limitações; decisões; próximo responsável. Se teste não foi executado, informar o motivo. Aprovação funcional do proprietário é evidência válida, identificada como relato, sem atribuir ao agente um teste que ele não fez.

Usar um executor externo por recorte, paralelo somente para trabalho independente. Não criar tarefas na barra lateral ou automações sem pedido específico. Repositório local pode estar adiante do GitHub; nenhum despacho é liberado enquanto a baseline não estiver publicada.

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

Para comandos e personalidade, usar os cenários de aceite de [INTERACTIONS.md](INTERACTIONS.md). Testar primeiro com entradas de texto e provider falso que registre chamadas: os reflexos locais devem produzir zero chamadas de IA. Testes de voz no dispositivo devem incluir ausência de rede, ruído ambiente, interrupção e retomada do app.

Alterações apenas de documentação exigem revisão de consistência e links, sem declarar build validado. O Gradle Wrapper 8.9 foi restaurado em BUILD-01. Com um JDK compatível configurado em `JAVA_HOME` e o SDK Android disponível por configuração local, executar no Windows:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest assembleRelease
```

Na entrega V0.2-A essa sequência passou usando o JBR instalado; consulte [BUILD-01](handoffs/BUILD-01.md) e [QA-01](handoffs/QA-01.md) para ambiente, resultados e roteiro manual. Não versionar `local.properties`. A variante release produz APK não assinado; usar o APK debug para o teste de desenvolvimento.

## Segurança e privacidade

- Nenhuma chave de API deve ser commitada.
- Credenciais devem ficar fora do repositório.
- O aplicativo deve solicitar somente permissões necessárias para a funcionalidade atual.
- O caminho local de comandos deve permanecer local.
- O usuário deve ter controle explícito sobre integrações que enviem dados para serviços externos.

## Hardware externo

O monitor automotivo de 7 polegadas utiliza entrada de vídeo composto e será integrado somente depois que o aplicativo estiver funcional no celular. Não introduzir código específico de saída de vídeo na V0.1.
