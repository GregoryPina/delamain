# VEXA — instruções para agentes de código

## Objetivo
Construir um aplicativo Android pessoal que funcione como assistente automotivo, inspirado na estética de Delamain/KITT. O projeto é para uso privado do proprietário.

## Documentação obrigatória

Antes de implementar qualquer mudança, ler:

1. `README.md`
2. `AGENTS.md`
3. `docs/ARCHITECTURE.md`
4. `docs/ROADMAP.md`
5. `docs/DESIGN.md` quando a tarefa envolver UI/visual
6. `docs/DEVELOPMENT.md` quando a tarefa envolver processo de desenvolvimento, testes ou agentes
7. `docs/PERSONALITY.md` e `docs/INTERACTIONS.md` quando a tarefa envolver respostas, comandos, voz ou IA
8. `docs/PROJECT_STATUS.md` para conhecer responsáveis, dependências e passagem de trabalho
9. `TECH_LEAD_HANDOFF.md` ao assumir a coordenação; `docs/DECISIONS.md` e a TASK atribuída em `tasks/` para trabalho externo

A documentação do repositório é a fonte de verdade do projeto. Não substituir decisões documentadas por uma arquitetura nova sem necessidade.

## Regras de desenvolvimento

- Astra é o Tech Lead e integrador local. Work é exclusivo do Astra: não criar ou reativar subagentes internos, workers ou instâncias paralelas. Esta regra substitui a divisão anterior com GPT-SOL interno.
- Trabalho externo é preparado como TASKs e prompts copiáveis; o proprietário abre os modelos fora do Work. Não presumir acesso deles ao filesystem local, execução de testes ou Git.
- Antes de despachar, sincronizar trabalho local válido com GitHub, sem descartar mudanças. Cada wave usa repositório, branch e BASE COMMIT exatos; nunca fornecer branch móvel como única referência.
- Cada executor externo recebe objetivo, arquivos permitidos, dependências e aceite. Tarefas de implementação exigem PR ou unified diff. Se tiver acesso autorizado de escrita, trabalhar em branch própria e abrir PR; nunca push em main ou merge por conta própria. Sem acesso, devolver patch.
- Astra revisa e aplica; o proprietário executa builds e testes locais e informa resultados. Não executar testes automaticamente no Work. Até confirmação, registrar “implementado, aguardando teste”; não confundir revisão estática com validação funcional.
- Registrar entregas em `docs/handoffs/`; Astra mantém `docs/PROJECT_STATUS.md`, decisões e `TECH_LEAD_HANDOFF.md`. Usar os caminhos existentes, sem duplicar documentos de estado.
- Preservar código funcional e trabalho local. Não usar reset/clean/restore destrutivos para resolver divergências. Correções maiores voltam como PATCH REQUEST externo.
- Não marcar tarefa como concluída só porque o código foi escrito. Distinguir implementação, testes automáticos e validação do proprietário.
- Na substituição temporária do Tech Lead, registrar starting/ending baseline e evidências em `TEMP_LEAD_HANDOFF.md`; ao retomar, revisar esse delta antes de planejar. Atualizar estado atual, sem manter instruções antigas como próximo passo.

- Não adicionar funcionalidades não solicitadas apenas por iniciativa própria.
- Trabalhar em incrementos pequenos e compiláveis.
- Antes de alterar arquitetura, consultar `docs/ARCHITECTURE.md`.
- Atualizar `docs/ROADMAP.md` quando uma etapa for concluída ou mudar.
- Preferir APIs Android oficiais e bibliotecas maduras.
- Evitar dependências pesadas sem justificativa.
- Priorizar baixa latência e funcionamento local para comandos simples.
- Não enviar áudio para serviços externos sem uma ação explícita que necessite disso.
- Não implementar acesso irrestrito ao sistema. Ações do assistente devem passar por comandos/ações permitidos.
- Não adicionar chaves de API, tokens, senhas ou credenciais ao repositório.
- Não colocar segredos em código-fonte, `local.properties` versionado ou recursos do app.
- Fornecer roteiro e comando de build após mudanças relevantes para execução pelo proprietário.
- Se algo não puder ser testado no ambiente, declarar isso claramente em vez de afirmar que funciona.

## Estilo de código

- Kotlin.
- Preferir Jetpack Compose para a interface.
- Código simples e legível.
- Nomes descritivos.
- Comentários somente quando explicarem uma decisão não óbvia.

## Hardware alvo

O aplicativo não deve depender inicialmente de um aparelho específico. O primeiro aparelho de teste previsto é um Xiaomi Redmi 13. Um Redmi 6 também poderá ser usado futuramente.

A conexão com o monitor automotivo de 7 polegadas é uma etapa posterior e não deve bloquear o desenvolvimento do app.

## Ordem de implementação

1. Fundação do projeto.
2. UI fullscreen e tela de boot.
3. Estados visuais do personagem/interface.
4. Motor de comandos locais.
5. TTS.
6. STT/wake word.
7. Integração de IA.
8. Mídia/Bluetooth/GPS/câmera.
9. Integração com tela externa.

## Critério de conclusão

Uma tarefa só é considerada concluída quando o código estiver implementado, compilação/testes relevantes forem executados quando possível e a documentação necessária estiver atualizada.
