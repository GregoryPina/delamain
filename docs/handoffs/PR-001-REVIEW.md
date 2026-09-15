# PR #1 — revisão estática Astra

PR: https://github.com/GregoryPina/delamain/pull/1
HEAD revisado: `8351703787b01fe598e31307d3dab43ffdefa902`.
Base de trabalho confirmada via merge-base: `d3e341a9d684f1fd2fb6d9490c8c6d554fbf502c`.
Status: CHANGES REQUIRED, um ajuste localizado em TASK-004.
Builds/testes executados nesta revisão: nenhum, conforme preferência do proprietário.

## Escopo conferido

12 arquivos alterados. TASK-005 troca app_name, manifesto, HUD e gatilho para VEXA, mantendo identificadores técnicos. Testes do motor ativo preservam cobertura e adicionam rejeição do nome antigo sem ação. Nenhuma alteração no executor Android, assets, Gradle ou conexão do roteador remoto à UI.

## P2 — normalização remove caracteres da sintaxe numérica

Em CommandRouter, `TERMINAL_PUNCTUATION = Regex("[\\p{P}]+$")` remove qualquer pontuação Unicode terminal antes de validar o comando inteiro. Isso inclui `%`, `/` e `-`. Por leitura do fluxo, `volume 20/`, `volume 20-` e `volume 20%%` viram `volume 20` e são aceitos, em vez de Unknown. Os testes novos não cobrem esses casos.

Restringir remoção à pontuação de encerramento explicitamente aceita, preservando a sintaxe de percentual, ou validar percentuais antes da normalização destrutiva. Adicionar regressões negativas para esses casos e positivas para `volume 20%`, `volume 20%!!!`, `Que horas são?` e comandos com ponto final. Não mudar o fluxo ativo, TASK-005, executor remoto ou ampliar funcionalidades.

## Próximo passo

Executor atualiza o mesmo PR/branch e retorna novo HEAD, arquivos, resumo e testes criados/não executados. Astra revisa o ajuste; proprietário executa build/testes. Não integrar antes disso. A revisão estática de identidade VEXA não encontrou impedimentos, mas ainda depende de teste local.
