# TASK-003 — revisão para consolidar motores locais

## Objective

Propor consolidação pequena entre o motor ativo local e a contribuição de comandos recebida do GitHub, preservando comportamento validado.

## Current behavior

`LocalCommandEngine` atende painel DEV, reflexos, hora e volume com resultados tipados. `domain/command/CommandRouter` e `platform/AndroidCommandExecutor` foram produzidos separadamente no remoto e ainda não estão ligados à UI. Há sobreposição de reconhecimento e execução.

## Desired behavior

Um plano de migração e, se viável em recorte pequeno, um patch que elimine sobreposição sem ativar mídia/apps/volume percentual prematuramente. Preservar presença informal, aliases, Unknown para negações/compostos e confirmação honesta de volume.

## Relevant files/modules

Leia AGENTS.md, docs/ARCHITECTURE.md, docs/DECISIONS.md, docs/PROJECT_STATUS.md e docs/handoffs/LOCAL-02.md. Examine apenas domain/, platform/, integration/audio/, painel debug e testes relacionados.

## Requirements / constraints

- Usar BASE COMMIT exato fornecido no despacho; não presumir filesystem local.
- Não criar agentes internos, alterar rosto/assets, dependências, voz, rede ou arquitetura do zero.
- Analisar falsos positivos por substring e negação, play/pause versus pausa idempotente, confirmação de sucesso sem evidência e disponibilidade/permissões ao abrir apps.
- Não conectar código remoto à interface só porque compila. Preservar trabalho útil e justificar cada remoção/migração proposta.
- Nenhum Git local, commit ou push pelo executor externo.

## Dependencies / wave

Wave 1: baseline sincronizada e publicada. Um executor externo; não paralelizar alterações nesses mesmos módulos. Tarefas de implementação seguintes dependem da revisão deste resultado.

## Acceptance criteria / validation

Plano objetivo com contratos e testes de regressão. Se devolver patch, os 18 testes existentes devem continuar passando; adicionar apenas testes necessários aos problemas tratados. Astra executará `assembleDebug testDebugUnitTest assembleRelease`. Não afirmar teste que não executou.

## Expected output

APPROVED ou CHANGES REQUIRED sobre a proposta atual; achados por prioridade; plano pequeno de consolidação; unified diff opcional com arquivos/testes/riscos. Não devolver uma reescrita ampla.
