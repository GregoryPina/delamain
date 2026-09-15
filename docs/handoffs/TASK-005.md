# TASK-005 — identidade exibida VEXA

Status: implementado, aguardando teste do proprietário.
Base: `d3e341a9d684f1fd2fb6d9490c8c6d554fbf502c`.

## Implementação

A companion passa a ser exibida e chamada por texto como VEXA, preservando a identidade técnica do aplicativo.

- `app_name` agora é `VEXA`;
- o manifesto usa `@string/app_name` no label;
- o HUD usa o mesmo recurso e exibe `VEXA // V0.1`, sem alteração de layout/efeitos;
- `LocalCommandEngine` aceita `Vexa` como nome isolado e prefixo opcional;
- `Delamain` deixou de funcionar como gatilho digitado;
- testes existentes foram atualizados para VEXA e foi adicionada regressão explícita para rejeição do nome antigo;
- README, personalidade e contrato de interações distinguem VEXA dos nomes técnicos preservados.

Não foram renomeados repositório, `applicationId`, namespace, pacotes, classes, arquivos, tema, assets ou pastas. A instalação existente deve permanecer a mesma porque o identificador do aplicativo não foi alterado.

## Validação pelo proprietário

Nenhum build ou teste foi executado por este executor. Executar:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease
```

Resultado esperado: testes unitários verdes e builds debug/release concluídos.

No APK debug, verificar também:

1. launcher mostra `VEXA`;
2. HUD mostra `VEXA // V0.1`;
3. painel DEV reconhece `Vexa`, `VEXA, tá aí?` e `Vexa, aumente o volume`;
4. `Delamain`, `Delamain, bom dia` e `Delamain, aumente o volume` retornam desconhecido e não executam ação.

A validação física do volume continua separada das mudanças de identidade.

## Fechamento

Proprietário confirmou teste ok no HEAD 39d9fce03943760eb722bd31b557596653a15798. PR #1 integrado em 85d249df33d5722cec74532bcee8dd6a58d2b3af. Relato sem logs detalhados; testes não repetidos por Astra. Status atual: concluída e integrada.
