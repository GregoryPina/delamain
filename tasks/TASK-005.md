# TASK-005 — nome da companion: VEXA

## Objective

Aplicar o nome VEXA solicitado pelo proprietário, preservando identidade técnica e comportamento do aplicativo.

## Current / desired behavior

Hoje app label, HUD e gatilho de chamada dizem Delamain. Passar a VEXA: “Vexa”, “VEXA, tá aí?” e “Vexa, aumente o volume” devem usar o fluxo ativo existente. O nome antigo deixa de ser gatilho; atualizar testes correspondentes sem perder cobertura de negações/compostos.

## Relevant files

AndroidManifest.xml, res/values/strings.xml, ui/DelamainApp.kt (somente texto do HUD), domain/LocalCommandEngine.kt (gatilho), LocalCommandEngineTest.kt, README.md e docs/PERSONALITY.md / INTERACTIONS.md. Criar docs/handoffs/TASK-005.md.

## Constraints

- NÃO renomear repositório GregoryPina/delamain, applicationId/namespace, pacotes Kotlin, classes, arquivos, tema, assets ou pastas. Delamain/KITT pode permanecer como referência estética e em registros históricos.
- Centralizar nome exibido em recurso app_name e referenciá-lo no manifesto/HUD quando adequado; não alterar layout ou efeitos.
- Não adicionar voz, wake word ou IA. Trata-se apenas de texto e chamada digitada.
- README deve distinguir nome VEXA de identificadores técnicos preservados. Não reescrever handoffs históricos nem docs centrais do coordenador.

## Dependencies / acceptance

Mesma wave de TASK-004; arquivos de código distintos. Testes existentes mantêm cobertura após substituir gatilho; incluir nome isolado, prefixo, caixa/acentos/pontuação e rejeição do nome antigo. Mudança deve preservar instalação existente e dados por applicationId inalterado.

## Validation / expected output

PR ou unified diff obrigatório, mesmo se sem acesso de escrita. Proprietário executará build e verificará nome no launcher/HUD, “Vexa, tá aí?” e comando de volume. Informar “aguardando teste do proprietário” até confirmação. Não fazer push em main nem merge.
